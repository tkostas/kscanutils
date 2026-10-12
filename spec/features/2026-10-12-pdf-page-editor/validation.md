# PDF Page Editor — Validation

| Requirement | Validation |
| --- | --- |
| EDIT-1 | Open Tools → PDF Utilities and verify **Edit PDF Pages** appears; verify the overview card opens the same workspace. |
| EDIT-2–3 | Open a valid multi-page PDF and verify one numbered preview per source page in source order; cancel the chooser and test invalid, corrupt, and protected PDFs. |
| EDIT-4 | Select non-adjacent pages, rotate left and right repeatedly, and verify only selected previews and exported pages rotate in 90-degree increments. |
| EDIT-5 | Remove selected pages and verify numbering updates; select all remaining pages and verify removal is rejected with an explanation. |
| EDIT-6 | Undo several rotations and removals and verify state is restored one edit at a time. |
| EDIT-7 | Reopen the output with an independent PDF reader and compare retained content, order, and orientation with the editor. |
| EDIT-8 | Choose an existing output and the source itself, then separately decline and accept replacement. |
| EDIT-9–10 | Exercise a large PDF and simulated read/write failures; verify UI responsiveness, retained state, temporary-file cleanup, and output validation. |
| EDIT-11 | Complete navigation, multi-selection, rotation, removal, undo, and save using only the keyboard and inspect accessibility semantics. |

Automated tests cover edit-state transformations, navigation, retained-page order, rotations, unchanged source hashes, and safe source replacement. Visual, keyboard, large-file, and complex-PDF checks remain manual.
