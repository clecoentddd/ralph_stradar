/*
 * Copyright (c) 2025 Nebulit GmbH
 * Licensed under the MIT License.
 */

var Generator = require('yeoman-generator');
var slugify = require('slugify')
const { v4: uuidv4 } = require('uuid');
const {
    _eventTitle,
    _readmodelTitle,
    _commandTitle,
    _aggregateTitle,
    _packageName,
    _packageFolderName
} = require("../../common/util/naming");
const { lowercaseFirstCharacter, uniqBy, splitByCamelCase, idField } = require("../../common/util/util");
const { idType, customItemTypeName } = require("../../common/util/generator");


function _sliceTitle(title) {
    return slugify(title.replace("slice:", ""), "").replaceAll("-", "").toLowerCase()
}

var config = {}

module.exports = class extends Generator {

    constructor(args, opts) {
        super(args, opts);
        this.givenAnswers = opts.answers
        config = require(this.env.cwd + "/config.json");
    }

    async prompting() {
        if (this.givenAnswers.generatorType !== 'Tests') {
            return;
        }

        const sliceChoices = config.slices.map(slice => ({ name: slice.title, value: slice.title }));

        const sliceAnswer = await this.prompt([{
            type: 'list',
            name: 'slice',
            message: 'Select Slice you want to generate the tests for',
            choices: sliceChoices
        }]);

        const selectedSlice = config.slices.find(s => s.title === sliceAnswer.slice);
        const specChoices = (selectedSlice.specifications || []).map(spec => ({
            name: spec.title,
            value: spec.id
        }));

        const specAnswer = await this.prompt([{
            type: 'checkbox',
            name: 'specifications',
            message: 'Select tests to generate',
            choices: specChoices
        }]);

        this.givenAnswers.slice = sliceAnswer.slice;
        this.selectedSpecIds = specAnswer.specifications;
    }

    writeSpecifications() {
        this._writeSpecifications();
    }

    _writeSpecifications() {
        var slice = this._findSlice(this.givenAnswers.slice)
        var title = _sliceTitle(slice.title).toLowerCase()

        slice.specifications?.filter(it => !it?.vertical)
            .filter(it => !this.selectedSpecIds || this.selectedSpecIds.includes(it.id))
            .forEach((specification) => {

                var given = specification.given.sort((a, b) => a.index - b.index)
                var when = specification.when?.[0]
                var then = specification.then.sort((a, b) => a.index - b.index)
                var comment = specification?.comments?.map(it => it.description)?.join("\n")

                var allElements = given.concat(when).concat(then).filter(item => item);
                var allFields = allElements.flatMap((item) => item.fields)
                var _elementImports = generateImports(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, title, allElements)
                var _typeImports = typeImports(allFields)
                var aggregateId = uuidv4()
                var defaults = {
                    "aggregateId": aggregateId
                }

                var givenCommands = given.map(ge => {
                    const event = config.slices.flatMap(s => s.events).find(e => e.id === ge.linkedId);
                    if (!event) return null;
                    const commandRef = event.dependencies.find(d => d.type === "INBOUND" && d.elementType === "COMMAND");
                    if (!commandRef) return null;
                    const command = config.slices.flatMap(s => s.commands).find(c => c.id === commandRef.id);
                    return { command, spec: ge };
                }).filter(it => it);

                var commands = uniqBy(givenCommands.map(gc => gc.command), it => it.title);

                var _commandImports = this._commandImports(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, commands);


                if (slice.processors?.length > 0) {

                    let specificationName = _specificationTitle(capitalizeFirstCharacter(slugify(specification.title, "")),)

                    var elementImports = generateImports(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, title, then)

                    //for now only result events supported
                    this.fs.copyTpl(
                        this.templatePath(`src/components/ProcessorSpecification.kt.tpl`),
                        this.destinationPath(`./src/test/kotlin/${_packageFolderName(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, false)}/${title}/integration/${specificationName}.kt`),
                        {
                            _slice: title,
                            _comment: comment,
                            _rootPackageName: this.givenAnswers.rootPackageName,
                            _packageName: _packageName(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, false),
                            _name: specificationName,
                            _testname: splitByCamelCase(specificationName),
                            _elementImports: elementImports,
                            _commandImports: _commandImports,
                            _typeImports: _typeImports,
                            _given: this._renderReadModelGiven(commands),
                            _then: this._renderProcessorThen(then),
                            // take first aggregate
                            _aggregate: _aggregateTitle((slice.aggregates || [])[0]),
                            _aggregateId: aggregateId,
                            link: boardlLink(config.boardId, specification.id)

                        }
                    );
                }

                if (then.some(it => it.type === "SPEC_READMODEL")) {

                    let specificationName = _specificationTitle(capitalizeFirstCharacter(slugify(specification.title, "")), "ReadModel")
                    var readModel = then.find(it => it.type === "SPEC_READMODEL");

                    var _queryImports = this._queryImports(title, this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, _readmodelTitle(readModel.title));

                    //for now only result events supported
                    this.fs.copyTpl(
                        this.templatePath(`src/components/ReadModelSpecification.kt.tpl`),
                        this.destinationPath(`./src/test/kotlin/${_packageFolderName(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, false)}/${title}/integration/${specificationName}.kt`),
                        {
                            _slice: title,
                            _comment: comment,
                            _rootPackageName: this.givenAnswers.rootPackageName,
                            _packageName: _packageName(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, false),
                            _name: specificationName,
                            _testname: splitByCamelCase(specificationName),
                            _elementImports: _elementImports,
                            _commandImports: _commandImports,
                            _queryImports: _queryImports,
                            _typeImports: _typeImports,
                            _when: when ? renderWhen(when, then, defaults) : "",
                            _given: this._renderReadModelGiven(givenCommands),
                            _then: this._renderReadModelThen(givenCommands, then, defaults),
                            // take first aggregate
                            _aggregate: _aggregateTitle((slice.aggregates || [])[0]),
                            _aggregateId: aggregateId,
                            link: boardlLink(config.boardId, specification.id)

                        }
                    );
                } else if (when) {
                    // command test
                    let specificationName = _specificationTitle(capitalizeFirstCharacter(slugify(specification.title, "")), "")
                    let idAttribute = idField(when)
                    let idFieldType = idType(when)

                    var idFieldString = `var ${idAttribute}:${idFieldType} = RandomData.newInstance<${idFieldType}> {}`

                    this.fs.copyTpl(
                        this.templatePath(`src/components/Specification.kt.tpl`),
                        this.destinationPath(`./src/test/kotlin/${_packageFolderName(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, false)}/${title}/${specificationName}.kt`),
                        {
                            _idAttribute: idFieldString,
                            _slice: title,
                            _comment: comment,
                            _command: specification.command,
                            _rootPackageName: this.givenAnswers.rootPackageName,
                            _packageName: _packageName(this.givenAnswers.rootPackageName, config.codeGen?.contextPackage, false),
                            _name: specificationName,
                            _testname: splitByCamelCase(specificationName),
                            _elementImports: _elementImports,
                            _typeImports: _typeImports,
                            _given: renderGiven(given, defaults),
                            _when: renderWhen(when, then, defaults),
                            _then: renderThen(when, then, defaults),
                            _thenExpectations: renderThenExpectation(when, then, defaults),
                            // take first aggregate
                            _aggregate: _aggregateTitle((slice.aggregates || [])[0]),
                            _aggregateId: aggregateId,
                            link: boardlLink(config.boardId, specification.id)

                        }
                    );
                }
            })

    }

    _commandImports(rootPackage, contextPackage, commands) {
        return commands.map(it => `import ${_packageName(rootPackage, contextPackage, false)}.domain.commands.${_sliceTitle(this._findSliceByCommandId(it.id)?.title)}.${_commandTitle(it.title)}`).join("\n")
    }

    _queryImports(slice, rootPackageName, contextPackage, readModel) {
        return `import ${_packageName(rootPackageName, contextPackage, false)}.${slice}.${readModel}Query
 import ${_packageName(rootPackageName, contextPackage, false)}.${slice}.${readModel}`
    }

    _renderProcessorThen(then) {
        return then?.length > 0 ? `
          awaitUntilAssserted {
           ${then.map(event => `streamAssertions.assertEvent(${idField(event)}.toString()) { it is ${_eventTitle(event.title)}}
           }`).join("\n")
            }
        ` : ""
    }

    _renderReadModelGiven(givenCommands) {
        if (!givenCommands || givenCommands.length === 0) return "";

        var firstCmd = givenCommands[0].command;
        var idFieldValue = idField(firstCmd) || "aggregateId";
        var idFieldType = idType(firstCmd) || "UUID";

        var commandExecution = givenCommands.map((gc, index) => {
            const cmd = gc.command;
            const spec = gc.spec;
            var cmdTitle = _commandTitle(cmd.title);
            var varName = `${lowercaseFirstCharacter(cmdTitle)}${index > 0 ? index : ""}`;

            return `
        var ${varName} = RandomData.newInstance<${cmdTitle}> {
${randomizedInvocationParamterList(spec.fields, { [idFieldValue]: idFieldValue }, "\n", "            this")}
        }
        commandGateway.sendAndWait<Any>(${varName})
            `;
        }).join("\n")

        return `val ${idFieldValue} = RandomData.newInstance<${idFieldType}> {}
        
        ${commandExecution}
        `
    }


    _renderReadModelThen(givenCommands, thenList, defaults) {
        return thenList.map(thenSpec => {
            const readModel = config.slices.flatMap(s => s.readmodels).find(rm => rm.id === thenSpec.linkedId);
            const queryCode = this._generateQuery(thenSpec, givenCommands.map(gc => gc.command));

            let assertions = "";
            if (thenSpec.fields && thenSpec.fields.length > 0) {
                assertions = thenSpec.fields.filter(f => f.example !== "" && f.example !== null).map(f => {
                    const expectedValue = renderVariable(f.example, f.type, f.name, defaults);
                    if (readModel.listElement) {
                        return `assertThat(result.map { it.${f.name} }).contains(${expectedValue})`;
                    } else {
                        return `assertThat(result.${f.name}).isEqualTo(${expectedValue})`;
                    }
                }).join("\n            ");
            }

            if (!assertions) {
                assertions = readModel?.listElement ? "assertThat(result).isNotEmpty" : "assertThat(result).isNotNull()";
            }

            return `
        awaitUntilAssserted {
            val result = ${queryCode}.get()
            ${assertions}
        }
            `;
        }).join("\n");
    }

    _generateQuery(readModelSpec, commands) {
        var readModel = config.slices.flatMap((item) => item.readmodels).find((it) => it.id === readModelSpec.linkedId)
        var readModelTitle = _readmodelTitle(readModel.title)
        var readModelIdFields = readModel.fields.filter(it => it.idAttribute).map(it => it.name)

        var commandIdSources = readModelIdFields.reduce((acc, field) => {
            var matchingCommand = commands.find(command =>
                command.fields.some(commandField => commandField.name === field)
            );

            if (matchingCommand) {
                acc.push({
                    name: field,
                    command: matchingCommand
                });
            }

            return acc;
        }, []);


        if (readModel.listElement ?? false) {
            return `queryGateway.query(${readModelTitle}Query(), ${readModelTitle}::class.java)`
        } else {
            if (readModelIdFields.length <= 0) {
                var idFieldValue = commands.length > 0 ? idField(commands[0]) : "aggregateId"
                return `queryGateway.query(${readModelTitle}Query(${idFieldValue}), ${readModelTitle}::class.java)`
            } else {
                return `queryGateway.query(${readModelTitle}Query(${commandIdSources.map(it => `${lowercaseFirstCharacter(_commandTitle(it.command.title))}.${it.name}`)}), ${readModelTitle}::class.java)`
            }
        }
    }


    _findSlice(sliceName) {
        return config.slices.find((item) => item.title === sliceName)
    }

    _findSliceByCommandId(id) {
        return config.slices.filter(it => it.commands.some(item => item.id === id))[0]
    }


};


