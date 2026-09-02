# Feature Development Guide

This guide is for **Android developers** who need to ship a feature safely. It covers how to gate
a feature behind a feature flag, how the feature moves through the release pipeline, and how to
request that it be enabled in production.

**For the technical mechanics of creating a flag** (constants, Dagger wiring, analytics logging),
see the [Platform Parameters & Feature Flags](Platform-Parameters-&-Feature-Flags.md) wiki.

---

## Table of Contents

1. [Gating a Feature Behind a Flag](#1-gating-a-feature-behind-a-flag)
2. [Navigating the Release Process](#2-navigating-the-release-process)
3. [Requesting Production Enablement](#3-requesting-production-enablement)

---

## 1. Gating a Feature Behind a Flag

Feature flags allow large features to land on `develop` incrementally and be enabled only when
they are fully ready, without blocking other releases. Every integration point of a multi-sprint
feature — UI entry points, background jobs, migrations — must be gated behind the flag.

### When you need a flag

Use a feature flag when your change:
- Spans more than one release cycle.
- Touches data migrations or irreversible schema changes.
- Could partially break the app if released before all integration points are complete.

### Gating code behind a flag

After [creating the flag constant and Dagger binding](Platform-Parameters-&-Feature-Flags.md#how-to-create-a-feature-flag),
inject the flag into any class that has a guarded entry point and check it before executing
feature-specific logic:

```kotlin
class SomeController @Inject constructor(
  @EnableMyNewFeature private val enableMyNewFeature: PlatformParameterValue<Boolean>
) {
  fun doSomething() {
    if (!enableMyNewFeature.value) return   // <-- guard every entry point

    // feature code here
  }
}
```

### Rules for gating

| Rule | Why |
|---|---|
| Default value is **`false`** | The feature must be invisible until explicitly enabled. |
| **Every** entry point is gated | A partially visible feature can corrupt data or confuse users. |
| No flag checks inside feature-internal code | Gate at the boundary; internals assume the flag is already `true`. |
| Flag check is the **first** thing in the guarded function | Keeps the guard obvious and avoids partial side effects. |

### Writing tests

Test both states of the flag. See
[How to write tests related to Platform Parameters](Platform-Parameters-&-Feature-Flags.md#how-to-write-tests-related-to-platform-parameters)
for the full test setup patterns. At minimum:

```kotlin
@Test
fun testFeature_flagDisabled_doesNothing() {
  // Set up with flag = false (default)
  setUpWithFlag(enabled = false)
  // Assert feature is not visible / not executed
}

@Test
fun testFeature_flagEnabled_works() {
  // Set up with flag = true
  setUpWithFlag(enabled = true)
  // Assert feature behaves correctly
}
```

---

## 2. Navigating the Release Process

Once your flagged feature lands on `develop`, it moves through a defined lifecycle before it
reaches production. The lifecycle is:

```
develop (flag off) → alpha build (flag on via alpha textproto)
  → product review → beta build (flag on via beta textproto)
  → tech lead sign-off → production (flag on via GA textproto) → cleanup
```

> **Alpha enablement is opt-in.** The alpha textproto is only updated when you explicitly request
> it — for example, to gather early feedback or validate the feature before product review.
> If you do not need alpha testing, your feature stays flagged off in the alpha build and first
> becomes visible in beta.
>
> To request alpha enablement, ask the release coordinator to add your flag to
> [`alpha/feature_flags_overrides.textproto`](../config/src/java/org/oppia/android/config/platform/featureoverrides/alpha/feature_flags_overrides.textproto)
> for the current release branch.

### Flag state across release stages

| Stage | Where the flag lives | Flag state | Who controls it |
|---|---|---|---|
| **Development** | `develop` branch | `false` (default) | Developer |
| **Alpha** | `release-X.Y` → alpha build | `false` by default; `true` only if alpha testing was requested (enabled via [`alpha/feature_flags_overrides.textproto`](../config/src/java/org/oppia/android/config/platform/featureoverrides/alpha/feature_flags_overrides.textproto)) | Release coordinator |
| **Beta** | `release-X.Y` → beta build | `true` (enabled via [`beta/feature_flags_overrides.textproto`](../config/src/java/org/oppia/android/config/platform/featureoverrides/beta/feature_flags_overrides.textproto), if alpha passed) | Release coordinator |
| **Production (GA)** | GA rollout | `true` (after QA + product sign-off) | Tech lead / release coordinator |
| **Cleanup** | Next release cycle | Flag removed entirely | Developer |

### What you need to do at each stage

**Before alpha cut:**
- Ensure all feature code is behind the flag and merged to `develop`.
- Confirm the flag's default value is `false`.
- Verify your tracking issue is up to date and linked from all feature PRs that have merged.
- Add the feature's CUJs to the Android team's CUJ sheet.

**During alpha (only if alpha testing was requested):**
- The coordinator enables your flag for the alpha build by setting it to `true` in
  [`alpha/feature_flags_overrides.textproto`](../config/src/java/org/oppia/android/config/platform/featureoverrides/alpha/feature_flags_overrides.textproto).
- Participate in alpha testing — verify the feature works end-to-end in a real build.
- File any regressions as separate issues, linked to the original tracking issue as child/sub-issues.
- If alpha testing was **not** requested, skip ahead to the beta stage below.

**During beta:**
- If alpha testing passes and product review approves, the coordinator enables the flag in beta
  by setting it to `true` in [`beta/feature_flags_overrides.textproto`](../config/src/java/org/oppia/android/config/platform/featureoverrides/beta/feature_flags_overrides.textproto).
- QA testers verify the feature in the beta build.

**Production:**
- After QA sign-off and team lead approval, the coordinator enables the flag in production.
- See [Requesting Production Enablement](#requesting-production-enablement) below.

**Cleanup (after production enablement):**
- Once the flag is fully enabled in production and stable for at least one release cycle, remove
  the flag constant, Dagger binding, gating check, and all flag-specific test branches.
- Removal PR should reference and close the original tracking issue.

### How feature flag enablement relates to binary releases

For oppia-android, flag states are controlled by per-flavor textproto override files that are
compiled into the binary. Enabling or disabling a flag for a specific release stage requires
modifying the corresponding override file and shipping a new binary for that flavor:

- **Alpha**: [`alpha/feature_flags_overrides.textproto`](../config/src/java/org/oppia/android/config/platform/featureoverrides/alpha/feature_flags_overrides.textproto)
- **Beta**: [`beta/feature_flags_overrides.textproto`](../config/src/java/org/oppia/android/config/platform/featureoverrides/beta/feature_flags_overrides.textproto)
- **Production (GA)**: [`ga/feature_flags_overrides.textproto`](../config/src/java/org/oppia/android/config/platform/featureoverrides/ga/feature_flags_overrides.textproto)

This means flag enablement is tied to the binary release cycle. For the binary release timeline
(branch cut dates, alpha/beta/GA dates), refer to the [Release Playbook](Release-Playbook.md).

---

## 3. Requesting Production Enablement

Once your feature has passed QA in beta and received team lead sign-off, you need to request
that the release coordinator enable your flag in the production environment.

### When to request

Only request production enablement when **all** of the following are true:

- [ ] The feature has been live in the beta build for at least one release cycle with no regressions.
- [ ] Product review has approved the feature for production.
- [ ] QA has signed off on the beta build.
- [ ] Team lead has approved.

### How to request

File a production enablement issue using the
[new launch request template](https://github.com/oppia/product-operations-team/issues/new?template=1_new_launch.yml)
in the product operations tracker. See
[product-operations-team#46](https://github.com/oppia/product-operations-team/issues/46) for a
complete example. Then assign or ping the **Tech lead** and the **release coordinator** for the
current cycle.

### Timeline

Production enablement happens as part of the monthly release cycle. If your request arrives
**before the GA rollout step**, the coordinator will enable your flag during that cycle's GA
rollout. If it arrives after, it targets the next release.

Check the [Release Playbook](Release-Playbook.md) for the current cycle's GA date.

### After production enablement

Once the coordinator confirms the flag is enabled in production:

1. Monitor crash rates and error logs for at least one week.
2. If stable, file a cleanup PR to remove the flag entirely (see [Cleanup](#what-you-need-to-do-at-each-stage)).
3. Close the tracking issue once cleanup is merged.
