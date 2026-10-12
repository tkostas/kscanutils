# PDF Drag and Drop — Validation

| Requirement | Validation |
| --- | --- |
| DROP-1 | Drop one file and multiple files; verify the drop is received. |
| DROP-2 | Drop readable PDFs and verify entries are appended. |
| DROP-3 | Test non-PDF, missing, unreadable, corrupt, and encrypted inputs and verify file-specific errors. |
| DROP-4 | Drop valid and invalid files together and verify valid inputs remain accepted. |
| DROP-5 | Drop one PDF twice and verify two independently identified entries. |
| DROP-6 | Add same-named files from different folders and verify they are distinguishable. |
| DROP-7 | Compare source files before and after validation to verify they are unchanged. |
| DROP-8 | Fill the visible list, add more PDFs, and verify the newest entry scrolls into view. |

Run file-drop integration checks on Windows, macOS, and Linux because desktop drag-and-drop APIs vary by platform.
