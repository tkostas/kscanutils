# PDF Merge and Export — Requirements

## Purpose

Merge the ordered inputs into one PDF and save it to a user-selected location.

## Requirements

- **MERGE-1:** Merge is available only when at least two valid PDF entries are listed.
- **MERGE-2:** Activating Merge opens a native output chooser for a PDF file name and location.
- **MERGE-3:** Cancelling output selection creates no output and preserves the ordered input list.
- **MERGE-4:** Existing output files are replaced only after explicit user confirmation.
- **MERGE-5:** The output contains every page from every input in displayed file order and original within-file page order.
- **MERGE-6:** Source files are never modified.
- **MERGE-7:** The UI remains responsive and displays progress during long merges.
- **MERGE-8:** Success is confirmed only after a complete, readable output PDF has been saved.
- **MERGE-9:** Read or write failures identify the affected file/reason when available, do not present an incomplete file as successful, and preserve the input list for retry.
- **MERGE-10:** Merge and export work on Windows, macOS, and Linux.

## Use-Case Traceability

Supports UC-1 steps 11-15 and extensions 11a-15a.

## Out of Scope

- Editing pages or PDF content.
- Guaranteed preservation of document-level metadata, bookmarks, forms, attachments, or digital signatures until their policy is specified.
