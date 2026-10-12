# PDF Drag and Drop — Requirements

## Purpose

Build a validated list of source PDFs by dropping files onto the Join PDFs workspace.

## Requirements

- **DROP-1:** The drop area accepts one or more files from the operating system.
- **DROP-2:** Valid, readable PDF files are appended to the displayed input list.
- **DROP-3:** Unsupported, unreadable, corrupted, and password-protected files are rejected with a file-specific explanation.
- **DROP-4:** Valid files from a mixed valid/invalid drop are still added.
- **DROP-5:** Dropping the same PDF more than once creates a separate entry each time.
- **DROP-6:** Entries show enough path information to distinguish identically named files.
- **DROP-7:** Adding or validating files does not modify the source files.
- **DROP-8:** Newly accepted files are brought into view when the input list overflows its visible area.

## Use-Case Traceability

Supports UC-1 steps 7-8 and extensions 7a-7c.

## Out of Scope

- Reordering inputs.
- Merging PDF content.
