# R-1: PDF Structure Preservation

## Risk

Although page content and order are preserved by merge and page-edit operations, document-level structures can behave differently when PDFs are combined or pages are removed. Examples include bookmarks, forms with duplicate field names, attachments, portfolios, metadata, accessibility structure, and digital signatures. Existing digital signatures cannot remain valid after a document is rewritten.

## Impact

- Information outside visible page content may be lost, renamed, or combined unexpectedly.
- Users could assume a signed output retains the validity of signatures from its source files.

## Current Mitigations

- Password-protected inputs are rejected.
- Sources are never modified without explicit replacement confirmation.
- Output is written to a temporary file and reopened with an independent PDFBox load before success is reported.
- The feature requirements explicitly limit the current preservation guarantee to pages and their order.
- Integration tests verify page count, page order, rotations, unchanged source hashes, and safe replacement of a source after editing.

## Further Work

- Define a product policy for bookmarks, forms, attachments, metadata, accessibility structure, and signatures.
- Add representative fixtures and preservation tests for every structure the policy promises to retain.
- Warn users about invalidated signatures if signed-input detection is added.

## Status

Open until the preservation policy and associated compatibility suite are complete.
