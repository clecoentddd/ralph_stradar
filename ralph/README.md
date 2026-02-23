# Ralph - Event Modeling Code Generator with LLM Loop

Ralph is an AI-powered code generation tool that automatically implements event-sourced applications based on slice definitions. It uses Claude AI in a continuous loop to generate Kotlin/Spring Boot code following event modeling patterns.

## Overview

Ralph reads slice definitions (Product Requirements) from `.slices/` and generates production-ready event-sourced code. The system runs in a loop, processing slices marked as "Planned" status, and automatically handles:

- Command handlers and domain logic
- Event definitions and aggregates
- Read models and projections
- REST API endpoints
- Test infrastructure with TestContainers

## Quick Start

### Prerequisites

- Java 21+
- Claude CLI installed and configured
- Maven
- Docker (for TestContainers)

### Running Ralph

Execute the Ralph loop to start automated code generation:

```bash
./ralph.sh [max_iterations]
```

By default, runs up to 10 iterations. Ralph will:
1. Read slice definitions from `.slices/index.json`
2. Process slices with status "Planned"
3. Generate code following event modeling patterns
4. Continue until all tasks are complete or max iterations reached

Ralph automatically stops when it encounters `<promise>COMPLETE</promise>` in the output.

### Starting the Application

Run the application using the test starter class:

```bash
./mvnw test -Dtest=ApplicationStarter
```

Located in `src/test/kotlin/de/nebulit/ApplicationStarter.kt`, this class starts the complete environment including:
- PostgreSQL (via TestContainers)
- Kafka (via TestContainers, if needed)
- Spring Boot application

## Slice Definitions

### Structure

Slices are defined in `.slices/` with the following structure:

```
.slices/
├── index.json                    # Master index of all slices
└── TODO/                         # Context folder
    ├── createtodolist/
    │   └── slice.json           # Slice definition
    ├── todolists/
    │   └── slice.json
    ├── addtodoitem/
    │   └── slice.json
    └── todoitems/
        └── slice.json
```

### Slice Status

Only slices with `"status": "Planned"` in `index.json` are processed by Ralph.

### Slice Types

- **STATE_CHANGE**: Command → Event flow (e.g., Create Todo List → Todo List Created)
- **STATE_VIEW**: Event → Read Model → Screen (e.g., Todo List Created → todo lists view)

### Example Slice

```json
{
  "id": "3458764656615997335",
  "status": "Planned",
  "title": "slice: Create Todo List",
  "context": "TODO",
  "sliceType": "STATE_CHANGE",
  "commands": [...],
  "events": [...],
  "aggregates": ["todo list"]
}
```

## Project Structure

### Generated Code Organization

```
src/
├── main/kotlin/de/nebulit/
│   ├── events/              # Domain events
│   ├── domain/              # Aggregates and domain logic
│   ├── common/              # Shared interfaces
│   └── <sliceName>/         # Isolated slice packages
│       ├── command/         # Command handlers
│       ├── projection/      # Read model projections
│       └── rest/           # REST controllers
└── test/kotlin/de/nebulit/
    └── ApplicationStarter.kt
```

### Configuration

- `config.json` - Ralph configuration and slice mappings
- `pom.xml` - Maven dependencies (Spring Boot, Axon Framework, Kotlin)
- `prompt.md` - System prompt for Claude AI loop

## Sample Project

This repository includes a sample TODO list application with:

- **Create Todo List** - Command to create new todo lists
- **Todo Lists View** - Read model displaying all lists
- **Add Todo Item** - Command to add items to a list (with max 3 items rule)
- **Todo Items View** - Read model displaying all items

### Technology Stack

- **Language**: Kotlin 2.1.20
- **Framework**: Spring Boot 3.5.6
- **Event Sourcing**: Axon Framework 4.12.1
- **Build Tool**: Maven
- **Java Version**: 21

## Post-Generation Tasks

After initial code generation:

1. Review TODOs in generated code
2. Adjust code to match your coding standards
3. The generator makes assumptions (e.g., aggregate IDs are UUIDs)
4. Minor adjustments may be needed if you deviate from these assumptions

## How Ralph Works

1. **Initialization**: Reads slice definitions from `.slices/index.json`
2. **Filtering**: Selects slices with status "Planned"
3. **Generation**: Processes each slice through Claude AI
4. **Validation**: Compiles and tests generated code
5. **Iteration**: Repeats until completion or max iterations
6. **Archiving**: Archives previous runs when branch changes

### Progress Tracking

- `progress.txt` - Logs each iteration's output
- `archive/` - Stores previous runs organized by date and branch

### Error Handling

Ralph includes automatic retry logic for API rate limits. If Claude hits spending limits, Ralph waits 5 minutes before retrying.

## Commands Reference

### Generate Code
```bash
./ralph.sh              # Run with default 10 iterations
./ralph.sh 20           # Run with custom max iterations
```

### Build Project
```bash
./mvnw clean install
```

### Run Tests
```bash
./mvnw test
```

### Start Application
```bash
./mvnw test -Dtest=ApplicationStarter
```

## Development Guidelines

- Events are in the `events` package
- Aggregates are in the `domain` package
- Each slice has an isolated package `<sliceName>`
- Common interfaces are in the `common` package
- Follow event modeling patterns: Command → Event → Read Model → Screen

## Configuration

### Code Generation Settings

Located in `config.json`:
- `application`: Context name (default: "Context")
- `rootPackage`: Base package for generated code (default: "de.nebulit")
- `boardId`: Optional Miro board ID for visualization

## Troubleshooting

### Claude API Issues
If you encounter rate limits, Ralph will automatically retry after 5 minutes.

### Compilation Errors
Generated code may require minor adjustments based on your specific requirements and coding standards. Look for TODO comments in the generated code.

### TestContainers
Ensure Docker is running before starting the application with TestContainers.

## License

See LICENSE file for details.

## Support

For issues and questions, refer to the slice definitions in `.slices/` and the configuration in `config.json`.