_For a detailed explanation of the fork-and-clone process, please see this [GitHub help page](https://help.github.com/en/github/getting-started-with-github/fork-a-repo#platform-linux)._

## How to fork and clone the Oppia Android repository

1. Make sure that you are in the `opensource/` folder on your local machine.

2. Click on the "Fork" button on the top-right corner (at the same level as the oppia/oppia-android repository name):
![Oppia-Android Fork](https://user-images.githubusercontent.com/9396084/71397560-eed7b600-2643-11ea-854c-ea1d57df497d.png)

3. You can now see Oppia-Android under [your repositories](https://github.com/). It will be marked as forked from `oppia/oppia-android`
![Oppia-Android Origin Repo](https://user-images.githubusercontent.com/9396084/71338640-b0c09080-2576-11ea-8dc3-3d4a0ef59877.png)

4. Clone this repository to your local computer by running `git clone https://github.com/USERNAME/oppia-android.git` in a terminal.

5. To keep your local repository, forked repository and main oppia-android repository in sync, configure your remote repositories by running the following two commands in a terminal:
   - `git remote -v` (this lists your current remote repositories)
   - `git remote add upstream https://github.com/oppia/oppia-android` (this adds oppia/oppia-android as an upstream repo)

## Updating your local repository

If you need to update your local branch with the latest changes in the main Oppia Android repository on develop (e.g. in cases when your PR is showing extra commits that you didn't create), follow the steps listed [here](https://docs.github.com/en/free-pro-team@latest/github/collaborating-with-issues-and-pull-requests/syncing-a-fork). Note that Oppia Android uses 'develop' not 'main' for its mainline branch.