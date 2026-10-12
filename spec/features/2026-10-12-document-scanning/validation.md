# Document Scanning — Validation

| Requirement | Validation |
| --- | --- |
| SCAN-1–2 | Connect supported scanners and verify discovery, selection, and requested DPI on Windows and Linux. |
| SCAN-3–4 | Scan, rotate, add, select, and remove pages; verify preview and state. |
| SCAN-5–6 | Verify one page offers PNG/JPEG/PDF and two pages offer only PDF. |
| SCAN-7 | Reopen exports and compare order, pixel orientation, page count, and dimensions. |
| SCAN-8 | Cancel and simulate device/write errors while checking responsiveness and retained pages. |
| SCAN-9 | Decline and accept replacement of an existing output. |
| SCAN-10 | Export the same page at each JPEG quality, verify readable images and increasing quality/file-size tendency; verify PNG is lossless. |

Automated model tests cover format eligibility and rotation arithmetic. Hardware acquisition remains a manual integration test.