const generateImports = (rootPackageName, contextPackage, sliceName, elements) => {
    var imports = elements?.map((element) => {
        switch (element.type?.toLowerCase()) {
            case "spec_event":
                return `import ${_packageName(rootPackageName, null, false)}.events.${_eventTitle(element.title)}`
            case "spec_command":
                return `import ${_packageName(rootPackageName, contextPackage, false)}.domain.commands.${sliceName}.${_commandTitle(element.title)}`
            case "spec_readmodel":
                return `import ${_packageName(rootPackageName, contextPackage, false)}.${sliceName}.${_readmodelTitle(element.title)}`
            default:
                console.log("Could not determine imports")
                return ""
        }
    })
    return Array.from(new Set(imports))?.flat()?.join("\n")
}

const typeImports = (fields) => {
    var imports = fields?.map((field) => {
        switch (field?.type?.toLowerCase()) {
            case "date":
                return ["import java.time.LocalDate", "import org.springframework.format.annotation.DateTimeFormat", "import java.time.format.DateTimeFormatter"]
            case "datetime":
                return ["import java.time.LocalDateTime", "import org.springframework.format.annotation.DateTimeFormat", "import java.time.format.DateTimeFormatter"]
            case "uuid":
                return ["import java.util.UUID"]
            default:
                return []
        }
        switch (field?.cardinality?.toLowerCase()) {
            case "LIST":
                return ["java.util.List"]
            default:
                return []
        }
    })
    return Array.from(new Set(imports?.flat()))?.join(";\n")

}

