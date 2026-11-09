# Architecture Decision Records

Decisions about how Munitorum is built, with the reasoning behind them. The format and the process for changing a decision are set out in [ADR 1](0001-record-architecture-decisions.md).

| ADR | Decision | Status | Date |
|---|---|---|---|
| [1](0001-record-architecture-decisions.md) | Record architecture decisions | Accepted | 2026-11-08 |
| [2](0002-dependency-selection.md) | Dependency selection | Accepted | 2026-11-08 |
| [3](0003-code-style.md) | Code style | Accepted | 2026-11-08 |
| [4](0004-ai-usage.md) | AI usage | Accepted | 2026-11-08 |

## Adding an ADR

1. Copy [template.md](template.md) to `NNNN-short-title.md`, using the next unused number.
2. Fill in the context, decision and consequences, and leave the status as **Proposed**.
3. Once the decision is made, set the status to **Accepted** and add a row to the table above.
4. If it replaces an earlier decision, change the old ADR's status to **Superseded by [NNNN](NNNN-short-title.md)** and update its row here. The old ADR's content stays as it was.
