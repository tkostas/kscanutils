# PDF Merge and Export — Plan

1. Select a permissively licensed JVM PDF library and record its license and preservation behavior.
2. Implement a merge service that consumes an ordered input list and writes to a temporary output.
3. Integrate the native save chooser and overwrite confirmation.
4. Publish progress without blocking the UI and support clear success/failure states.
5. Validate the temporary result before atomically promoting it to the selected destination where supported.
6. Add unit, integration, failure-path, performance, and cross-platform tests.

## Completion Criteria

All requirements in `requirements.md` pass the checks in `validation.md`; any newly identified PDF compatibility or data-loss risk is recorded under `spec/risks`.