const defaultValue = (type, cardinality = "single", name, defaults) => {
    if (cardinality?.toLowerCase() !== "list" && defaults[name]) {
        return renderVariable(defaults[name], type, name, defaults)
    }
    switch (type.toLowerCase()) {
        case "string":
            return cardinality.toLowerCase() === "list" ? "[]" : "\"\"";
        case "boolean":
            return cardinality.toLowerCase() === "list" ? "[]" : "false";
    }
}


function _specificationTitle(title, postfix) {
    var adjustedTitle = title.replace("Spec:", "").replace("-", "").trim()
    var testName = `${slugify(capitalizeFirstCharacter(adjustedTitle), "")}${capitalizeFirstCharacter(postfix ?? "")}`
    return testName.endsWith("Test") ? testName : `${testName}Test`
}


function capitalizeFirstCharacter(inputString) {
    // Check if the string is not empty
    if (inputString.length > 0) {
        // Capitalize the first character and concatenate the rest of the string
        return inputString.charAt(0).toUpperCase() + inputString.slice(1);
    } else {
        // Return an empty string if the input is empty
        return "";
    }
}

function renderThenExpectation(when, thenList, defaults) {
    //in case no error render then
    var thens = thenList.map((item) => {
        if (item.type === "SPEC_EVENT") {
            return `
               expectedEvents.add(RandomData.newInstance<${_eventTitle(item.title)}> { 
               ${assertionList(item.fields, when.fields, defaults)}
                })   
                `
        }

    }).join("\n")

    if (thens?.length === 0 && !thenList.some((error) => error.type === "SPEC_ERROR")) {
        return "Assertions.fail<Unit>(\"No assertion defined in Model. Manual implementation required\")"
    }
    return thens
}

