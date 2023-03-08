
_For a detailed explanation of the fork-and-clone process, please see this [GitHub help page](https://help.github.com/en/github/getting-started-with-github/fork-a-repo#platform-linux)._

## Fork and clone the Oppia Android repository

To make code changes, please follow the following instructions
- [Using the terminal](https://gist.github.com/MaskedCarrot/ea0933311b95a108f99e3c6c106ea101#using-the-terminal)
- [Using android studio's UI based github workflow](https://gist.github.com/MaskedCarrot/ea0933311b95a108f99e3c6c106ea101#using-android-studios-ui-based-github-workflow)

### Using the terminal

1. Make sure that you are in the `opensource/` folder on your local machine.

2. Click on the "Fork" button on the top-right corner (at the same level as the oppia/oppia-android repository name):
![Oppia-Android Fork](https://user-images.githubusercontent.com/9396084/71397560-eed7b600-2643-11ea-854c-ea1d57df497d.png)

3. You can now see Oppia-Android under [your repositories](https://github.com/). It will be marked as forked from `oppia/oppia-android`
![Oppia-Android Origin Repo](https://user-images.githubusercontent.com/9396084/71338640-b0c09080-2576-11ea-8dc3-3d4a0ef59877.png)

4. Clone this repository to your local computer by running `git clone https://github.com/USERNAME/oppia-android.git` in a terminal.

5. To keep your local repository, forked repository and main oppia-android repository in sync, configure your remote repositories by running the following two commands in a terminal:
   - `git remote -v` (this lists your current remote repositories)
   - `git remote add upstream https://github.com/oppia/oppia-android` (this adds oppia/oppia-android as an upstream repo)

### Using android studio's UI based GitHub workflow
1. Navigate to your fork, e.g. ``https://github.com/<your_username>/oppia-android``.
Click on **Clone or download** and copy the link (the URL will look different since you should be using your fork, **not** https://github.com/oppia/oppia-android).

<img width="1680" alt="1" src="https://user-images.githubusercontent.com/53938155/136960580-446eecd5-2903-4a3d-99bb-d4df18eee3e0.png">

2. Now, go to **Android Studio**>**File**>**New**>**Project from Version Control**>**Git**

<img width="1680" alt="2" src="https://user-images.githubusercontent.com/54615666/72599230-51685d00-3937-11ea-8020-485cd0111566.png">

3. Log in GitHub with your credentials.

4. Paste the **URL** and click on **Clone** button.
Wait for a few minutes until Gradle build completes. 

<img width="1680" alt="4" src="https://user-images.githubusercontent.com/54615666/72599231-51685d00-3937-11ea-9850-796700298af2.png">