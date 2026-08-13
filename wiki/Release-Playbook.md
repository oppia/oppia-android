# Release Playbook

This page is the step-by-step coordinator guide for Oppia Android releases using the automated
release pipeline (PR series #6106). For conceptual background on any step, see the
[App and Feature Release Process](app-and-feature-release-process.md) wiki page. For manual
fallback procedures when automation fails, see the
[In-Depth Release Reference](In-Depth-Release-Reference.md).

---

## Table of Contents

1. [Weekly alpha release](#1-weekly-alpha-release)
2. [Changelog review](#2-changelog-review)
3. [Production release](#3-production-release)
4. [Staged rollout](#4-staged-rollout)
5. [Weekly coordinator checklist](#5-weekly-coordinator-checklist)

---

## 1. Weekly Alpha Release

The `auto_release_alpha.yml` workflow fires automatically every **Tuesday at 03:30 UTC**. It
finds the latest passing commit on `develop`, tags it as `latest-alpha`, and dispatches
`build_and_sign.yml`. The coordinator's job starts at the approval gate.

### Steps

- [ ] **Approve the build** — Navigate to Actions → `build_and_sign.yml` run → approve in the
  `oppia-android-release-env` gate.
- [ ] **Copy the GCS path** — After the build completes, copy the signed AAB path from the job
  summary (format: `gs://oppia-android-alpha-releases/…/*.aab`).
- [ ] **Distribute to QA** — Trigger `deploy_to_firebase.yml` with the GCS path above.
- [ ] **Await QA sign-off** — Notify the alpha tester group and wait for their confirmation.
- [ ] **Deploy to Play Console** — Trigger `deploy_to_play_console.yml`:
  | Input | Value |
  |---|---|
  | `gcs_aab_path` | GCS path from above |
  | `track` | `alpha` |
  | `rollout_fraction` | `1000` (100% for alpha track) |

> If `auto_release_alpha.yml` exits with an error or finds no passing commit, see the
> [In-Depth Release Reference](In-Depth-Release-Reference.md) for manual fallback steps.

---

## 2. Changelog Review

`generate_changelog.yml` opens a PR to `develop` automatically when `version.bzl` is updated
with a new `MINOR_VERSION`. It can also be triggered manually via `workflow_dispatch`.

### Steps

- [ ] Open the changelog PR created by the workflow.
- [ ] Review the AI-generated release notes in `config/changelogs/{version}.md` (and any
  flavor-specific overrides such as `{version}_alpha.md` or `{version}_beta.md`).
- [ ] Edit the release notes if the AI summary needs adjustment before merging.
- [ ] Merge the changelog PR into `develop`.

---

## 3. Production Release

Follow this checklist for a full beta or production (GA) release.

### Pre-release

- [ ] Confirm `MINOR_VERSION` is bumped in `version.bzl` and the changelog PR is merged (§2).
- [ ] Cut the release branch from `develop` HEAD:
  ```
  git checkout -b release-0.X upstream/develop
  git push upstream release-0.X
  ```

### Build & sign

- [ ] Trigger `build_and_sign.yml`:
  | Input | Value |
  |---|---|
  | `flavor` | `beta` or `ga` |
  | `source_ref` | `release-0.X` |
- [ ] Approve the run in the `oppia-android-release-env` gate.
- [ ] Copy the signed AAB GCS path from the job summary.

### QA

- [ ] Trigger `deploy_to_firebase.yml` with the GCS path.
- [ ] Notify QA testers and await sign-off.
- [ ] If QA fails: land the fix on `develop`, cherry-pick to `release-0.X`, rebuild from
  Build & sign above.

### Deploy

- [ ] Trigger `deploy_to_play_console.yml`:
  | Input | Value |
  |---|---|
  | `gcs_aab_path` | GCS path from above |
  | `track` | `beta` or `production` |
  | `rollout_fraction` | `100` (10% initial rollout — see §4) |

---

## 4. Staged Rollout

After the initial deployment, progressively increase the rollout using `update_rollout.yml`.
Monitor Firebase Crashlytics between each step; halt and investigate if crash rates spike.

| Day | `rollout_fraction` | Action |
|---|---|---|
| 0 | `100` | Initial deploy via `deploy_to_play_console.yml` |
| 1 | `250` | Trigger `update_rollout.yml` after monitoring |
| 3 | `500` | Trigger `update_rollout.yml` |
| 7+ | `1000` | Trigger `update_rollout.yml` for full rollout |

**Inputs for `update_rollout.yml`:**

| Input | Value |
|---|---|
| `track` | `beta` or `production` |
| `version` | e.g. `0.18` |
| `rollout_fraction` | New fraction from the table above |

---

## 5. Weekly Coordinator Checklist

Run through this checklist each week:

- [ ] Check whether `auto_release_alpha.yml` completed successfully on Tuesday. If not,
  consult the [In-Depth Release Reference](In-Depth-Release-Reference.md).
- [ ] Check whether `pull_latest_lesson_versions.yml` opened a PR on Monday. If so, review
  and merge the lesson-versions PR.
- [ ] Check for any pending changelog PRs opened by `generate_changelog.yml` and merge them.
- [ ] Review crash rates in Firebase Crashlytics and advance the rollout fraction if stable
  (see §4).

---

*See also: [App and Feature Release Process](app-and-feature-release-process.md) ·
[In-Depth Release Reference](In-Depth-Release-Reference.md) ·
[Platform Parameters & Feature Flags](Platform-Parameters-&-Feature-Flags.md)*
