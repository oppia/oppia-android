The following instructions will help you submit a PR effectively and in a clean way. Before going through this document, make sure you look at the instructions on the [wiki home page](https://github.com/oppia/oppia-android/wiki).

## Important Points

1. **UI related issue/bug fix**: If your PR introduces changes to the UI/UX part of the app, do the following:
    - Include "before" and "after" screenshots (and possibly a video if needed).
    - Test the UI/UX with [Accessibility Scanner](https://support.google.com/accessibility/android/answer/6376570?hl=en). _(Tip: All your dimensions should be in multiples of 4dp.)_
2. **Bug fixes**: While fixing an issue labelled **Bug**, make sure to write test cases which actually catch that bug.
3. **Self Review**: Always self review your PR first before assigning it to anyone else, so that you can fix nit changes at very early stage. This makes the review process faster.

## Clarification regarding **Assignees** and **Reviewers** section.
1. **Reviewers**: In this section, add one or more people who should review this PR. Once this section is filled out, it generally should not change throughout the timeline of the PR.
2. **Assignees**: The Assignees field indicates the person(s) who the PR is currently blocked on. More specifically:
   - Initially, when the PR is submitted, the **Assignees** and **Reviewers** sections should be the same. 
   - Once a reviewer has reviewed the PR, they should de-assign themselves and assign it back to the PR author.
   - Similarly, once the author has made the requested changes, they should assign it back to the appropriate reviewer and de-assign themselves.

## Checklist before submitting a PR

Note: This checklist should be embedded in your every PR explanation with appropriate ticks. Please tick the relevant boxes by putting an "x" in them
- [ ] The PR title starts with "Fix #bugnum: ", followed by a short, clear summary of the changes. (If this PR fixes part of an issue, prefix the title with "Fix part of #bugnum: ...".)
- [ ] The PR explanation includes the words "Fixes #bugnum: ..." (or "Fixes part of #bugnum" if the PR only partially fixes an issue).
- [ ] The PR follows the [style guide](https://github.com/oppia/oppia-android/wiki/Coding-style-guide).
- [ ] The PR does not contain any unnecessary auto-generated code or unnecessary files from Android Studio.
- [ ] The PR is made from a branch that's **not** called "develop".
- [ ] The PR is made from a branch that is up-to-date with "develop".
- [ ] The PR's branch is based on "develop" and **not** on any other branch.
- [ ] The PR is **assigned** to an appropriate reviewer in **Assignees** as well as **Reviewers** section.


## Tips for getting your PR merged after submission
1. Keep track of **Assignees** section and reply to comments in the PR itself.
2. PRs should not be merged if there are any requested changes, or if there are any unresolved conversation threads.
3. Do not resolve a conversation if you didn't open it, unless the author explicitly says that you can. You should re-assign the PR to the author once you address all of the comments so that they can verify & mark the conversations as resolved.
4. Make sure that the assignee list always reflects the contributors who have actionable items left on the PR (either to make requested changes, merge the PR, review it, or resolve open comment threads). Note that it's perfectly fine if sometimes both the author and reviewer of a PR are both assigned to it (one reviewer may have finished a review pass and requested changes--in this situation both another reviewer and author might have actionable work items to complete).
5. Address all comments before sending a PR back to a reviewer. 'Address' can mean implementing a code change, asking a clarifying question, or providing an explanation why you don't think a suggestion should implemented. Make sure each comment thread has at a reply from you before sending the PR back to that reviewer.
6. Once the PR is ready to merge, add a top-level comment confirming the merge decision, and merge the PR. If any issues need to be filed subsequently, file them and refer to them in the PR via a comment.