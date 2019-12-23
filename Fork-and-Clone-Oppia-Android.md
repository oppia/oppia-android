For a detailed explanation for fork and clone, you can go to github help page

Make sure you are in `opensource/` folder. Now follow the steps below :

* Click on the `fork` button. It is placed on the top right corner opposite to repository name `oppia/oppia-android`.
![Oppia-Android Fork](https://user-images.githubusercontent.com/9396084/71338568-663f1400-2576-11ea-9893-8d616f65368f.png)

* You can now see Oppia-Android under your repositories. It will be marked as forked from `oppia/oppia-android`
![Oppia-Android Origin Repo](https://user-images.githubusercontent.com/9396084/71338640-b0c09080-2576-11ea-8dc3-3d4a0ef59877.png)

* Let's clone this repository to your local computer using git clone `https://github.com/USERNAME/oppia-android.git`

* We will configure remote repositories to keep your local repository, forked repository and main oppia-android repository in sync. You can check your current remote repositories by typing `git remote -v`.

* Type the following to add oppia as upstream `git remote add upstream https://github.com/oppia/oppia-android`.

* And we are done! You can access main oppia repository using `git fetch upstream` and access your forked version using `git fetch origin`.
