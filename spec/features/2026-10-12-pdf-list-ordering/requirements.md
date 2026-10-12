# PDF List Ordering — Requirements

## Purpose

Let users define the exact order in which accepted PDFs will be merged.

## Requirements

- **ORDER-1:** The list preserves the order in which accepted PDFs are added.
- **ORDER-2:** The user can click and drag an entry to a different position.
- **ORDER-3:** Each entry exposes accessibility actions for moving it up or down.
- **ORDER-4:** The visible list updates immediately and unambiguously after a reorder.
- **ORDER-5:** The user can remove an entry without changing the relative order of remaining entries.
- **ORDER-6:** Duplicate entries can be moved and removed independently.
- **ORDER-7:** The file-list region uses available vertical space, shows a scrollbar on overflow, and does not displace the merge controls.

## Use-Case Traceability

Supports UC-1 steps 9-10 and extension 9a.

## Out of Scope

- Reordering pages within an individual PDF.
- Persisting the list between application sessions.
