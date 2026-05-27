# Changelogs

This directory contains user-facing release changelogs for each version of the Oppia Android app. These files are the source of truth for Play Store release notes and are used by the deployment scripts during release.

## Naming Convention

| File | Used for |
|---|---|
| `<major.minor>.md` | Default changelog applied to all flavors (e.g., `0.17.md`) |
| `<major.minor>_<flavor>.md` | Flavor-specific override (e.g., `0.17_beta.md`) |

Supported flavors: `alpha`, `beta`, `ga`

## Lookup Order

When deploying a release, `UploadBinaryToPlayConsole` and `UploadChangelogToPlayConsole` resolve the changelog as follows:

1. Check for `config/changelogs/<version>_<flavor>.md`
2. Fall back to `config/changelogs/<version>.md`
3. **Fail** if neither exists — a changelog is always required for deployment

## Format

Each changelog file must contain **2–3 user-facing sentences** describing what's new. Write for end users, not developers — no internal implementation details, no PR numbers, no technical jargon.

Example Changelog:

```bash
This release improves lesson loading speed and fixes a crash that occurred when switching profiles. It also adds support for audio playback on older devices.
```

## Adding a New Changelog

Changelogs describe **what changed in the previous version** that is being released. They are generated automatically when a PR is merged to `develop` (via the `generate_changelog.yml` workflow), which kicks off a script to draft a changelog for the version that was just completed. The LLM-generated draft is proposed as a PR for human review before it is merged.

To manually create or edit a changelog:
1. Create/edit `config/changelogs/<version>.md`
2. Keep it to 2–3 sentences, user-facing
3. Open a PR targeting `develop`

Corrections to an already-deployed changelog can be pushed directly — the `deploy_updated_changelog.yml` workflow detects the diff and syncs it to Play Console automatically.

## Flavor-Specific Overrides

If a flavor needs a different changelog (e.g., the beta release includes experimental features not in GA):

1. Create `config/changelogs/<version>_beta.md` alongside the default `<version>.md`
2. The deploy script will pick up the flavor-specific file automatically

## Play Store Character Limit

Google Play enforces a **500-character limit** per language for release notes. Keep changelogs within this limit. The deployment script will reject changelogs that exceed 500 characters.
