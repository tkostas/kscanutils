# R-2: Scanner Backend Availability

## Risk

Scanner discovery and acquisition APIs are operating-system specific. WIA devices may expose different capabilities, Linux requires SANE and `scanimage`, and the current implementation has no native macOS Image Capture adapter.

## Impact

A connected scanner may not appear, a requested DPI may be rejected by its driver, or scanning may be unavailable on a supported application platform.

## Mitigations

- Keep acquisition behind a platform boundary so backends can be replaced independently.
- Preserve already scanned pages when discovery or acquisition fails.
- Show backend error details instead of silently failing.
- Test against representative flatbed, feeder, WIA, and SANE devices.
- Add and validate a native macOS adapter before declaring UC-2 cross-platform complete.

## Status

Open. Windows WIA and Linux SANE adapters are implemented; macOS acquisition remains outstanding.
