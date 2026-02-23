# Ralph Agent Instructions

You are an autonomous coding agent working on a software project.

the code is pre-generated.
delete all "/debug/*" endpoints if available, they are not needed.

After having read your slice definition, load the appropriate skills to adjust the slices.
The generated code does not reflect the target state, always check against the skill.

## Your Task

1. Read the PRD at `.slices/index.json` (in the same directory as this file). Every item is a task.
2. Read the progress log at `progress.txt` (check Codebase Patterns section first)
3. Make sure you are on the right branch "feature/<slicename>", if unsure, start from main.
5. Pick the **highest priority** slice where status is "in progress". If no status is in progress, pick the highest
   priority task where status is not "Done" or "Informational". Set that slice to status "in progress"
6. Pick the slice definition from .slices
6. Implement that single slice, make use of the skills in the skills directory, but also your previsously collected
   knowledge.
7. A slice is only 'Done' if business logic is implemented, APIs are implemented, test scenarios are implemented and it
   fulfills the slice.json
8. Run quality checks ( mvn spotless:apply, organize imports, mvn test -q )
10. If checks pass, commit ALL changes with message: `feat: [Slice Name]` and merge back to main as FF merge ( update
    first )
11. Update the PRD to set `status: Done` for the completed story. If a story is completed, start a new iteration.
12. Append your progress to `progress.txt` after each iteration
13. Append your learnings to AGENTS.md in a compressed form, reusable for future iterations.

## Progress Report Format

APPEND to progress.txt (never replace, always append):

```
## [Date/Time] - [Slice]

- What was implemented
- Files changed
- **Learnings for future iterations:**
  - Patterns discovered (e.g., "this codebase uses X for Y")
  - Gotchas encountered (e.g., "don't forget to update Z when changing W")
  - Useful context (e.g., "the evaluation panel is in component X")
---
```

The learnings section is critical - it helps future iterations avoid repeating mistakes and understand the codebase
better.

## Consolidate Patterns

If you discover a **reusable pattern** that future iterations should know, add it to the `## Codebase Patterns` section
at the TOP of progress.txt (create it if it doesn't exist). This section should consolidate the most important
learnings:

```
## Codebase Patterns
- Example: Use `sql<number>` template for aggregations
- Example: Always use `IF NOT EXISTS` for migrations
- Example: Export types from actions.ts for UI components
```

Only add patterns that are **general and reusable**, not story-specific details.

## Update AGENTS.md Files

Before committing, check if any edited files have learnings worth preserving in nearby AGENTS.md files:

1. **Identify directories with edited files** - Look at which directories you modified
2. **Check for existing AGENTS.md** - Look for AGENTS.md in those directories or parent directories
3. **Add valuable learnings** - If you discovered something future developers/agents should know:
    - API patterns or conventions specific to that module
    - Gotchas or non-obvious requirements
    - Dependencies between files
    - Testing approaches for that area
    - Configuration or environment requirements

**Examples of good AGENTS.md additions:**

- "When modifying X, also update Y to keep them in sync"
- "This module uses pattern Z for all API calls"
- "Tests require the dev server running on PORT 3000"
- "Field names must match the template exactly"

**Do NOT add:**

- Story-specific implementation details
- Temporary debugging notes
- Information already in progress.txt

Only update AGENTS.md if you have **genuinely reusable knowledge** that would help future work in that directory.

## Quality Requirements

- ALL commits must pass your project's quality checks (typecheck, lint, test)
- Do NOT commit broken code
- Keep changes focused and minimal
- Follow existing code patterns

## Skills

Use the provided skills in the skills folder as guidance.
Update skill definitions if you find an improvement you can make.

## Specifications

For every specification added to the Slice, you need to implement one use executable Specification in Code.

A Slice is not complete if specifications are missing or can´t be executed.

## Stop Condition

After completing a user story, check if ALL slices have `status: Done`.

If ALL stories are complete and passing, reply with:
<promise>COMPLETE</promise>

If there are still stories with `status: Planned`, end your response normally (another iteration will pick up the next
story).

## Important

- Work on ONE story per iteration
- Commit frequently
- Keep CI green
- Read the Codebase Patterns section in progress.txt before starting

## When an iteration completes

Use all the key learnings from the project.txt and update the Agends.md file with those learning.
