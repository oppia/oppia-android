# Release Workflow Manual Fallbacks

This page covers what to do when an automated release workflow fails or needs to be run
manually outside of its normal trigger.

For the standard step-by-step coordinator guide see the
[Release Playbook](Release-Playbook.md). For conceptual background see the
[App and Feature Release Process](app-and-feature-release-process.md).

---

## Table of Contents

1. [Generate Changelog fails](#1-generate-changelog-fails)
2. [Auto Release Alpha fails](#2-auto-release-alpha-fails)
3. [Pull Latest Lesson Versions fails](#3-pull-latest-lesson-versions-fails)
4. [Deploy Updated Changelog fails](#4-deploy-updated-changelog-fails)
5. [Build and Sign Release fails](#5-build-and-sign-release-fails)
6. [Deploy to Firebase fails](#6-deploy-to-firebase-fails)
7. [Deploy to Play Console fails](#7-deploy-to-play-console-fails)
8. [Update Rollout fails](#8-update-rollout-fails)

---

## 1. Generate Changelog fails

**Normal trigger:** A push to `develop` that modifies `version.bzl`, or manual dispatch.

**What it does:** Runs `GenerateChangelogs.kt` (Vertex AI) and opens a changelog PR.

**Manual fallback:**

1. Run the script locally:
   ```bash
   bazel run //scripts:generate_changelogs -- \
     $(pwd) \
     <version>          # e.g. 0.18
     <github_token>     # PAT with repo scope — see https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens
   ```
2. The script writes `config/changelogs/<version>.md` (and flavor variants if applicable).
3. Commit the file and open a PR to `develop` manually.
4. Review and edit the AI-generated notes before merging.

> **Note:** If Vertex AI is unavailable, the workflow still opens the changelog PR — it falls
> back to a raw commit list and inserts an `<!-- LLM generation failed -->` marker. Review the
> auto-created PR, fill in the user-facing summary manually, and merge it as normal.

---

## 2. Auto Release Alpha fails

**Normal trigger:** Weekly cron, Tuesday 03:30 UTC.

**What it does:** Finds the latest passing commit on `develop`, tags it as `latest-alpha`,
and dispatches Build and Sign Release.

**Case A — No new commits since the latest-alpha tag:**

The workflow exits cleanly (no error). No action is needed. If a release is urgent, manually
trigger the workflow:
1. Go to **Actions** → **Auto Release Alpha** → **Run workflow**.
2. Click **Run workflow** (no inputs required).

The script will re-evaluate recent commits against the current `latest-alpha` tag and
dispatch Build and Sign Release if a newer passing commit is found.

**Case B — Commits exist but none have passing CI:**

The workflow exits with an error. The alpha channel is blocked on CI flakiness.
1. Investigate the failing CI checks on `develop` and fix the root cause.
2. Once CI is green, either wait for the next Tuesday cron or manually trigger via
   **Actions** → **Auto Release Alpha** → **Run workflow**.

**Case C — Workflow succeeded but Build and Sign Release was not dispatched:**

This can happen when `FindAlphaCandidate` finds no commits newer than the existing
`latest-alpha` tag — i.e., the tag already points to the newest passing commit on `develop`
so the dispatch step is intentionally skipped. It can also occur if the `gh workflow run`
API call to dispatch Build and Sign Release fails transiently after the tag was already
updated.

To manually trigger a build for a specific commit:

1. Force-push the `latest-alpha` tag to the desired commit:
   ```bash
   git tag -f latest-alpha <commit-sha>
   git push -f upstream latest-alpha
   ```
2. Trigger Build and Sign Release via **Actions** → **Build and Sign Release** →
   **Run workflow**:
   - **flavor**: `alpha`
   - **source_ref**: `latest-alpha`

---

## 3. Pull Latest Lesson Versions fails

**Normal trigger:** Weekly cron, Monday 02:00 UTC.

**What it does:** Downloads the latest lesson versions from the Oppia production server and
opens a PR updating `config/lessons/*.textproto`.

**Manual fallback — re-dispatch:**

If the failure was transient (network error, API rate limit), re-trigger the workflow:
1. Go to **Actions** → **Pull Latest Lesson Versions** → **Run workflow**.
2. Click **Run workflow**.

Re-dispatch is sufficient for most transient failures. Use the local fallback below only if
the GitHub Actions environment itself is unavailable or the stored secret is suspected to be
incorrect.

**Manual fallback — run locally:**

The workflow uses a production API secret (`PROD_SERVER_LESSON_SECRET`) that is not
distributed for security reasons. **Contact a tech lead** to perform this recovery — they
have access to the secret and can run the steps below or re-run the workflow directly.

1. Obtain `prod_server.key` from the secure secret store (tech lead only).
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

---

## 4. Deploy Updated Changelog fails

**Normal trigger:** Push to `develop` that modifies `config/changelogs/**.md`, or manual
dispatch.

**What it does:** Uploads updated release notes to Play Console for a live release.

**Manual fallback — trigger via dispatch:**

If the automatic trigger failed, re-run manually:

1. Go to **Actions** → **Deploy Updated Changelog** → **Run workflow**.
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

**Manual fallback — edit directly in Play Console:**

Release notes can also be updated directly in the Play Console web UI:
1. Go to [Play Console](https://play.google.com/console) → Oppia Android → the target track.
2. Click **Manage release** on the live release → **Edit release**.
3. Update the **Release notes** field and click **Save**.
4. Submit the release for review or publish directly as appropriate.

> **Note:** The script will fail if the version is not yet live on Play Console — this is by
> design to prevent a race with the initial binary upload.

---

## 5. Build and Sign Release fails

**Normal trigger:** Manual dispatch (or dispatched by Auto Release Alpha).

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

## 6. Deploy to Firebase fails

**Normal trigger:** Manual dispatch after Build and Sign Release succeeds.

**What it does:** Distributes the signed AAB to Firebase App Distribution.

**Manual fallback:**

1. Download the signed AAB from the GCS path shown in the Build and Sign Release job summary:
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

## 7. Deploy to Play Console fails

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

**To preserve a previous release alongside a new one (keep two versions alive):**

When a new release is deployed, Play Console may stop serving the previous binary to existing
device configurations (e.g. keeping 16-kitkat alive alongside release 17). To retain both:

1. Go to [Play Console](https://play.google.com/console) → Oppia Android → the target track.
2. Click **Create new release** and upload the new AAB.
3. Under **APKs and AABs**, click **Add from library** and select the old version code you
   want to continue serving to existing users.
4. Both version codes will now be listed in the same release entry — the new one as the
   primary, the old one as retained. Publish the release.

> **Note:** The automated `deploy_to_play_console.yml` script handles this automatically via
> the frozen version codes configuration. Manual steps above are only needed if the script
> cannot run or the old version code was accidentally dropped from the track.

---

## 8. Update Rollout fails

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
| Active edit session conflict | The Deploy Updated Changelog concurrency lock may be held — wait and retry |
| Version not found on track | Verify `version` input matches a release currently live on the track |

---
