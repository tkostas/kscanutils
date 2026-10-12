# UC-2: Scan a Document

**Status:** Done (2026-10-12)

## Characteristic Information

- **Scope:** KScanUtils desktop application
- **Level:** User goal
- **Primary actor:** User
- **Stakeholders and interests:**
  - **User:** Wants to capture one or more paper pages, correct their orientation, and save them locally without uploading their contents.
- **Preconditions:** KScanUtils is running and a scanner supported by the operating system is connected and available.
- **Minimal guarantee:** No output is reported as saved unless it was written successfully; an existing file is not replaced without confirmation.
- **Success guarantee:** The scanned pages are saved in their displayed order and orientation as an image for a single page or as a PDF for one or more pages.
- **Trigger:** The user chooses **Scan Documents** from the home screen.

## Main Success Scenario

1. The user opens KScanUtils and chooses **Scan Documents**.
2. The system discovers available scanners and displays them.
3. The user selects a scanner and a DPI setting.
4. The user presses **Preview**.
5. The system scans and displays the first page.
6. The user rotates the page if needed.
7. The user optionally presses **Add page** and repeats scanning and rotation for further pages.
8. The system offers image and PDF formats for one page, or PDF only for multiple pages.
9. The user chooses the format and presses **Save**.
10. The system asks for an output name and location.
11. The system writes the output and confirms its location.

## Extensions

- **2a. No scanner is available:** The system explains that no scanner was found and leaves scanning disabled.
- **4a. Acquisition fails or is cancelled:** The system reports the failure, preserves existing pages, and allows retry.
- **6a. The user rotates in either direction:** The preview and exported page use the selected orientation.
- **7a. The user removes a page:** The system updates the page count and available formats.
- **10a. Output selection is cancelled:** No file is created and all scanned pages remain available.
- **10b. The output already exists:** The system requests confirmation before replacement.
- **11a. Export fails:** The system reports the reason and preserves the scanned pages for retry.

## Special Requirements

- Scanning and export must not block the UI.
- Scanned content remains local to the computer.
- Supported DPI choices are 150, 200, 300, and 600.
- Page order and orientation must be preserved in a multi-page PDF.
- Windows uses WIA and Linux uses an installed SANE `scanimage` backend.

## Open Issues

- A native macOS Image Capture adapter is not yet implemented.
- Automatic document feeder, color-mode, crop, and duplex controls are future enhancements.
