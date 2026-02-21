/*
 * Copyright (c) 2025 Nebulit GmbH
 * Licensed under the MIT License.
 */

var Generator = require('yeoman-generator');
var slugify = require('slugify')
var fs = require('fs');
const { _packageFolderName } = require('../../common/util/naming');
const { lowercaseFirstCharacter } = require('../../common/util/util');

let config = {}

module.exports = class extends Generator {

    defaultAppName = "app"

    constructor(args, opts) {
        super(args, opts);
        this.argument('appname', { type: String, required: false });
        config = require(this.env.cwd + "/config.json");
    }

    initializing() {
        this.answers = {
            appName: config?.codeGen?.application ?? this.defaultAppName,
            rootPackageName: config?.codeGen?.rootPackage
        };
        this.displayInfo();
    }

    // Async Await
    async prompting() {
        this.answers = await this.prompt([{
            type: 'input',
            name: 'appName',
            message: 'Projectame?',
            default: this.answers.appName,
            when: () => !config?.codeGen?.application,
        }, {
            type: 'input',
            name: 'rootPackageName',
            message: 'Root Package?',
            default: this.answers.rootPackageName,
            when: () => !config?.codeGen?.rootPackage,
        },
        {
            type: 'list',
            name: 'generatorType',
            message: 'What should be generated?',
            choices: ['Skeleton', 'slices', 'aggregates', 'adapters', 'Tests']
        }]);
    }

    setDefaults() {
        // Ensure defaults are set if not prompted
        if (!this.answers.appName) {
            this.answers.appName = config?.codeGen?.application ?? this.defaultAppName
        }
        if (!this.answers.rootPackageName) {
            this.answers.rootPackageName = config?.codeGen?.rootPackage
        }
    }

    displayInfo() {
        const configPath = this.env.cwd + "/config.json";
        let configTime = 'N/A';
        try {
            const stats = fs.statSync(configPath);
            configTime = stats.mtime.toLocaleString();
        } catch (e) {
            // handle error if needed
        }

        const rootPackageEffective = this.answers.rootPackageName || config?.codeGen?.rootPackage || '';
        const targetPath = this.destinationPath(`src/main/kotlin/${_packageFolderName(rootPackageEffective, config?.codeGen?.contextPackage, false)}`);

        this.log('\n--- Generator Configuration ---');
        this.log(`Project Name:     ${this.answers.appName || config?.codeGen?.application || this.defaultAppName}`);
        this.log(`Context:          ${config?.codeGen?.contextPackage ?? 'N/A'}`);
        this.log(`Target Directory: ${targetPath}`);
        this.log(`Config Updated:   ${configTime}`);
        this.log('-------------------------------\n');
    }

    writing() {

        if (this.answers.generatorType === 'Skeleton') {
            this._writeSkeleton();
        } else if (this.answers.generatorType === 'slices') {
            this.log('starting commands generation')
            this.composeWith(require.resolve('../slices'), {
                answers: this.answers,
                appName: this.answers.appName ?? this.appName
            });
        } else if (this.answers.generatorType === 'aggregates') {
            this.log('starting aggregates generation')
            this.composeWith(require.resolve('../aggregates'), {
                answers: this.answers,
                appName: this.answers.appName ?? this.appName
            });
        } else if (this.answers.generatorType === 'adapters') {
            this.log('starting adapters generation')
            this.composeWith(require.resolve('../adapters'), {
                answers: this.answers,
                appName: this.answers.appName ?? this.appName
            });
        } else if (this.answers.generatorType === 'Tests') {
            this.log('starting tests generation')
            this.composeWith(require.resolve('../specifications'), {
                answers: this.answers,
                appName: this.answers.appName ?? this.appName
            });
        }
    }

    _writeSkeleton() {
        this.fs.copyTpl(
            this.templatePath('root'),
            this.destinationPath("."),
            {
                rootPackageName: this.answers.rootPackageName,
                appName: this.answers.appName !== "." ? slugify(this.answers.appName) : "app",
            }
        )
        this.fs.copyTpl(
            this.templatePath('src'),
            this.destinationPath(`./src/main/kotlin/${this.answers.rootPackageName.split(".").join("/")}`),
            {
                rootPackageName: this.answers.rootPackageName
            }
        )
        this.fs.copyTpl(
            this.templatePath('test'),
            this.destinationPath(`./src/test/kotlin/${this.answers.rootPackageName.split(".").join("/")}`),
            {
                rootPackageName: this.answers.rootPackageName
            }
        )
        this.fs.copyTpl(
            this.templatePath('git/gitignore'),
            this.destinationPath(`./.gitignore`),
            {
                rootPackageName: this.answers.rootPackageName
            }
        )

    }

    end() {
    }
};
