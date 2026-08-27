# Release Playbook

This page is the step-by-step coordinator guide for Oppia Android releases using the automated
release pipeline. For conceptual background on any step, see the
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

The **Auto Release Alpha** workflow, defined by
[`auto_release_alpha.yml`](../.github/workflows/auto_release_alpha.yml), fires automatically every
**Tuesday at 03:30 UTC**. It finds the latest passing commit on `develop`, tags it as
`latest-alpha`, and dispatches **Build and Sign**. The coordinator's job starts at the approval
gate.

### Steps

- [ ] **Approve the build** — Navigate to Actions → **Build and Sign** run → approve in the
  `oppia-android-release-env` gate.
- [ ] **Copy the GCS path** — After the build completes, copy the signed AAB path from the job
  summary (format: `gs://oppia-android-alpha-releases/…/*.aab`), which will be used for the
  deployment steps.
- [ ] **Distribute to QA** — Trigger **Deploy to Firebase** (`deploy_to_firebase.yml`) with the
  GCS path above and approve the run in the `oppia-android-release-env` gate.
- [ ] **Await QA sign-off** — Notify the alpha tester group and wait for their confirmation that
  the alpha build is stable and ready for Play Store deployment.
- [ ] **Deploy to Play Console** — After QA sign-off, trigger **Deploy to Play Console**
  (`deploy_to_play_console.yml`) with `track=alpha` and `rollout_fraction=1000` (100% of the
  alpha track). The Firebase and Play Console deployments are sequential: QA on Firebase is
  completed first, then the approved build is pushed to the alpha track:

  ![Deploy to Play Console dispatch dialog](https://github.com/user-attachments/assets/76b3dbbd-eede-4a1c-a359-8751d08b304e)

> If **Auto Release Alpha** exits with an error or finds no passing commit, see the
> [In-Depth Release Reference](In-Depth-Release-Reference.md) for manual fallback steps.

---

## 2. Changelog Review

The **Generate Changelogs** workflow
([`generate_changelog.yml`](../.github/workflows/generate_changelog.yml)) opens a PR to
`develop` automatically when `version.bzl` is updated with a new `MINOR_VERSION`. It can also
be triggered manually via `workflow_dispatch`.

### Steps

- [ ] Review the AI-generated release notes in `config/changelogs/{version}.md` (and any
  flavor-specific overrides such as `{version}_alpha.md` or `{version}_beta.md`).
- [ ] Edit the release notes if the AI summary needs adjustment before merging.
- [ ] Merge the changelog PR into `develop`. Merging automatically triggers **Deploy Updated
  Changelog** (`deploy_updated_changelog.yml`), which uploads the release notes to Play Console.

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

- [ ] Trigger **Build and Sign** (`build_and_sign.yml`) with `flavor=beta` or `flavor=ga` and
  `source_ref=release-0.X`:

  ![Build and Sign dispatch dialog](https://github.com/user-attachments/assets/c9119df5-5d3a-459e-bb31-ba3b7683b98b)

- [ ] Approve the run in the `oppia-android-release-env` gate.
- [ ] Copy the signed AAB GCS path from the job summary.

### QA

- [ ] Trigger **Deploy to Firebase** (`deploy_to_firebase.yml`) with the GCS path and approve
  the run in the `oppia-android-release-env` gate.
- [ ] Notify QA testers and await sign-off.
- [ ] If critical issues are found during QA: land the fix on `develop`, cherry-pick to
  `release-0.X`, then rebuild from Build & sign above.

### Deploy

- [ ] Trigger **Deploy to Play Console** (`deploy_to_play_console.yml`) with `track=beta` or
  `track=production` and `rollout_fraction=100` (10% initial rollout — see §4).

---

## 4. Staged Rollout

After the initial deployment, progressively increase the rollout using `update_rollout.yml`.
Monitor Firebase Crashlytics between each step; halt and investigate if crash rates spike.

| Day | `rollout_fraction` | Actual rollout | Action |
|---|---|---|---|
| 0 | `100` | 10% | Initial deploy via **Deploy to Play Console** |
| 1 | `250` | 25% | Trigger **Update Rollout** after monitoring |
| 3 | `500` | 50% | Trigger **Update Rollout** |
| 7+ | `1000` | 100% | Trigger **Update Rollout** for full rollout |

**Inputs for `update_rollout.yml`:**

| Input | Value |
|---|---|
| `track` | `beta` or `production` |
| `version` | e.g. `0.18` |
| `rollout_fraction` | New fraction from the table above |

---

## 5. Weekly Coordinator Checklist

Run through this checklist each week:

- [ ] Check whether **Auto Release Alpha** completed successfully on Tuesday. If not,
  consult the [In-Depth Release Reference](In-Depth-Release-Reference.md).
- [ ] Check whether **Pull Latest Lesson Versions** opened a PR on Monday. If so, review
  and merge the lesson-versions PR.
- [ ] Check for any pending changelog PRs opened by **Generate Changelogs** and merge them.
- [ ] Review crash rates in Firebase Crashlytics and advance the rollout fraction if stable
  (see §4).

---
