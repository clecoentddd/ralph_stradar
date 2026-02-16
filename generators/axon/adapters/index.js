/*
 * Copyright (c) 2025 Nebulit GmbH
 * Licensed under the MIT License.
 */

var Generator = require('yeoman-generator');
var slugify = require('slugify')
const {
    _readmodelTitle,
    _sliceTitle,
} = require("./../../../generators/common/util/naming");
const { _packageName, _packageFolderName } = require("../../common/util/naming");
const { capitalizeFirstCharacter } = require("../../common/util/util");
const path = require('path');

let config = {}

function sanitize(it) {
    return it.replaceAll("-", "").replaceAll("_", "");
}

// Adapter title: same as _readmodelTitle but WITHOUT the "ReadModel" suffix
function _adapterTitle(title) {
    var titleElements = title.split(" ").map(it => capitalizeFirstCharacter(sanitize(it))).join("")
    return slugify(capitalizeFirstCharacter(sanitize(titleElements)), "")
}

module.exports = class extends Generator {

    constructor(args, opts) {
        super(args, opts);
        this.givenAnswers = opts.answers
        config = require(this.env.cwd + "/config.json");
    }

    _findAdapterSlices() {
        return config.slices.filter(
            (slice) => slice.sliceType === 'STATE_VIEW'
                && slice.title.toLowerCase().includes('adapter')
        )
    }

    _findAutomationInfo(adapterSlice) {
        var readmodel = adapterSlice.readmodels?.[0]
        if (!readmodel) return null

        var automationDep = readmodel.dependencies?.find(
            (dep) => dep.type === 'OUTBOUND' && dep.elementType === 'AUTOMATION'
        )
        if (!automationDep) return null

        var automationSlice = config.slices.find(
            (slice) => slice.processors?.some((p) => p.id === automationDep.id)
        )
        if (!automationSlice) return null

        var processor = automationSlice.processors.find((p) => p.id === automationDep.id)

        var inboundReadModelDeps = processor.dependencies?.filter(
            (dep) => dep.type === 'INBOUND' && dep.elementType === 'READMODEL'
        ) || []

        var allReadModels = config.slices.flatMap((s) => s.readmodels || [])
        var inboundReadModels = inboundReadModelDeps.map((dep) => {
            var rm = allReadModels.find((r) => r.id === dep.id)
            var parentSlice = config.slices.find((s) => s.readmodels?.some((r) => r.id === dep.id))
            return {
                title: dep.title,
                readmodel: rm,
                parentSlice: parentSlice,
                isAdapter: parentSlice?.title.toLowerCase().includes('adapter')
            }
        })

        return {
            automationSlice: automationSlice,
            processor: processor,
            inboundReadModels: inboundReadModels,
            adapterReadModel: inboundReadModels.find((rm) => rm.isAdapter),
            nonAdapterReadModel: inboundReadModels.find((rm) => !rm.isAdapter)
        }
    }

    _slicePackage(title) {
        return slugify(title.replace("slice:", "").trim(), "")
    }

    async prompting() {
        var adapterSlices = this._findAdapterSlices()

        if (adapterSlices.length === 0) {
            this.log('No adapter slices found (STATE_VIEW with "adapter" in title)')
            return
        }

        this.answers = await this.prompt([
            {
                type: 'checkbox',
                name: 'adapters',
                message: 'Which Adapters should be generated?',
                choices: adapterSlices.map((slice) => slice.title).sort()
            }
        ])

        if (!this.answers?.adapters || this.answers.adapters.length === 0) {
            return
        }

        // For each selected adapter, ask about ReadModel inclusion and compose with slices
        for (const adapterTitle of this.answers.adapters) {
            var adapterSlice = config.slices.find((s) => s.title === adapterTitle)
            if (!adapterSlice) continue

            var info = this._findAutomationInfo(adapterSlice)
            if (!info) {
                this.log(`  ⚠ No AUTOMATION relationship found for "${adapterTitle}"`)
                continue
            }

            var adapterRM = info.adapterReadModel
            var nonAdapterRM = info.nonAdapterReadModel
            var adapterName = adapterRM?.readmodel ? _adapterTitle(adapterRM.readmodel.title) : null

            this.log(`\n── Adapter: ${adapterTitle} ──`)
            this.log(`  OUTBOUND AUTOMATION: ${info.automationSlice.title} (processor: ${info.processor.title})`)
            this.log(`  INBOUND STATE_VIEWs:`)
            info.inboundReadModels.forEach((rm) => {
                this.log(`    - ${rm.parentSlice?.title ?? '??'} (ReadModel: ${rm.title})${rm.isAdapter ? ' [ADAPTER]' : ''}`)
            })

            // Ask whether to include the non-adapter ReadModel in the processor
            var includeReadModel = true
            if (nonAdapterRM?.readmodel) {
                var rmAnswer = await this.prompt([{
                    type: 'confirm',
                    name: 'includeReadModel',
                    message: `Include non-adapter ReadModel "${nonAdapterRM.title}" in the Processor? (No = Standalone)`,
                    default: true
                }])
                includeReadModel = rmAnswer.includeReadModel
            }

            // Store info for writing phase
            this._adapterInfos = this._adapterInfos || []
            this._adapterInfos.push(info)

            // Compose with slices generator — it will handle ALL generation (commands, events, RestResource, processor, etc.)
            // The adapterInfo flag tells slices to use WithAdapter processor templates
            this.log(`  Composing with slices generator for: ${info.automationSlice.title}`)
            this.composeWith(require.resolve('../slices'), {
                answers: {
                    ...this.givenAnswers,
                    slice: [info.automationSlice.title],
                    adapterInfo: { adapterName: adapterName, includeReadModel: includeReadModel }
                },
                appName: this.givenAnswers.appName ?? this.appName
            })
        }
    }

    writing() {
        if (!this._adapterInfos || this._adapterInfos.length === 0) {
            return
        }

        // Generate only the adapter class files — everything else is handled by the slices generator
        this._adapterInfos.forEach((info) => {
            this._writeAdapter(info)
        })
    }

    _writeAdapter(info) {
        var automationSlice = info.automationSlice
        var title = this._slicePackage(automationSlice.title).toLowerCase()
        var adapterRM = info.adapterReadModel

        if (!adapterRM?.readmodel) {
            this.log(`  ⚠ No adapter readmodel found, skipping adapter generation`)
            return
        }

        var adapterName = _adapterTitle(adapterRM.readmodel.title)

        // Find ALL collection fields (Custom type with List cardinality)
        var fields = adapterRM.readmodel.fields || []
        var collectionFields = fields.filter(f => f.type === 'Custom' && f.cardinality === 'List')

        var templateVars = {
            _slice: title,
            _packageName: _packageName(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, false),
            _name: adapterName,
            link: `https://miro.com/app/board/${config.boardId}/?moveToWidget=${adapterRM.readmodel.id}`,
        }

        if (collectionFields.length > 1) {
            // Multiple collection fields → use APIDataWrapper
            this.log(`  [adapter] Found ${collectionFields.length} collection fields → generating APIDataWrapper`)
            var multipleCollectionFields = collectionFields.map(cf => ({
                fieldName: cf.name,
                dataClassName: `${capitalizeFirstCharacter(cf.name)}Data`,
                subfields: (cf.subfields || []).map(sf => ({
                    name: sf.name,
                    type: this._kotlinType(sf.type)
                }))
            }))
            templateVars._multipleCollectionFields = multipleCollectionFields
            templateVars._collectionField = null
            templateVars._subfields = []
            templateVars._dataClassName = null
        } else if (collectionFields.length === 1) {
            // Single collection field → direct List<DataClass> return
            var collectionField = collectionFields[0]
            var subfields = (collectionField.subfields || []).map(sf => ({
                name: sf.name,
                type: this._kotlinType(sf.type)
            }))
            templateVars._multipleCollectionFields = null
            templateVars._collectionField = collectionField
            templateVars._subfields = subfields
            templateVars._dataClassName = `${adapterName}Data`
        } else {
            // No collection fields → fallback to Any
            templateVars._multipleCollectionFields = null
            templateVars._collectionField = null
            templateVars._subfields = []
            templateVars._dataClassName = null
        }

        this.log(`  Generating adapter: ${adapterName}.kt`)
        this.fs.copyTpl(
            path.join(__dirname, '..', 'slices', 'templates', 'src', 'components', 'Adapter.kt.tpl'),
            this.destinationPath(`./src/main/kotlin/${_packageFolderName(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, false)}/${title}/internal/adapter/${adapterName}.kt`),
            templateVars)
    }

    _kotlinType(type) {
        switch (type?.toLowerCase()) {
            case 'string': return 'String'
            case 'long': return 'Long'
            case 'int': return 'Int'
            case 'double': return 'Double'
            case 'boolean': return 'Boolean'
            case 'uuid': return 'UUID'
            case 'date': return 'LocalDate'
            case 'datetime': return 'LocalDateTime'
            default: return 'String'
        }
    }
}
