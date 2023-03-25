## Table of contents

* [Contributing to the wiki](#contributing-to-the-wiki)
   * [Opening a pull request](#opening-a-pull-request)
   * [Failed to push changes to wiki upon PR merge.](#failed-to-push-changes-to-wiki-upon-pr-merge)
* [Implementation details](#implementation-details)
   * [Wiki architecture](#wiki-architecture)
   * [When you make changes through the web interface](#when-you-make-changes-through-the-web-interface)
   

## Contributing to the wiki

If you notice something about the wiki that could be improved, please let us know! There are a couple of ways you can do so:

* If the improvement you have in mind is big, you want feedback before you start working on it, or you don't have time to make the change yourself, [open an issue](https://github.com/oppia/oppia-android/issues/new) in the oppia-android repository.
* If you can make the change yourself, see the instructions below for opening a pull request (PR).

### Opening a pull request

For your *first contribution* to oppia-android wiki, you'll need to set up your repository (you only have to do this once). Please follow the instructions in the [Installing Oppia Android](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android) to set up your repository.

Then for every new contribution (including your first), you should follow these steps:

1. Checkout the `develop` branch and pull in the latest changes from upstream:

   ```console
   git checkout develop
   git pull upstream develop
   ```

2. Create a new branch for your changes:

   ```console
   git checkout -b {{branch name}}
   ```

3. Make your changes, being sure to follow our [style guide](https://github.com/oppia/oppia/wiki/Wiki-style-guide). You can use whatever text editor you prefer for this.

4. Commit your changes. You can make multiple commits as you write if you prefer.

   ```console
   git add {{paths to the files you changed}}
   git commit
   ```

5. Push your changes to your fork (called `origin` by default):

   ```console
   git push -u origin {{branch name}}
   ```

6. [Open a pull request.](https://github.com/oppia/oppia-android/compare) Remember to click the "compare across forks" link since your changes are on a different fork than oppia-android. The base for your PR should be the `develop` branch on the `oppia/oppia-android` repository.

7. Wait for the reviewers to review your PR.

8. Once the reviewers leaves comments, respond to them and make changes as needed. Please do not resolve review threads--let the reviewer do that. Repeat as needed until reviewers approve.

9. Once reviewers have approved, they will merge your PR, and your changes will be automatically deployed to the Oppia wiki. Congratulations! :tada:

### Failed to push changes to wiki upon PR merge.

If the deployment of changes to the wiki following the merging of a pull request was unsuccessful, you can re-run the failed "Deploy to wiki" workflow to deploy to the wiki by following these steps:

1. Navigate to the Oppia Android wiki repository's Actions tab.
2. Select the "Deploy to wiki" workflow.
3. Refer to these [instructions](https://docs.github.com/en/actions/managing-workflow-runs/re-running-workflows-and-jobs#re-running-all-the-jobs-in-a-workflow) to re-run the workflow.

## Implementation details

### Wiki architecture

Our wiki consists of the following components:

* The `oppia/oppia-android.wiki` git repository automatically created by GitHub to hold the wiki viewable at https://github.com/oppia/oppia-android/wiki. This is our deployment repository where we put wiki source files to be viewed by the community.
* The `/wiki` folder in the `oppia/oppia-android` repository is our source repository where we store and edit the wiki source files. We consider this to be the single source of truth for our wiki.
* A [`wiki.yml`](https://github.com/oppia/oppia-android/blob/develop/.github/workflows/wiki.yml) workflow in oppia-android deploys any new commits in oppia-android to the deployment repository. The workflow is activated whenever a pull request that modifies any file in the `/wiki` directory is pushed to the `develop` branch.

### When you make changes through the web interface

If you change the wiki through the web interface at https://github.com/oppia/oppia-android/wiki, the [`wiki.yml`](https://github.com/oppia/oppia-android/blob/develop/.github/workflows/wiki.yml) workflow will be triggered and the wiki will be reset to by pushing the files from the `/wiki` folder to the `oppia/oppia-android.wiki` git repository.
> **Note**  
> The `/wiki` folder in the `oppia/oppia-android` repository is the single source of truth for our wiki.

## Tips and Tricks

* ### How can I see markdown (wiki) preview in Android Studio?

1. Start the IDE, use Help -> Find Action, type `Choose Boot Java Runtime for the IDE`, press Enter.
2. Select the newest version with JCEF to install.
3. Click the OK button and wait until the file with JetBrains Runtime is downloaded and configured.
4. Restart the IDE and Enjoy.

#### Demo Screenshot
<img width="1074" alt="Screenshot wiki preview" src="https://user-images.githubusercontent.com/76530270/227737306-0bf3d5ef-ddcb-4886-b65b-a9c1ce1b4069.gif">
