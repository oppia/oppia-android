# Feature Development Guide

This guide is for **Android developers** who need to gate a feature behind a feature flag, understand how that flag progresses through the release pipeline, and request that it be enabled in production.

For a full explanation of the Platform Parameter system and the Dagger wiring required to create flags, see the [Platform Parameters & Feature Flags](Platform-Parameters-&-Feature-Flags) wiki.

## Table of Contents

- [Gating a Feature Behind a Flag](#gating-a-feature-behind-a-flag)
- [Navigating the Release Process](#navigating-the-release-process)
- [Requesting Production Enablement](#requesting-production-enablement)

---

## Gating a Feature Behind a Flag

> TODO: Explain the step-by-step process for gating new feature code behind a feature flag:
> creating the constant in `FeatureFlagConstants.kt`, providing it in `PlatformParameterModule`,
> injecting and checking it in the feature code, and writing tests with the flag both enabled
> and disabled.

---

## Navigating the Release Process

> TODO: Explain the lifecycle of a feature flag across releases — how it starts `false` in alpha,
> what the developer needs to verify during beta testing, and when it is safe to request
> production enablement. Include a table showing the flag state at each release stage
> (alpha / beta / GA) and reference the Release Playbook for the release timeline.

---

## Requesting Production Enablement

> TODO: Explain how a developer requests that their feature flag be flipped to `true` in the
> Feature Gating Console for production. Include: who to contact (CLaM lead / release
> coordinator), what information to provide (flag name, issue number, verified beta status),
> and what the expected timeline is relative to the monthly release cycle.
