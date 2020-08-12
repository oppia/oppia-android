When you find yourself in a situation where something seems newly broken in the app, it's important to:
1. Isolate the problem & [file an issue](https://github.com/oppia/oppia-android/issues/new?assignees=&labels=Type%3A+Improvement%2C+Status%3A+Not+started&template=feature_request.md&title=) for it
2. Identify the commit which introduced the regression (see below section)
3. [Revert the PR](https://github.blog/2014-06-24-introducing-the-revert-button/) associated with the commit & send it as a PR for review (the bug you filed in (1) should be marked as fixed by this PR)
4. Once the revert commit is submitted, reopen any issues fixed by the original PR that was reverted

If you get stuck along the way, please mention in the issue that you filed in (1) that you are unable to locate commit introducing the regression.

## Why do we revert PRs rather than just fix them?

Leaving problems checked in can result in the following two situations:
1. As the known issues are fixed, others go unnoticed or are discovered later (potentially by users)
2. The original commit may become non-revertible without manual conflict resolution as more changes are checked in on top of that

For this reason, the team's policy is to quickly revert regressions when they're found. This immediately unblocks team members and provides time for the original PR author to more carefully investigate the underlying issue & potential edge cases.

## How to find the offending commit

1. Always checkout an unchanged, up-to-date copy of develop & verify that the issue is still occurring. Note this commit hash.
    - If you can't repro the issue, double check that it isn't flaky (try a few times). If you still can't, it's likely the issue has been resolved. Please still file an issue and leave notes for what you tried so that the team can triage accordingly.
2. Go back to an earlier commit on develop that doesn't repro this issue (if you're unsure, try something at the bottom of [this page](https://github.com/oppia/oppia-android/commits/develop) or earlier, if the regression was introduced a while ago).
3. Perform a binary search until you find the commit that introduced the issue. Specifically:
    - Open up a text editor and list all of the commit hashes on develop between the one that you found is reproing the regression and the commit not reproing the issue
    - Pick the middle hash of the list, check out that commit, and verify whether the issue regresses. If it does, this becomes your new lower bound. If it doesn't, this becomes your new upper bound. Like standard [binary search](https://en.wikipedia.org/wiki/Binary_search_algorithm), the number of commits you're considering is cut in half during each check.

## What do I do if I caused a regression?

If it hasn't been reverted, please follow the steps above to revert. Once your PR is reverted, follow these steps:
1. Ensure you can repro the issue fully. If you can't, talk to the original author to better understand how to repro the issue.
2. Make sure you fully understand _why_ your change caused the observed regression(s)--without this step, no progress can actually be made. Remember: fix issues not symptoms. We don't want the observed problem to just go away, we want the underlying root cause to be understood & addressed.
3. Once you understand the root cause, think through potential edge cases that may break other aspects of the app. It's quite likely that the observed regression was just one of several broken behaviors caused by the underlying issue.
4. Turn the behaviors from (3) into tests. Verify that these tests fail without a fix, and pass with a fix to the original regression.
5. Submit a new PR with your original changes + the fixes and tests from (4). Make sure to detail the investigation in this new PR, and reference the original issue that tracked the regression.

Finally, be aware that everyone causes regressions. While we have a lot checks in place to try and avoid regressions from being checked in, they _will_ still happen. Focus on learning from the situation rather focusing on the fact that a regression happened.