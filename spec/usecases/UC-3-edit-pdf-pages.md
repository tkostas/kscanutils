# UC-3: Edit PDF Pages

**Status:** Done (2026-10-12)

## Characteristic Information

- **Scope:** KScanUtils desktop application
- **Level:** User goal
- **Primary actor:** User
- **Stakeholders and interests:**
  - **User:** Wants to remove unwanted pages and correct page orientation in a PDF, then save the edited document locally without changing the source unexpectedly.
- **Preconditions:** KScanUtils is running and the user has a PDF document that the application can read.
- **Minimal guarantee:** The source PDF remains unchanged unless the user explicitly confirms replacing it, and no incomplete output is reported as saved.
- **Success guarantee:** A valid PDF containing the retained pages in their original order and selected orientations is saved to the location chosen by the user.
- **Trigger:** The user chooses **Edit PDF Pages** from the **PDF Utilities** submenu in the top navigation.

## Main Success Scenario

1. The user opens the **PDF Utilities** submenu in the top navigation.
2. The system displays the PDF utility commands, including **Edit PDF Pages**.
3. The user chooses **Edit PDF Pages**.
4. The system displays the Edit PDF Pages workspace and asks the user to select a PDF.
5. The user selects one PDF document.
6. The system validates and loads the document, then displays its pages in their current order with clear page numbers and previews.
7. The user selects one or more pages.
8. The user rotates the selected pages left or right and/or removes selected pages.
9. The system immediately updates the previews, page numbers, selection, and remaining page count to reflect each edit.
10. The user reviews the edited document and chooses **Save as PDF**.
11. The system asks the user to choose an output file name and location.
12. The user confirms the output file name and location.
13. The system writes a PDF containing the remaining pages in the displayed order and orientation.
14. The system confirms that the edited PDF was saved and shows its location.

## Extensions

- **1a. The submenu cannot be opened with a pointer:**
  1. The user opens and navigates the submenu with the keyboard.
- **5a. The user cancels document selection:**
  1. The system returns to the empty Edit PDF Pages workspace without changing any file.
- **5b. The selected file is not a PDF:**
  1. The system rejects the file, explains that a PDF is required, and allows another selection.
- **5c. The PDF is unreadable, corrupted, password-protected, or otherwise cannot be processed:**
  1. The system identifies the file and explains why it cannot be opened.
  2. The system allows the user to select another PDF.
- **7a. The user changes the page selection:**
  1. The system clearly identifies every selected page and applies the next page command only to that selection.
- **8a. The user rotates selected pages repeatedly:**
  1. The system applies each rotation in 90-degree increments and updates the previews.
- **8b. The user tries to remove every remaining page:**
  1. The system prevents the operation and explains that the output PDF must contain at least one page.
- **8c. The user wants to reverse an edit:**
  1. The system allows the user to undo page rotations and removals made during the current editing session.
- **10a. The document has no unsaved edits:**
  1. The system disables saving or explains that there are no changes to save.
- **11a. The user cancels output selection:**
  1. The system returns to the editor without creating a file and preserves all edits.
- **12a. The selected output is the source PDF or another existing file:**
  1. The system asks the user to confirm replacement.
  2. If the user declines, the system returns to output selection with all edits preserved.
- **13a. The output cannot be written:**
  1. The system reports the failure and its reason when available.
  2. The system preserves the loaded document and edits so the user can try again.

## Special Requirements

- The **Edit PDF Pages** command must appear under the **PDF Utilities** submenu in the top navigation and be operable by keyboard.
- Rotation and removal commands must operate only on the visibly selected pages.
- Page selection, rotation, and removal must be possible with a pointer and keyboard-accessible controls.
- The editor must preserve the order and content of retained pages except for the rotations explicitly requested by the user.
- The application must remain responsive and show progress while loading or saving large PDFs.
- PDF contents and edits must remain local to the user's computer.
- The workflow must work on every operating system supported by KScanUtils.

## Technology and Data Variations

- Input and output file choosers may follow native operating-system conventions.
- Pages may have different dimensions or orientations; rotation applies independently to each selected page.
- PDFs may contain metadata, bookmarks, forms, annotations, attachments, or digital signatures that cannot remain valid after editing.

## Frequency of Occurrence

On demand, potentially several times during one application session.

## Open Issues

- Define preservation behavior for document metadata, bookmarks, forms, annotations, attachments, and links after pages are removed.
- Define how the application warns users that editing invalidates digital signatures.
- Decide whether a later feature will support page reordering in this workspace.
- Define the maximum supported page count and file size through performance testing.
