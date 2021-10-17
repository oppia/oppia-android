**Important:** Please read the [Oppia Android coding style guide](https://github.com/oppia/oppia-android/wiki/Coding-style-guide) before making any code changes.

**Working on your first Pull Request?** You can learn how from this free series: [How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github).

*If your change isn't trivial, please [talk to us](https://gitter.im/oppia/oppia-android) before you start working on it -- this helps avoid duplication of effort, and allows us to offer advice and suggestions. For larger changes, it may be better to first create a short doc outlining a suggested implementation plan, and send it to the Android dev team for feedback.*

The following instructions describe how to make a one-off code change using a feature branch. (In case you're interested, we mainly use the [Gitflow workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).) Please follow them carefully, otherwise your code review may be delayed.

You might also find this reference for the [Android Studio UI-based Github workflow](https://github.com/oppia/oppia-android/wiki/Android-Studio-UI-based-Github-workflow) helpful.

1. **Choose a descriptive branch name.** It should be lowercase and hyphen-separated, such as `splash-screen`. Also, it shouldn't start with `hotfix` or `release`.
2. **Before coding anything, create a new branch with this name, starting from 'develop'.** I.e., run:

    ```
      git fetch upstream
      git checkout develop
      git merge upstream/develop
      git checkout -b your-branch-name
    ```

3. **Make commit(s) to your feature branch.** Each commit should be self-contained and have a descriptive commit message that helps other developers understand why the changes were made. However, **do not write "Fix #ISSUE_NUMBER"** (e.g. Fix #99999) in your commit messages, as this will cause Github to close the original issue automatically. You can rename your commit messages using `git commit --amend`.

    * Before making the commit, do some sanity-checks:
       * Start up a local instance of Oppia and do some manual testing in order to check that you haven't broken anything!
       * Ensure that your code follows the [style rules](https://github.com/oppia/oppia-android/wiki/Coding-style-guide) and that it is well-tested.
       * Ensure that the code has no [lint errors](https://github.com/oppia/oppia-android/wiki/Ktlint-Guide) and passes all automated tests (which will automatically run when you create your pull request).
      * Use a tool like `git diff` or `meld` to check that the changes you've made are exactly what you want them to be, and that you haven't left in anything spurious. Make sure to do this _before_ you push.

    * To actually make the commit and push it to your GitHub fork, run:

        ```
          git commit -a -m "{{YOUR COMMIT MESSAGE HERE}}"
          git push origin {{YOUR BRANCH NAME}}
        ```

      Make sure to do this from the command line (and not GitHub's Desktop client), since this also runs some important presubmit checks before your code gets uploaded to GitHub. **If any of these checks fail, the push will be interrupted**. If this happens, fix the issues that the tests tell you about and **repeat the instructions above** ('commit' and then 'push').

4. **When your feature is ready to merge, create a pull request.**
    * Go to your fork on GitHub, select your branch from the dropdown menu, and click "pull request". Ensure that the 'base' repository is the main oppia repo (not your fork) and that the 'base' branch is 'develop'.
    * Add a descriptive title explaining the purpose of the PR (e.g. "Fix issue #bugnum: add a warning when the user leaves a page in the middle of an exploration.").
       * If the PR resolves an issue on the issue tracker, the title must start with **"Fix #bugnum: "**. This will be the case for most PRs.
       * However, if your PR fixes part of a bigger issue (e.g. the first-contributor-issues listed above), please use **"Fix part of #bugnum: "** instead. Otherwise, GitHub will close the entire issue automatically when your PR is merged.
    * Fill out the PR checklist (this shows up in the body of the PR when you create it), ensuring that your PR description includes the issue number (e.g. "This PR fixes issue #bugnum" or "This PR fixes part of issue #bugnum").
    * Click "Create pull request", then **immediately** check the "Files changed" tab on your PR on GitHub and read it carefully to make sure that the changes are correct (e.g., that you haven't left out important files that should be part of the PR. (If not, please fix this by making additional commits, or by closing this PR and submitting a new one, before requesting a review.) This is a good way to catch obvious errors that would otherwise lead to delays in the review process.
    * Request a review from the issue's "owner" **and** also set them as the PR assignee.
    * Leave a top-level comment on your PR saying "@{{reviewer}} PTAL", where {{reviewer}} is the GitHub username of your reviewer. ("PTAL" means "Please take a look".)
    * Then, wait for your code to get reviewed! While you're doing so, it's totally fine to start work on a new PR if you like. Just make sure to **checkout the develop branch** and sync to HEAD before you check out a new branch, so that each of your feature branches is based off the main trunk.

5. #### **Address review comments until all reviewers give LGTM ('looks good to me').** 
    * When your reviewer has reviewed the code, you'll get an email. You'll need to respond in two ways:
       * Make a new commit addressing the comments you agree with, and push it to the same branch. (Continue to use descriptive commit messages. If your commit addresses lots of disparate review comments, it's fine to refer to the original commit message and add something like "(address review comments)".)
          * **Always make commits locally, and then push to GitHub.** Don't make changes using the online GitHub editor -- this bypasses lint/presubmit checks, and will cause the code on GitHub to diverge from the code on your machine.
          * **Never force-push changes to GitHub, especially after reviews have started.** This will delay your review, because it overwrites history on GitHub and makes the incremental changes harder to review.
       * In addition, reply to each comment via the Files Changed tab, choosing the "Start a review" option for the first comment. Each reply should be either "Done" or a response explaining why the corresponding suggestion wasn't implemented. When you've responded to all comments, submit the review to add all your messages to the main thread. All comments must be responded to and resolved before LGTM can be given.
    * Resolve any merge conflicts that arise. To resolve conflicts between 'new-branch-name' (in your fork) and 'develop' (in the oppia repository), run:

      ```
        git checkout new-branch-name
        git fetch upstream
        git merge upstream/develop
        ...[fix the conflicts -- see https://help.github.com/articles/resolving-a-merge-conflict-using-the-command-line]...
        ...[make sure the tests pass before committing]...
        git commit -a
        git push origin new-branch-name
      ```
    * Once you've finished addressing everything, and would like the reviewer to take another look:
       * Start a dev server in order to make sure that everything still works.
       * Check that the changes in the "Files Changed" tab are what you intend them to be.
       * **Write a top-level comment** explicitly asking the reviewer to take another look (e.g. "@XXX PTAL"), and set them as the assignee for the PR.
    * At the end, the reviewer will merge the pull request.

6. **Tidy up!** After the PR status has changed to "Merged", delete the feature branch from both your local clone and the GitHub repository:

     ```
       git branch -D new-branch-name
       git push origin --delete new-branch-name
     ```

7. **Celebrate.** Congratulations, you have contributed to the Oppia Android project!
