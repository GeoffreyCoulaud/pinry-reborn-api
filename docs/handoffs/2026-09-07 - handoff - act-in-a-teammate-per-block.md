# Handoff: Act runs in a teammate per block

Date: 2026-09-07
Branch: `docs/act-in-a-teammate-per-block`, one block, one pull request
Decision: `docs/adr/0023-act-in-a-teammate-per-block.md`
Tier: Spec, one block, written inline by the lead, the regime in force being the one on `main`. The
ADR is the specification and its review ran on it; the holistic review was waived by the operator
for this documents-only block, a stated exception.

## Current state

`./gradlew gate` green at the block's tip. The pull request waits for the human's reading. The
regime the ADR describes is in force from its merge, and no lot has run under it yet.

## What was built

- **ADR 0023**: in tier Spec, each block is implemented by a named background agent, one per block,
  spawned by the lead after the previous merge and stopped by name at this block's merge. The lead
  keeps the lot's thread: Discuss, Spec, both reviews, the relay of the operator's answers. Tier 2
  stops the teammate, which asks the operator directly through `main`; the answer comes back through
  the lead, verbatim. The pull request's body is the block's report in five parts, and the last code
  block writes the handoff from the lot's pull requests.
- **`agents/workflow.md`**: the two roles, phases 2 to 6 rewritten for them, and the tier table
  restored (commit `b3a8671a` had cut it while `AGENTS.md` kept pointing at it).
- **The two mandates** say the lead dispatches them; **ADR 0018 and 0020** carry the amendment in
  their status lines.

## The measurements behind it

From the P2 lot's transcript, main loop only: 578 calls, a context of 451 k tokens at the median and
694 k at the maximum, one `/compact` after block 5, and 23 k to 125 k tokens added per block. Twenty
operator turns from the spec's approval to the end, the approval included, fourteen of them merge
notices for twelve merges. The ADR's context section carries the table and the method.

## Pitfalls, in the order they cost time

1. **A throwaway script is written with the Write tool, into the scratch directory, then run.** The
   evidence guard blocks `python3` reading a script from stdin, and the auto-mode classifier blocked
   a heredoc writing a `.py` file even into the scratch directory. Two attempts were lost to that.
2. **A named agent's stop notice can arrive late.** The first probe's three notices reached the lead
   in one batch, one to seventy minutes after they were produced; its answers were read from its
   transcript under this session's `subagents/` directory in the meantime. `ListAgents` showed it
   idle; `TaskOutput` does not know a named agent by its name. A teammate's question travels by
   `SendMessage` to `main`, which woke an idle lead within seconds.
3. **The tier definitions were gone.** `agents/workflow.md` said "Tier Direct skips it" and defined
   no tier; the definition had left with the trim commit. Check that a word a living document leans
   on is still defined in it after a trim.
4. **A transcript line is not a model call.** A streamed response is stored as one line per content
   block, each repeating the response's usage; the first count summed 1332 lines where 578 responses
   had been made, and the specification review caught it. Count distinct `message.id`.
5. **A background command does not wake an idle teammate.** The second probe's `sleep 120` in the
   background ended and the probe stayed idle until messaged. A teammate runs the gate and the wait
   for continuous integration in the foreground.

## Not validated

- **How a background agent's permission prompt reaches the operator.** Both probes used tools that
  need no permission.
- **The fresh-start cost per block** (reading the pointers, the spec and the block's files), and the
  lead's context at the end of a lot under the regime. Both are the next tier Spec lot's to measure,
  against the baseline in the ADR.

## Tier-2 questions asked

One: restore the tier table in this block, or fix the pointer in `AGENTS.md`, or file it. Answer:
restore it here.

## Next step

The first tier Spec lot under the regime. Its handoff measures the lead's context from the
transcript against the P2 baseline and the failure value the ADR sets, records how a permission
prompt behaved, and says whether the fixed report shape survived contact with a real block.
