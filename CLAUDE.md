# AGENTS.md

## Project Overview

## Working Rules

- Keep changes scoped to the current request.
- Prefer existing document structure before introducing a new structure.
- Do not create new skills, hooks, plugins, or conventions unless requested.

## Command Rules

- Do not run destructive commands.
- Do not run deployment, release, migration, or credential-related commands unless explicitly requested.
- Do not install, update, remove, or replace dependencies without explicit user confirmation.
- If a command fails due to missing execute permission or insufficient permission, do not bypass it with an alternate invocation unless the user explicitly asks you to.

## Dependency Rules

- Do not add, upgrade, remove, or replace production, test, build, plugin, or tooling dependencies without explicit user confirmation.

## Lint And Format Rules

- Use Spotless as the default formatter when Java formatting is needed.
- Use Gradle Spotless tasks when Spotless is configured.
- Do not introduce or modify Spotless configuration without explicit user confirmation.
- Do not introduce any other lint or format tool without explicit user confirmation.

## Validation Rules

- Run only validation directly relevant to the changed files.
- If validation is delegated to another agent or skill, state that instead of inventing commands.
- Report validation that was not run.

## Convention Rules

- Follow existing naming and document structure.
- Keep detailed naming and style rules in a separate convention document.

## Response Rules

- State the conclusion first.
- Include only necessary rationale.
- Ask one specific question when uncertainty affects the result.
