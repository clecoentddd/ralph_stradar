
const config = require('./config.json');

const sliceName = "slice: Admin Connection";
const commandTitle = "To connect";
const fieldName = "connectionId";

const generatedFields = [];

// Simulate prompting logic
if (config.slices) {
    const selectedSlices = config.slices.filter(s => s.title === sliceName);
    for (const slice of selectedSlices) {
        if (slice.commands) {
            for (const command of slice.commands) {
                if (command.fields) {
                    for (const field of command.fields) {
                        if (field.generated) {
                            generatedFields.push({
                                slice: slice.title,
                                command: command.title,
                                field: field.name
                            });
                        }
                    }
                }
            }
        }
    }
}

console.log("Generated Fields found in config:", generatedFields);

generatedFields.forEach(genField => {
    const key = `${genField.slice}:${genField.command}:${genField.field}`;
    console.log(`Key generated in prompting: '${key}'`);
});

const keyInWriting = `${sliceName}:${commandTitle}:${fieldName}`;
console.log(`Key generated in writing:   '${keyInWriting}'`);

if (generatedFields.length > 0) {
    const genField = generatedFields[0];
    const key1 = `${genField.slice}:${genField.command}:${genField.field}`;
    if (key1 === keyInWriting) {
        console.log("Keys MATCH");
    } else {
        console.log("Keys DO NOT MATCH");
    }
}
