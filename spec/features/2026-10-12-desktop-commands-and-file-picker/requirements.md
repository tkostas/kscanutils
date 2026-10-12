# Desktop Commands and File Picker — Requirements

## Purpose

Expose application-wide commands through desktop conventions and provide an alternative to drag-and-drop file input.

## Requirements

- **CMD-1:** The desktop window has visually integrated **File**, **Tools**, and **Help** dropdown menus.
- **CMD-2:** File contains only the Exit command.
- **CMD-3:** Tools provides direct navigation to Home and **Scan Documents**, plus a true **PDF Utilities** submenu containing **Overview**, **Join PDFs**, and **Edit PDF Pages**.
- **CMD-4:** Help provides an About dialog containing application information and open-source license details separated by a divider.
- **CMD-5:** Add PDFs opens a native multi-file chooser filtered for PDF files.
- **CMD-6:** The Join PDFs workspace provides a visible Add PDFs button in addition to drag-and-drop.
- **CMD-7:** Top-level menus and nested submenus open on pointer hover while remaining operable by click.
- **CMD-8:** Menus use compact spacing and nested submenus appear without a visual gap.
- **CMD-9:** Activating the KScanUtils logo or name in the application header navigates to Home.

## Use-Case Traceability

Enhances UC-1 by providing alternative access paths for steps 3-7 and 11-13, UC-2 by providing direct access to document scanning, and UC-3 by exposing the page editor under PDF Utilities.

## Out of Scope

- Settings, until configurable preferences are defined.
- Edit commands that do not apply to the current workflow.
- File-menu commands already presented directly in the Join PDFs workspace.