function renderThen(whenList, thenList, defaults) {

    if (thenList.some((error) => error.type === "SPEC_ERROR")) {
        // in case error render erro
        return `.expectException(CommandException::class.java)`
    } else {
        return `.expectSuccessfulHandlerExecution()
                .expectEvents(*expectedEvents.toTypedArray())`
    }
}


function renderWhen(whenCommand, thenList, defaults) {
    //only render when if no error occured
    return `val command = ${_commandTitle(whenCommand.title)}(
 \t\t\t\t${randomizedInvocationParamterList(whenCommand.fields, defaults)}
            )`

}

function renderGiven(givenList, paramDefaults) {
    var givens = givenList.map((event) => {
        var idFieldValue = idField(event)

        var defaults = idFieldValue ? { ...paramDefaults, [idFieldValue]: idFieldValue } : paramDefaults

        return `events.add(RandomData.newInstance<${_eventTitle(event.title)}> {
                        ${randomizedInvocationParamterList(event.fields, defaults, "\n", "this")}
                    })`
    }).join("\n")

    var given = `
     ${givens}
    `
    return given

}

function renderVariable(variable, defaults) {
    var value = variable.example;
    var name = variable.name;

    if (!value && defaults[name]) {
        value = defaults[name];
    }

    if (variable.cardinality?.toLowerCase() === "list") {
        if (Array.isArray(value)) {
            return `listOf(${value.map(val => renderSingleValue(variable, val, defaults)).join(", ")})`;
        }
        return "listOf()";
    }

    return renderSingleValue(variable, value, defaults);
}

