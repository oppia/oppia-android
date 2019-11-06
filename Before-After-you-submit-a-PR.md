The following instructions will help you submit a PR effectively and in a much more cleaner way.
Before going through this document make sure you look at [Home-Page](https://github.com/oppia/oppia-android/wiki).

## Important Points

1. **UI related issue/bug fix**: If your PR introduces changes to the UI/UX part of the android app then make sure you include before and after screenshot. Maybe be include a video if needed. Test the UI/UX with [Accessibility Scanner](https://support.google.com/accessibility/android/answer/6376570?hl=en). _(Tip: All your dimensions should be in multiple of of 4dp.)_
2. **Bug fixes**: While fixing an issue which is labelled as **Bug**, make sure to write test cases which actually catches that bug.
3. **Self Review**: Always self review your PR first before assigning it to anyone else, so that you can fix nit changes at very early stage.

##Clarification regarding **Assignees** and **Reviewers** section.
1. **Reviewers**: Add reviewers in this section who should review this PR, there can be multiple reviewers too. Once this section is filled out it would mostly not change throughout the timeline of that PR.
2. **Assignees**: Assignees indicate who all needs to currently check that PR. For the first time while submitting the PR, the **Assignees** and **Reviewers** section will be same. Now, once a reviewer has reviewed the PR, they will de-assign themselves and assign it back to you. And similarly the code-owner will make the request-changes and assign it back to the appropriate reviewer and de-assign themselves. That way this section will keep on changing, always point to people who need to check that PR currently.

## Checklist before submitting a PR

Note: This checklist should be embedded in your every PR explanation with appropriate ticks. Please tick the relevant boxes by putting an "x" in them
- [ ] The PR title starts with "Fix #bugnum: ", followed by a short, clear summary of the changes. (If this PR fixes part of an issue, prefix the title with "Fix part of #bugnum: ...".)
- [ ] The PR explanation includes the words "Fixes #bugnum: ..." (or "Fixes part of #bugnum" if the PR only partially fixes an issue).
- [ ] The PR follows the [style guide](https://github.com/oppia/oppia-android/wiki/Coding-style-guide).
- [ ] The PR does not contain any unnecessary auto-generated code or unnecessary files from Android Studio.
- [ ] The PR is made from a branch that's **not** called "develop".
- [ ] The PR's branch is based on "develop" and **not** on any other branch.
- [ ] The PR is **assigned** to an appropriate reviewer in **Assignees** as well as **Reviewers** section.


## After PR submission
1. Keep track of **Assignees** section and reply to comments in the PR itself.
2. PR should not be merged if there are any requested changes.
3. Make sure to resolve all conversations with appropriate comments and replies, **no** conversation should be resolved without having a proper end to the conversation**.
4. Once everything is ready for merge, add a high-level comment regarding merge description and merge the PR.
