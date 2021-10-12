# Clone Oppia-Android using Android Studio
Navigate to your fork, e.g. ``https://github.com/<your_username>/oppia-android``.
Click on **Clone or download** and copy the link (the URL will look different since you should be using your fork, **not** https://github.com/oppia/oppia-android).

<img width="1680" alt="1" src="https://user-images.githubusercontent.com/53938155/136960580-446eecd5-2903-4a3d-99bb-d4df18eee3e0.png">

Now, go to **Android Studio**>**File**>**New**>**Project from Version Control**>**Git**


<img width="1680" alt="2" src="https://user-images.githubusercontent.com/54615666/72599230-51685d00-3937-11ea-8020-485cd0111566.png">
Log in GitHub with your credentials.

Paste the **URL** and click on **Clone** button.
Wait for a few minutes until Gradle build completes. 

<img width="1680" alt="4" src="https://user-images.githubusercontent.com/54615666/72599231-51685d00-3937-11ea-9850-796700298af2.png">

# Create New Branch

Now the local copy of Oppia-Android is ready on your Android Studio
Right side bottom of your android studio screen you will find a **develop** branch.
Click on **develop** > **New Branch** > _Enter your new branch name_ > **OK**

<img width="1680" alt="10" src="https://user-images.githubusercontent.com/54615666/72599235-5200f380-3937-11ea-979b-cf4ef2d772dc.png">
<img width="1680" alt="12" src="https://user-images.githubusercontent.com/54615666/72599237-52998a00-3937-11ea-9c89-48367bcbfaaa.png">


If you want to go back to "develop" or any other branch/"check-out any branch" you may right-click and find options for that

<img width="1675" alt="11" src="https://user-images.githubusercontent.com/54615666/72599236-5200f380-3937-11ea-9ef4-e29ef9ef974d.png">

Now, when you create a new activity, it will be unversioned and therefore displayed with a reddish-brown colour file name. To add the files to Git/version Select files and click the "Add" button.

<img width="1675" alt="5" src="https://user-images.githubusercontent.com/54615666/72599232-51685d00-3937-11ea-8efc-ca02a1840ede.png">

New/modified files will be displayed in green /blue colour respectively in the project window. (select file/folder/directory Ctrl+Alt|+A to manually version an unversioned file/files)

<img width="1674" alt="8" src="https://user-images.githubusercontent.com/54615666/72599233-5200f380-3937-11ea-9303-f0c4b17aadf0.png">

Click on the green tick symbol to commit your files. 

<img width="1676" alt="9" src="https://user-images.githubusercontent.com/54615666/72599234-5200f380-3937-11ea-8ae6-c73b27e85d51.png">

Now click on commit(This will save the reference of your changes for pushing to Git).
If there are errors or warnings, review the code change and fix them before committing.

<img width="1674" alt="13" src="https://user-images.githubusercontent.com/54615666/72599238-52998a00-3937-11ea-8392-45694c3752ee.png">

## Push Changes

Now we need to Push the code changes to local branch(origin) and upstream(remote)
Right click **app > Git > Repository > Push**

<img width="1680" alt="15" src="https://user-images.githubusercontent.com/54615666/72599239-52998a00-3937-11ea-8a0c-9c4ae1536299.png">

Your new branch needs to be added to both origin and upstream remote.
Once you push a branch to Git you wont be able to rename it so, be sure about the naming before pushing.

<img width="1676" alt="16" src="https://user-images.githubusercontent.com/54615666/72599241-53322080-3937-11ea-8ef6-3253acd58215.png">

### How to Fix Push Failures?

If you are using Command line to push and if you have any file formatting issues, the code will not get pushed and it will show you an error like this:

<img src="https://i.ibb.co/MS2VgTb/Screenshot-2021-02-10-at-10-00-04.png" />

<br></br>
If you are using Android Studio VCS>Git to push the code, you will get the failure result like this:

<img src="https://i.ibb.co/dW38jz1/Screenshot-2021-02-10-at-10-07-42.png" />

While using Android Studio VCS>Git you will not be able to see much detail on what the failure is, but you can try running below commands to checks the failure details:

If you are using Windows, please use [Git Bash Command Line](https://gitforwindows.org/) to run these scripts. 

If you have failure related to kotlin, then use this script to get a detailed failure result:
```
scripts/ktlint_lint_check.sh
```

If you have failure related to checkstyle/java, then use this script to get a detailed failure result:
```
scripts/checkstyle_lint_check.sh
```
Refer [comment](https://github.com/oppia/oppia-android/pull/2101#issuecomment-759331732) here for error related to `google_checks.xml` config file.

## Pull changes from Develop

Once you push your new branch to "develop" you are ready to merge new changes from remote(upstream). Make sure the new branch's base branch is "develop" and the new branch is up-to-date with changes in "develop".

<img width="1674" alt="17" src="https://user-images.githubusercontent.com/54615666/72599242-53322080-3937-11ea-93cb-c030747fc021.png">

If you are not getting the below options of selecting upstream.Please log in your GitHub account.

<img width="1680" alt="18" src="https://user-images.githubusercontent.com/54615666/72599243-53322080-3937-11ea-9c78-99de27c82911.png">

For log in Go to **Android Studio>Preference>Version Control >GitHub>Add Account**

<img width="1672" alt="19" src="https://user-images.githubusercontent.com/54615666/72599244-53cab700-3937-11ea-959b-142f079bdc75.png">

Fill all the credentials asked and log in.
Now Click **VCS>Git>Fetch** 

<img width="1680" alt="20" src="https://user-images.githubusercontent.com/54615666/72599248-54634d80-3937-11ea-916f-fc3cdce0476c.png">

<img width="1672" alt="21" src="https://user-images.githubusercontent.com/54615666/72599249-54634d80-3937-11ea-8252-eba4ecc86e3c.png">

Now select the upstream to pull changes from "develop" 
Select upstream in pull changes **dialog>select branch develop>Pull**
you will find all the new changes pulled from "develop" in version control update info. 

<img width="1680" alt="23" src="https://user-images.githubusercontent.com/54615666/72599250-54634d80-3937-11ea-89ad-a24ce196d233.png">

## Merge and resolve conflict
Follow these steps mentioned below (Note: The image is just for reference in this case) 
* Select remote branch as **upstream**
* Select **develop** branch
<img width="1672" alt="25" src="https://user-images.githubusercontent.com/54615666/72599251-54fbe400-3937-11ea-8a72-75edc4ac8e24.png">

* Select **your_branch_name**
* Click on **Pull**

<img width="1680" alt="27" src="https://user-images.githubusercontent.com/54615666/72599252-54fbe400-3937-11ea-9b89-8c1806472ce9.png">

You will get the dialog of conflicted files. You need to resolve the conflicts by merging the file changes.
select **file>merge**

<img width="1680" alt="28" src="https://user-images.githubusercontent.com/54615666/72599253-54fbe400-3937-11ea-882d-be4cb88b79f7.png">

Now the local changes, result and remote changes are displayed, should merge changes.

<img width="1680" alt="29" src="https://user-images.githubusercontent.com/54615666/72599254-55947a80-3937-11ea-9265-8cff69069b4d.png">

Once all conflicts are resolved, "All changes have been processed" pop up appears. Now click on **Apply**

<img width="1680" alt="30" src="https://user-images.githubusercontent.com/54615666/72599255-55947a80-3937-11ea-8b9a-5ae661fb5df9.png">

Now you will find the merged file changes locally in your Android Studio code.

<img width="1676" alt="31" src="https://user-images.githubusercontent.com/54615666/72599257-55947a80-3937-11ea-9b27-ac0169d4f192.png">

