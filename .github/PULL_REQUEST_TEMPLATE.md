<!-- READ ME FIRST: Please fill in the explanation section below and check off every point from the Essential Checklist! -->
## Explanation
<!--
  - Explain what your PR does. If this PR fixes an existing bug, please include
  - "Fixes #bugnum:" in the explanation so that GitHub can auto-close the issue
  - when this PR is merged.
  -->

## Essential Checklist
<!-- Please tick the relevant boxes by putting an "x" in them. -->
- [ ] The PR title and explanation each start with "Fix #bugnum: " (If this PR fixes part of an issue, prefix the title with "Fix part of #bugnum: ...".)
- [ ] Any changes to [scripts/assets](https://github.com/oppia/oppia-android/tree/develop/scripts/assets) files have their rationale included in the PR explanation.
- [ ] The PR follows the [style guide](https://github.com/oppia/oppia-android/wiki/Coding-style-guide).
- [ ] The PR does not contain any unnecessary code changes from Android Studio ([reference](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR#undo-unnecessary-changes)).
- [ ] The PR is made from a branch that's **not** called "develop" and is up-to-date with "develop".
- [ ] The PR is **assigned** to the appropriate reviewers ([reference](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR#clarification-regarding-assignees-and-reviewers-section)).

## For UI-specific PRs only
<!-- Delete these section if this PR does not include UI-related changes. -->
If your PR includes UI-related changes, then:
- Add screenshots for portrait/landscape for both a tablet & phone of the before & after UI changes
- For the screenshots above, include both English and pseudo-localized (RTL) screenshots (see [RTL guide](https://github.com/oppia/oppia-android/wiki/RTL-Guidelines))
- Add a video showing the full UX flow with a screen reader enabled (see [accessibility guide](https://github.com/oppia/oppia-android/wiki/Accessibility-(A11y)-Guide))
- Add a screenshot demonstrating that you ran affected Espresso tests locally & that they're passing