function renderSingleValue(variable, value, defaults) {
    const variableType = variable.type.toLowerCase();
    switch (variableType) {
        case "uuid":
            return `UUID.fromString("${value}")`;
        case "string":
            return `"${value}"`;
        case "date":
            return `LocalDate.parse("${value}", DateTimeFormatter.ofPattern("dd.MM.yyyy"))`;
        case "datetime":
            return `LocalDateTime.parse("${value}", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))`;
        case "boolean":
        case "long":
        case "double":
        case "int":
            return `${value}`;
        case "custom":
            const typeName = customItemTypeName(variable.name);
            if (typeof value === 'object' && value !== null) {
                const params = (variable.subfields || []).map(sf => {
                    const sfValue = value[sf.name];
                    return `${sf.name} = ${renderSingleValue(sf, sfValue, defaults)}`;
                }).join(", ");
                return `${typeName}(${params})`;
            }
            return `${typeName}()`;
        default:
            return `${value}`;
    }
}

function randomizedInvocationParamterList(variables, defaults, separator = ",\n", assignmentPrefix) {

    return variables?.map((variable) => {
        if (variable.example !== "" && variable.example !== undefined && variable.example !== null) {
            return `\t${variable.name} = ${renderVariable(variable, defaults)}`
        } else if (variable.idAttribute) {
            return `\t${assignmentPrefix ? `${assignmentPrefix}.` : ""}${variable.name} = ${variable.name}`
        } else {
            if (Object.keys(defaults).includes(variable.name)) {
                return `\t${variable.name} = ${defaultValue(variable.type, variable.cardinality, variable.name, defaults)}`;
            } else {
                return `\t${variable.name} = RandomData.newInstance {  }`;
            }
        }
    }
    ).join(separator);

}

function assertionList(variables, assignmentValues, defaults) {
    return variables.map((variable) => {
        // if example data provided, take the example into assertion
        if (variable.example !== "") {
            return `\tthis.${variable.name} = ${renderVariable(variable.example, variable.type, variable.name, defaults)}`;
            // take the value from the command if available
        } else if (assignmentValues?.some(field => field.name === variable.name)) {
            return `\tthis.${variable.name} = command.${variable.name}`;
        } else if (variable.example === "" && defaults[variable.name]) {
            // is there any default? take the default
            return `\tthis.${variable.name} = ${renderVariable(defaults[variable.name], variable.type, variable.name, defaults)}`;
        } else {
            return `//this.${variable.name} = ...`
        }
    }).filter(it => it).join("\n");
}

function boardlLink(boardId, sliceId) {
    var link = `https://miro.com/app/board/${boardId}/?moveToWidget=${sliceId}`
    return boardId && sliceId ? link : undefined
}