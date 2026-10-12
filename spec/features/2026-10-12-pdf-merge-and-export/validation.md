# PDF Merge and Export — Validation

| Requirement | Validation |
| --- | --- |
| MERGE-1 | Verify Merge is unavailable for zero/one input and available for two or more. |
| MERGE-2 | Open the chooser and save using a new PDF path. |
| MERGE-3 | Cancel the chooser and verify no output and no list changes. |
| MERGE-4 | Test declining and accepting replacement of an existing file. |
| MERGE-5 | Merge PDFs with distinct pages, sizes, and orientations; inspect page count, content, and exact order. |
| MERGE-6 | Hash source files before and after successful and failed merges. |
| MERGE-7 | Merge a large fixture while verifying progress updates and UI responsiveness. |
| MERGE-8 | Reopen the output with an independent PDF reader/parser before showing success. |
| MERGE-9 | Simulate missing inputs, read failures, insufficient space, permission errors, and destination loss; verify cleanup, messaging, and retry state. |
| MERGE-10 | Run the merge/export suite or recorded smoke test on each supported OS. |

Also test empty PDFs, duplicate input entries, mixed page dimensions, and filenames containing Unicode characters.
