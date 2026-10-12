# UC-1: Merge PDF Files

**Status:** Done (2026-10-12)

## Characteristic Information

- **Scope:** KScanUtils desktop application
- **Level:** User goal
- **Primary actor:** User
- **Stakeholders and interests:**
  - **User:** Wants to combine PDF files in a chosen order into one valid PDF without changing the source files.
- **Preconditions:** KScanUtils is installed and can be started.
- **Minimal guarantee:** The source files remain unchanged, and no incomplete output file is presented as a successful merge.
- **Success guarantee:** One PDF containing every selected source PDF, in the order shown in the UI, is saved to the location chosen by the user.
- **Trigger:** The user opens KScanUtils to combine two or more PDF files.

## Main Success Scenario

1. The user opens KScanUtils.
2. The system displays the home screen containing cards for the available utility categories, including **PDF Utilities**.
3. The user opens the **PDF Utilities** card.
4. The system displays the PDF tools screen, including **Join PDFs**.
5. The user opens **Join PDFs**.
6. The system displays the Join PDFs screen with an area for dropping PDF files.
7. The user drags two or more PDF files from the operating system and drops them onto the Join PDFs area.
8. The system validates the files and displays them in a list.
9. The user arranges the files into the desired order in the UI.
10. The system displays the current order clearly.
11. The user presses **Merge**.
12. The system asks the user to choose an output file name and location.
13. The user confirms the output file name and location.
14. The system merges the complete contents of the listed PDFs in the displayed order.
15. The system saves the merged PDF and confirms that the merge succeeded.

## Extensions

- **1a. The application cannot start:**
  1. The system reports the startup failure when possible.
- **7a. One or more dropped items are not PDF files:**
  1. The system rejects the unsupported items and identifies them to the user.
  2. The system adds any valid PDF files from the same drop.
- **7b. A dropped PDF is unreadable, corrupted, password-protected, or otherwise cannot be processed:**
  1. The system does not add that file to the merge list.
  2. The system identifies the file and explains why it cannot be used.
- **7c. A PDF already in the list is dropped again:**
  1. The system adds it as another list entry, allowing its contents to appear more than once in the result.
- **9a. The user removes a file from the list:**
  1. The system removes the selected list entry and updates the displayed order.
- **11a. Fewer than two valid PDFs are listed:**
  1. The system does not start the merge and tells the user to add at least two PDF files.
- **12a. The user cancels output selection:**
  1. The system returns to the Join PDFs screen without creating an output file or changing the list.
- **13a. The selected output file already exists:**
  1. The system asks the user to confirm replacement.
  2. If the user declines, the system returns to output selection.
- **14a. A source file becomes unavailable or unreadable before merging finishes:**
  1. The system stops the merge, identifies the affected file, and reports that no merged PDF was created.
- **15a. The output cannot be written:**
  1. The system reports the failure and its reason when available.
  2. The system preserves the ordered list so the user can try again.

## Special Requirements

- The merge must preserve all pages from each source PDF and preserve page order within each source file.
- Reordering must be possible using a pointer or keyboard-accessible controls.
- The UI must distinguish files with identical names, for example by showing their paths.
- The application must remain responsive and show progress while merging large files.
- The merge must work on every operating system supported by KScanUtils.

## Technology and Data Variations

- Drag-and-drop behavior and the output file chooser may use native operating-system conventions.
- PDF files may have different page sizes, orientations, metadata, bookmarks, forms, or annotations; merging must not intentionally alter their page content.

## Frequency of Occurrence

On demand, potentially several times during one application session.

## Open Issues

- Define whether document-level metadata, bookmarks, forms, attachments, and digital signatures must be preserved.
- Define the maximum supported input count and total input size, if any.
