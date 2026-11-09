# Agent prompts

Prompts given to coding agents, one file per agent-authored change, as required by [ADR 4: AI usage](../architecture_decision_records/0004-ai-usage.md).

## Conventions

- **File name:** `YYYY-MM-DD-short-description.md`, for example `2026-11-08-rename-litany-package.md`.
- **Contents:** the prompt exactly as given, then each follow-up prompt in order. Use [the layout below](#layout).
- **Commit:** the commit containing the change references the prompt file in its message, for example:

  ```text
  Rename litany package to match the domain glossary

  Prompt: documentation/agent_prompts/2026-11-08-rename-litany-package.md
  ```

## Layout

```markdown
# Short description of the change

- Date: YYYY-MM-DD
- Agent: tool and model used

## Prompt

The prompt as given.

## Follow-ups

1. Each follow-up prompt, in order.
```
