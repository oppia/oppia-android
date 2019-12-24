_For a detailed explanation of the fork-and-clone process, please see this [GitHub help page](https://help.github.com/en/github/getting-started-with-github/fork-a-repo#platform-linux)._

## How to fork and clone the Oppia Android repository

1. Make sure that you are in the `opensource/` folder on your local machine.

2. Click on the "Fork" button on the top-right corner (at the same level as the oppia/oppia-android repository name):
![Oppia-Android Fork](https://user-images.githubusercontent.com/9396084/71338568-663f1400-2576-11ea-9893-8d616f65368f.png)

3. You can now see Oppia-Android under your repositories. It will be marked as forked from `oppia/oppia-android`
![Oppia-Android Origin Repo](https://user-images.githubusercontent.com/9396084/71338640-b0c09080-2576-11ea-8dc3-3d4a0ef59877.png)

4. Clone this repository to your local computer by running `git clone https://github.com/USERNAME/oppia-android.git` in a terminal.

5. To keep your local repository, forked repository and main oppia-android repository in sync, configure your remote repositories by running the following two commands in a terminal:
   - `git remote -v` (this lists your current remote repositories)
   - `git remote add upstream https://github.com/oppia/oppia-android` (this adds oppia/oppia-android as an upstream repo)

## Updating your local repository

You can download the latest contents of the main Oppia Android repository using `git fetch upstream`, and you can also access your forked version using `git fetch origin`. (Usually, you'll want to do the former.)
