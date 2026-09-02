# In-Depth Release Reference

This page is the manual fallback reference for every automated step in the Oppia Android
release pipeline. It covers what to do when an automated workflow fails or needs to be run
manually outside of its normal trigger. It is linked from the
[Release Playbook](Release-Playbook.md).

For the standard step-by-step coordinator guide see the
[Release Playbook](Release-Playbook.md). For conceptual background see the
[App and Feature Release Process](app-and-feature-release-process.md).

---

## Table of Contents

1. [generate\_changelog.yml fails](#1-generate_changelogymlfails)
2. [auto\_release\_alpha.yml fails](#2-auto_release_alphaymlfails)
3. [pull\_latest\_lesson\_versions.yml fails](#3-pull_latest_lesson_versionsymlfails)
4. [deploy\_updated\_changelog.yml fails](#4-deploy_updated_changelogymlfails)
5. [build\_and\_sign.yml fails](#5-build_and_signymlfails)
6. [deploy\_to\_firebase.yml fails](#6-deploy_to_firebaseymlfails)
7. [deploy\_to\_play\_console.yml fails](#7-deploy_to_play_consoleymlfails)
8. [update\_rollout.yml fails](#8-update_rolloutymlfails)

---

## 1. generate_changelog.yml fails

**Normal trigger:** Push to `develop` that modifies `version.bzl`, or manual dispatch.

**What it does:** Runs `GenerateChangelogs.kt` (Vertex AI) and opens a changelog PR.

**Manual fallback:**

1. Run the script locally:
   ```bash
   bazel run //scripts:generate_changelogs -- \
     $(pwd) \
     <version>          # e.g. 0.18
     <github_token>     # PAT with repo scope
   ```
2. The script writes `config/changelogs/<version>.md` (and flavor variants if applicable).
3. Commit the file and open a PR to `develop` manually.
4. Review and edit the AI-generated notes before merging.

> **Note:** If Vertex AI is unavailable, write the release notes manually based on `git log`
> since the previous version tag.

---

## 2. auto_release_alpha.yml fails

**Normal trigger:** Weekly cron, Tuesday 03:30 UTC.

**What it does:** Finds the latest passing commit on `develop`, tags it as `latest-alpha`,
and dispatches `build_and_sign.yml`.

**Case A — No commits exist within the configured limit:**

The workflow exits cleanly (no error). No action needed unless a release is urgent — in that
case, manually dispatch the workflow or extend the commit search limit via `workflow_dispatch`
inputs.

**Case B — Commits exist but none have passing CI:**

The workflow exits with an error. The alpha channel is blocked on CI flakiness.
1. Investigate the failing CI checks on `develop` and fix the root cause.
2. Once CI is green, either wait for the next Tuesday cron or manually dispatch
   `auto_release_alpha.yml` via `workflow_dispatch`.

**Case C — Workflow succeeded but `build_and_sign.yml` was not dispatched:**

Manually force-push the `latest-alpha` tag to the desired commit and then trigger
`build_and_sign.yml`:

```bash
git tag -f latest-alpha <commit-sha>
git push -f upstream latest-alpha
```

Then trigger `build_and_sign.yml` via workflow_dispatch:
- `flavor`: `alpha`
- `source_ref`: `latest-alpha`

---

## 3. pull_latest_lesson_versions.yml fails

**Normal trigger:** Weekly cron, Monday 02:30 UTC.

**What it does:** Downloads the latest lesson versions from the Oppia production server and
opens a PR updating `config/lessons/*.textproto`.

**Manual fallback:**

1. Obtain `prod_server.key` from the repository secret (ask the infrastructure team).
2. Run locally for both flavors:
   ```bash
   bazel run //scripts:download_lesson_list -- \
     https://www.oppia.org \
     https://storage.googleapis.com \
     oppiaserver-resources \
     $(pwd)/prod_server.key \
     $(pwd)/config/lessons/alpha_pinned_lesson_versions.textproto \
     $(pwd)/scripts/assets/alpha_download_config.textproto

   bazel run //scripts:download_lesson_list -- \
     https://www.oppia.org \
     https://storage.googleapis.com \
     oppiaserver-resources \
     $(pwd)/prod_server.key \
     $(pwd)/config/lessons/prod_pinned_lesson_versions.textproto \
     $(pwd)/scripts/assets/prod_download_config.textproto
   ```
3. Commit both updated textproto files and open a PR to `develop`.
4. Delete `prod_server.key` from your local machine after use.

---

## 4. deploy_updated_changelog.yml fails

**Normal trigger:** Push to `develop` that modifies `config/changelogs/**.md`, or manual
dispatch.

**What it does:** Uploads updated release notes to Play Console for a live release.

**Manual fallback — trigger via dispatch:**

If the automatic trigger failed, re-run manually:

1. Go to Actions → `deploy_updated_changelog.yml` → **Run workflow**.
2. Fill in:
   - `version`: e.g. `0.18`
   - `flavor`: `alpha`, `beta`, or leave blank for the default changelog

**Manual fallback — run script locally:**

```bash
bazel run //scripts:upload_changelog_to_play_console -- \
  $(pwd) \
  <version>   \
  <flavor>    \
  <play_console_credentials_json>
```

> **Note:** The script will fail if the version is not yet live on Play Console — this is by
> design to prevent a race with the initial binary upload.

---

## 5. build_and_sign.yml fails

**Normal trigger:** Manual dispatch (or dispatched by `auto_release_alpha.yml`).

**What it does:** Builds the release AAB with Bazel and signs it via Cloud KMS.

**Common failure causes and fixes:**

| Symptom | Fix |
|---|---|
| Bazel build error | Check the build logs; likely a code issue on the `source_ref` branch |
| Cloud KMS permission denied | Verify the Workload Identity Federation service account has `roles/cloudkms.signerVerifier` |
| GCS upload failed | Check the GCS bucket exists and the service account has `roles/storage.objectAdmin` |
| Approval gate timed out | Re-run the workflow and approve promptly |

There is no local fallback for signing — the private key never leaves Cloud KMS by design.
If KMS is unavailable, wait for the outage to resolve before retrying.

---

## 6. deploy_to_firebase.yml fails

**Normal trigger:** Manual dispatch after `build_and_sign.yml` succeeds.

**What it does:** Distributes the signed AAB to Firebase App Distribution.

**Manual fallback:**

1. Download the signed AAB from the GCS path shown in the `build_and_sign.yml` job summary:
   ```bash
   gcloud storage cp gs://oppia-android-<flavor>-releases/.../*.aab .
   ```
2. Upload to Firebase App Distribution manually using the Firebase CLI:
   ```bash
   firebase appdistribution:distribute oppia-android-*.aab \
     --app <firebase-app-id> \
     --groups <tester-group>
   ```
   Or upload via the Firebase console at https://console.firebase.google.com.

---

## 7. deploy_to_play_console.yml fails

**Normal trigger:** Manual dispatch after QA sign-off.

**What it does:** Uploads the AAB to a Play Console track at a given rollout fraction.

**Common failure causes and fixes:**

| Symptom | Fix |
|---|---|
| Version inversion error | Verify you are deploying a newer version than what is live on the target track |
| Duplicate deploy error | The same commit SHA is already live — no action needed |
| Changelog missing | Ensure `config/changelogs/<version>.md` exists and is merged to `develop` |
| Active edit session conflict | Wait ~5 minutes for the previous Play API session to expire, then retry |

**Manual fallback — Play Console web UI:**

If the script cannot recover, upload the AAB directly:
1. Go to [Play Console](https://play.google.com/console) → Oppia Android → the target track.
2. Click **Create new release** and upload the AAB from GCS.
3. Set the rollout percentage manually.

---

## 8. update_rollout.yml fails

**Normal trigger:** Manual dispatch to increase staged rollout fraction.

**What it does:** Calls the Play Developer API to update the rollout fraction for a live
release without re-uploading the binary.

**Manual fallback — Play Console web UI:**

1. Go to [Play Console](https://play.google.com/console) → Oppia Android → the target track.
2. Click **Manage rollout** on the current release.
3. Increase the rollout percentage to the desired value.

**Common failure causes:**

| Symptom | Fix |
|---|---|
| Active edit session conflict | The `deploy_updated_changelog.yml` concurrency lock may be held — wait and retry |
| Version not found on track | Verify `version` input matches a release currently live on the track |

---
