# 0022. A lost lease is an exception

Status: Accepted
Date: 2026-09-07
Specification: `docs/specs/2026-09-05-p2-debt-elimination.md`, section 3 (D3) and section 4.7.
Related: `docs/adr/0017-promote-inside-the-publishing-transaction.md` (why correctness never
depended on this), `docs/adr/0016-fence-by-compare-and-set.md` (the guards every settle carries),
`docs/specs/2026-08-27-export-build-completion.md` section 8 (the two items this closes).

## Context

`TaskQueueInterface.renewLease` answers a `Boolean` whose KDoc says the caller "has lost the task
and must stop working on it". `TaskProcessor` built the heartbeat as `() -> Unit` and dropped the
answer, so no handler could read it. A handler that lost its lease kept spending disk and CPU on work
it could no longer publish, every settle being fenced on the lease it lost. The import half is the
one that costs: its runner writes into account data as it walks, and an evicted attempt kept writing
beside the attempt that replaced it until its own fenced `advance` refused, one pin later in the pin
walk and one whole entry later in the tag and board walks. Correctness never depended on the fix,
ADR 0017 having put the promote inside the publishing fence; the cost did.

The export-build-completion lot filed the item rather than fixing it, and filed beside it that
`EbeanTaskQueue.claimNext` kills a task whose handler may still hold a live lease, which the export
sweep's `PT6H` grace only made improbable. Both are one defect seen from two sides: a handler that
cannot learn it lost, and a queue that cannot assume it stopped.

## Decision

1. **The heartbeat throws.** `TaskProcessor` builds `renewLease` so that a `false` from the queue
   throws `TaskLeaseLostException(taskId)`, a `RuntimeException` under `usecases/tasks/exceptions`
   beside `PermanentTaskException`. `TaskContext.renewLease` keeps its `() -> Unit` type, and its
   KDoc says it throws. Rejected (D3): `() -> Boolean`, which breaks both handlers at compile time
   and invites the swallowing lambda the export specification warned of.

2. **A fourth outcome settles nothing.** `runHandler` catches the exception ahead of `Exception`
   into `Abandoned`, on which `execute` logs at WARN and returns: the lease is another attempt's or
   nobody's, and every `mark*` would be refused by its own guard. `markCancelledIfRequested` is
   skipped for the same reason.

3. **Every handler net rethrows it before its own arm.** `UserDataExportBuilder.stageOrFail` and
   `UserDataImportRunner.replay` both catch `Throwable` and mark the row `FAILED` on the last
   attempt; each now catches `TaskLeaseLostException` first and rethrows it, so an evicted attempt
   never writes `FAILED` over a row whose winner is still building. The catch arm that only rethrows
   is what detekt's `RethrowCaughtException` reports and its alternative, an `is` check inside the
   general arm, is what `InstanceOfCheckForException` reports; the rethrow arm is suppressed with
   this reason at both sites. The import's per-line nets sit inside the heartbeat calls, not around
   them, so the exception reaches `replay` without meeting them; `replay`'s `finally` still runs
   `releaseArchive`, which re-reads the row and deletes nothing while it is `RUNNING`.

4. **`claimNext` and the grace do not change.** A handler that lost its lease now stops at its next
   heartbeat, so the task `claimNext` kills is at most one heartbeat gap behind: one image stream for
   the export, `imports.lease_renewal_lines` lines for the import. `claimNext`'s comment names that
   window; `ReapUserDataExports.failInterruptedBuilds`'s KDoc no longer says the kill is "without
   regard for a handler still running". The `PT6H` grace stays: it is anchored on the staged file's
   age, not on the lease.

## Consequences

- **A handler's nets must know the exception.** A third handler with a `Throwable` net that marks
  its row on failure must rethrow `TaskLeaseLostException` first, as the two existing ones do; the
  rule lives here and in `TaskContext`'s KDoc, not in a detekt rule.
- **An abandoned attempt leaves its row as it was.** The export row stays `PENDING`, the import row
  `RUNNING` under its run token, for the attempt that holds the lease to finish or for the sweeps
  to settle. Nothing writes on their behalf.
- **The queue's `false` is finally read**, and its KDoc's "must stop" is enforced rather than
  hoped for. The reaper's kill is now bounded by the heartbeat interval, which is what makes the two
  backlog items one exit.
