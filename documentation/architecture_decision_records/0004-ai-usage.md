# 4. AI usage

Date: 2026-11-08

## Status

Accepted

## Context

The point of this repository is that I understand, and can reason about, every decision in the codebase. That goes hand in hand with the goals in [ADR 2: Dependency selection](0002-dependency-selection.md): little magic, local control flow, and a repo small and plain enough to hold in my head. Code I didn't write is code I have to reverse-engineer later, whether it came from a library or from an agent.

## Decision

Code in this repository is written by hand. Coding agents are a tool for specific, well-bounded jobs, not the default way work gets done.

### Code

#### Written by hand

- Domain modelling, business logic and anything that encodes a design decision.
- Ports, and the decisions about what the domain needs from the outside world.
- Adapters. They only map data into the domain, but every external service has specifics that agents miss a lot.
- Concurrency, error handling and anything else where the details are the point.
- Tests that pin down behaviour. Deciding what "correct" means is a design decision.

#### Delegated to an agent

- **Boilerplate.** Code whose shape is already decided and where writing it teaches me nothing.
- **Mechanical refactors.** Changes that follow an established pattern across multiple files, such as renames, package moves, or applying a convention that already exists elsewhere in the repo.

The test is the same one used for dependencies, turned around: if the work involves a decision I need to understand, I write it. If it's repetitive work with a clear, known shape, an agent can do it.

#### Traceability

Every agent-authored change can be traced back to what the agent was asked to do.

- The prompt given to the agent, along with any follow-up prompts, is saved as a file under [`documentation/agent_prompts/`](../agent_prompts/).
- The commit containing the change references that prompt file in its message.
- I review agent-authored changes before committing them, the same way I would review a contributor's PR. A change I can't explain doesn't get committed.

### Documentation

Documentation is the exception. It is largely maintained by an agent, because agents are genuinely good at it and it's the part of the work most likely to fall behind otherwise.

- **I audit everything.** Agent-written docs are reviewed for accuracy against the code before they're committed. The agent drafts and I check.
- **Docs move with the code.** Documentation is updated iteratively alongside the application, with the aim of keeping the two in step. A change that makes the docs wrong isn't finished until the docs are fixed too.
- **Decisions stay mine.** An agent can write up the reasoning behind a decision, but the decision itself, and the reasoning, come from me. ADRs record what I decided, not what an agent suggested.

### Research and ad hoc analysis

Agents are also a research tool and a quick way to work with data: a better search engine, and a faster way to answer a one-off question than writing a throwaway script.

- **Research.** Looking up how a library, protocol or service behaves, comparing approaches, and finding prior art. Research feeds into decisions but doesn't make them, the same as with documentation.
- **Ad hoc data work.** Reshaping, summarising or querying data I need to look at, such as test output, logs or query results.

This output is for me to read and isn't part of the repository, so no prompt file is kept. If something produced this way does end up committed, it counts as agent-authored and the rules above apply.

### Summary

| Work | Who writes it | Record kept |
|---|---|---|
| Domain logic, design decisions, tests | Me | Normal commit history |
| Boilerplate, pattern-following refactors | Agent, reviewed by me | Prompt file, referenced from the commit |
| Documentation | Agent, audited by me | Normal commit history |
| Research, ad hoc data work | Agent, for my own use | None: nothing is committed |

## Consequences

- This keeps the history honest. Anyone reading `git log` can tell which changes were hand-written and which were generated, and for the generated ones, exactly what was asked for.
- Delegating work to an agent has a small, fixed cost: saving the prompt and referencing it from the commit.
