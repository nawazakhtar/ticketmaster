---
name: pr-review
description: Review code changes against this project's rules (.claude/RULES.md) for correctness, security, and convention drift. Works locally on the working tree/current branch, or remotely on a GitHub pull request. Use when the user asks to review a PR, review the diff, review my changes, or "/pr-review".
---

# PR review

Review a set of changes against `.claude/RULES.md` (project-specific
conventions for this codebase) plus general correctness, security, and
maintainability. Works in two modes — decide which from `$ARGUMENTS`:

| Argument looks like | Mode | Diff source |
|---|---|---|
| empty | local | working tree changes if any exist, else current branch vs base |
| a branch/ref name (e.g. `main`, `develop`) | local | current branch vs that ref |
| a number (e.g. `42`) or a GitHub PR URL | remote | `gh pr diff` |

## 1. Determine scope and get the diff

**Local mode:**
1. `git status --porcelain` — if non-empty, that's the review scope:
   `git diff HEAD` (tracked changes) plus `git status --porcelain` for
   untracked new files (read those in full, they won't show in `git diff`).
2. If the working tree is clean, diff the current branch against its base:
   `git merge-base main HEAD` then `git diff <merge-base>...HEAD`. Use the
   ref the user passed instead of `main` if they gave one. Also run
   `git log <merge-base>..HEAD --oneline` so you have commit context.

**Remote mode:**
1. Requires `gh` (GitHub CLI), authenticated. If `gh` isn't available or
   `gh auth status` fails, say so and stop — don't try to fake it via raw git.
2. `gh pr view <arg> --json title,body,baseRefName,headRefName,number` for
   context, then `gh pr diff <arg>` for the actual diff.

In both modes, if the diff is large, work file-by-file rather than trying to
hold everything in view at once.

## 2. Read the rules

Read `.claude/RULES.md`. If it doesn't exist, skip project-rule checks and
review on general correctness/security/maintainability grounds only — say so
in your summary rather than silently inventing rules.

## 3. Review

For each changed file that contains logic (skip pure formatting/generated
files):
- Read enough surrounding context (not just the diff hunk) to judge behavior
  correctly — open the full file when a change touches a method's control
  flow, a transaction boundary, or a lock's acquire/release pair.
- Check against every applicable rule in `.claude/RULES.md`.
- Check general correctness: logic bugs, null/empty handling, off-by-one,
  incorrect exception types for the situation, resource leaks.
- Check security: injection, auth/authz gaps introduced fresh (vs. the known
  pre-existing ones already called out in RULES.md), secrets or sensitive
  data (payment details, tokens) in logs or error messages.
- Skip anything speculative — only report a finding if you can point to the
  concrete input/state that breaks and what the wrong outcome is.

## 4. Report

Call `ReportFindings` with the verified findings, most severe first. Use
`category` values like `correctness`, `security`, `concurrency`,
`convention-drift`, `test-coverage`. Empty array is a valid, good outcome —
don't invent findings to have something to report.

## 5. Remote mode only — posting back to GitHub

Reviewing is read-only by default. If the user asks you to actually post the
review to the PR (not just show it to them), confirm the target PR and findings summary with them first, then use
`gh pr review <num> --comment -b "<summary>"` (or `--request-changes` /
`--approve` if they explicitly want that verdict). Never post automatically —
this is a visible, hard-to-fully-undo action on shared state.
