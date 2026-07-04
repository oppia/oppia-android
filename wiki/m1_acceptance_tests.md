# M1 Acceptance Tests

This document records the manual critical user journey validations for Milestone 1 (User Stories 1 to 4, 7, 9).
These tests verify that the automated release infrastructure behaves as expected when executed against the live Oppia environment.

### User Story 1: Coordinator starts a new release by cutting a branch and bumping the version
**Setup:** `latest-alpha` tag exists at commit `xyz789`. The current `MINOR_VERSION` in `version.bzl` is `17`.

| Step | Action | Expected Outcome | Actual Outcome / Status |
| :--- | :--- | :--- | :--- |
| 1 | Cut release branch: `git checkout -b release-0.17 latest-alpha`. | Branch `release-0.17` is created at commit `xyz789`. | ⬜️ Pending |
| 2 | Send a PR on `develop` bumping `MINOR_VERSION` to `18` in `version.bzl`. | PR is created with only the version file change. | ⬜️ Pending |
| 3 | Merge the version bump PR. | `generate_changelog.yml` is automatically triggered. Changelog is generated for version 0.17. | ⬜️ Pending |
| 4 | Run `build_and_sign.yml` for `beta` and `ga` with source ref `release-0.17`. | Both builds complete; signed AABs uploaded to their respective GCS buckets. | ⬜️ Pending |
| 5 | Run `deploy_to_firebase.yml` for `beta` and `ga`. | QA team receives both builds via Firebase App Distribution. | ⬜️ Pending |
| 6 | Attempt `git checkout -b release-0.17` from a non-existent tag. | Command fails with a clear error. | ⬜️ Pending |

### User Story 2: Build and deploy a release to Firebase for QA testing
**Setup:** A signed beta AAB exists in the GCS archive from the build step above. Firebase App Distribution is configured with tester group `qa-team`. WIF credentials are configured in GitHub environment `oppia-android-release-env`.

| Step | Action | Expected Outcome | Actual Outcome / Status |
| :--- | :--- | :--- | :--- |
| 1 | Navigate to Actions → `deploy_to_firebase`. Select source ref: `release-0.17`, flavor: `beta`. Click "Run workflow". | The workflow derives expected binary name from flavor + source ref commit hash. | ⬜️ Pending |
| 2 | Observe the "Download from Archive" step. | Signed AAB is downloaded from the correct GCS bucket path. | ⬜️ Pending |
| 3 | Observe the "Deploy" step output. | Firebase CLI uploads the signed AAB to App Distribution under group `qa-team`. Release notes from the latest changelog are attached. | ⬜️ Pending |
| 4 | Check the workflow summary. | Summary includes: version name (`0.17-beta`), Firebase release link, AAB size, and tester group name. | ⬜️ Pending |
| 5 | Run the same workflow with an invalid flavor (e.g., `staging`). | The workflow fails with a clear input validation error before any build starts. | ⬜️ Pending |

### User Story 3: Deploy a reviewed build to Play Console with staged rollout
**Setup:** A signed beta AAB exists in the GCS archive. Play Console API credentials are available via WIF. The `beta` track on Play Console has no pending releases.

| Step | Action | Expected Outcome | Actual Outcome / Status |
| :--- | :--- | :--- | :--- |
| 1 | Navigate to Actions → `deploy_to_play_console`. Select flavor: `beta`, track: `beta`, source ref: `release-0.17`, rollout: `25`. Click "Run workflow". | The workflow starts and authenticates to Play Console API via WIF. Downloads AAB from GCS. | ⬜️ Pending |
| 2 | Observe the "Upload" step. | A new edit is created. The signed AAB is uploaded to the `beta` track with the correct changelog. | ⬜️ Pending |
| 3 | Observe the "Set Rollout" step. | Rollout fraction is set to `0.25` (25%). | ⬜️ Pending |
| 4 | Observe the "Commit" step. | The edit is committed. Workflow summary shows version code, track, and rollout percentage. | ⬜️ Pending |
| 5 | After ~1 week, run with same flavor/ref but rollout: `100`. | The existing beta track release is updated to 100% rollout. No new AAB is uploaded. | ⬜️ Pending |
| 6 | Attempt to deploy version `0.16` when `0.17` is already on track. | Workflow fails: version inversion detected. | ⬜️ Pending |

### User Story 4: Receive and install a build via Firebase App Distribution (QA Tester)
**Setup:** A beta build (`0.17-beta`) has been deployed to Firebase App Distribution. The tester's email is registered in the `qa-team` group.

| Step | Action | Expected Outcome | Actual Outcome / Status |
| :--- | :--- | :--- | :--- |
| 1 | Check email for Firebase App Distribution notification. | An email is received with the subject referencing "Oppia" and version `0.17-beta`. | ⬜️ Pending |
| 2 | Click the download link in the email. | The Firebase App Distribution page opens, showing the correct version name and release notes. | ⬜️ Pending |
| 3 | Download and install the APK. | The APK installs successfully without errors. | ⬜️ Pending |
| 4 | Open the installed app → Settings → About. | The version displayed is `0.17-beta` with the correct version code (`200`). | ⬜️ Pending |
| 5 | Repeat with a different tester not in the `qa-team` group. | The tester does not receive a notification and cannot access the build via the Firebase link. | ⬜️ Pending |

### User Story 7: Changelog is auto-generated after the version bump
**Setup:** A version bump PR (→ `MINOR_VERSION = 18`) has been merged to `develop`. Two release branches exist: `release-0.16` and `release-0.17`. 15 PRs have been merged between their merge bases. Vertex AI API credentials available via WIF.

| Step | Action | Expected Outcome | Actual Outcome / Status |
| :--- | :--- | :--- | :--- |
| 1 | Observe `generate_changelog` workflow triggered by the merge event. | The workflow starts and runs `GenerateChangelogs` Kotlin script. | ⬜️ Pending |
| 2 | Observe the "Collect Commits" step. | The script determines this is for version `0.17` (the previous version). Collects all 15 merged PRs and their linked issues. | ⬜️ Pending |
| 3 | Observe the "Generate via LLM" step. | The Vertex AI API is called with the commit/issue data. A user-facing changelog summary is generated. | ⬜️ Pending |
| 4 | Check `develop` for a new PR. | A PR is created by adding `config/changelogs/0.17.md` (changelog file). The PR body includes the raw commit list for reviewer reference. | ⬜️ Pending |
| 5 | Verify the changelog content. | The changelog contains 2-3 concise, user-facing sentences summarizing the changes. No internal implementation details are exposed. | ⬜️ Pending |
| 6 | Simulate Vertex AI API failure (timeout/error). | The workflow still creates a PR with the raw commit list and a note that LLM generation failed. | ⬜️ Pending |

### User Story 9: Pinned lesson versions auto-regenerated weekly
**Setup:** Cron schedule reached. `develop` branch has existing `config/pinned_download_list_versions.textproto`.

| Step | Action | Expected Outcome | Actual Outcome / Status |
| :--- | :--- | :--- | :--- |
| 1 | Cron triggers workflow (or manually via `workflow_dispatch`). | `download_lesson_list` script runs and regenerates the textproto file. | ⬜️ Pending |
| 2 | Check for PR on `develop`. | PR created (or force-pushed to existing branch). Diff shows updated lesson IDs/versions. | ⬜️ Pending |