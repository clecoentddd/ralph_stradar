const fs = require('fs');
const config = JSON.parse(fs.readFileSync('c:/Users/chris/ralph/axon-code-gen/stradar/config.json', 'utf8'));

console.log("--- CONFIG DIAGNOSTIC ---");
console.log("Total Slices:", config.slices.length);

const eventId = "3458764645830373813"; // Team Created
const commandId = "3458764645830671029"; // Create Team

console.log("\nSearching for Event ID:", eventId);
const allEvents = config.slices.flatMap(s => s.events || []);
const event = allEvents.find(e => e.id === eventId);
if (event) {
    console.log("Found Event:", event.title, "(Slice:", event.slice, ")");
    const cmdRef = event.dependencies.find(d => d.type === "INBOUND" && d.elementType === "COMMAND");
    if (cmdRef) {
        console.log("Found Inbound Command Dependency ID:", cmdRef.id, "(Type:", typeof cmdRef.id, ")");
        console.log("Is it exactly the target commandId?", cmdRef.id === commandId);
    } else {
        console.log("No INBOUND COMMAND dependency found for this event.");
    }
} else {
    console.log("Event NOT FOUND in any slice!");
}

console.log("\nSearching for Command ID:", commandId);
const allCommands = config.slices.flatMap(s => s.commands || []);
const command = allCommands.find(c => c.id === commandId);
if (command) {
    console.log("Found Command:", command.title, "(Slice:", command.slice, ")");
    console.log("Command ID Type:", typeof command.id);
} else {
    console.log("Command NOT FOUND in any slice!");
}

console.log("\n--- Slices with no commands attribute: ---");
config.slices.forEach(s => {
    if (!s.commands) console.log("Slice has NO commands property:", s.title);
});

console.log("\n--- END DIAGNOSTIC ---");
