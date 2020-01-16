## How to clone the Oppia Android repository from Android Studio.
Go to _https://github.com/oppia/oppia-android_
Clone or download(Copy the link)>
<img width="1680" alt="1" src="https://user-images.githubusercontent.com/54615666/72495729-8e582500-384d-11ea-8cdb-a00f8af03294.png">

Now ,go to _Android Studio>File>New> Project from Version Control >Git_


<img width="1680" alt="2" src="https://user-images.githubusercontent.com/54615666/72495730-8e582500-384d-11ea-8c1a-a515c48832d6.png">
Login to gitHub with your credentials.
Paste the URL>_Clone_ 
Wait for a few minutes that gradle build completes. 

<img width="1680" alt="4" src="https://user-images.githubusercontent.com/54615666/72495731-8e582500-384d-11ea-8a89-8ee7a0ba7c00.png">

## Create New Branch

Now your android studio project is ready to start your work.
Right side bottom of your android studio screen you will find you cloned branch name "develop".
develop>New Branch>fill your new branch name>OK

<img width="1680" alt="10" src="https://user-images.githubusercontent.com/54615666/72495739-8f895200-384d-11ea-9d66-2b54f94fc39f.png">
<img width="1680" alt="12" src="https://user-images.githubusercontent.com/54615666/72495741-9021e880-384d-11ea-8291-12e471a5f7ca.png">


If you want to go back to develop or any branch/check out any branch you may righ click and find options for that
<img width="1680" alt="11" src="https://user-images.githubusercontent.com/54615666/72495740-8f895200-384d-11ea-8fb4-3a2aa2a533f3.png">
Now when you create new activity it will show a dialog to add new files to Git(new files will be unversioned unless you allow this i.e. shown in reddish brown color file name.)Select files >Add
<img width="1680" alt="5" src="https://user-images.githubusercontent.com/54615666/72495732-8ef0bb80-384d-11ea-89f4-f24d2d647783.png">
New files will be shown in green color and modified file name will be displayed in blue color in project window(select file/folder/directory Ctrl+Alt|+A to manually version an unversioned file/files)
<img width="1680" alt="8" src="https://user-images.githubusercontent.com/54615666/72495735-8f895200-384d-11ea-877c-fa68a4fa4876.png">

click on the green tick symbol to commit your files 
<img width="1680" alt="9" src="https://user-images.githubusercontent.com/54615666/72495736-8f895200-384d-11ea-82b5-5cc75107b057.png">
Now click on commit.(This will save the reference of your changes for pushing to git)
If there are errors or warnings review the code change and fix before commiting.

<img width="1680" alt="13" src="https://user-images.githubusercontent.com/54615666/72495742-9021e880-384d-11ea-86a0-53110fa00529.png">

## Push Changes

Now we need to Push the code changes to local branch(orgin) and upstream(remote)
Right click app>Git>Repository >Push
<img width="1680" alt="15" src="https://user-images.githubusercontent.com/54615666/72495744-9021e880-384d-11ea-9094-32746401595f.png">
Your new branch need to be added to both local(orgin) and upstream(remote).
Once you push a branch to git you wont be able to rename it so be sure about the naming before pushing.
<img width="1680" alt="16" src="https://user-images.githubusercontent.com/54615666/72495745-90ba7f00-384d-11ea-83a1-3760f18d1b39.png">
## Pull changes from Develop

Once you push your new branch to develop you are ready to merge new changes from remote(upstream).Make sure your new branch's base branch is develop and the new branch is up-to-date with changes in develop.
<img width="1680" alt="17" src="https://user-images.githubusercontent.com/54615666/72495746-90ba7f00-384d-11ea-971f-4950e5b3dd8e.png">
If you are not getting the below options of selecting upstream.Please login to your git hub account 
<img width="1680" alt="18" src="https://user-images.githubusercontent.com/54615666/72495747-90ba7f00-384d-11ea-9c63-ec015c37e0f8.png">
For login Go to Android Studio>Preference>Version Control >Github>Add Account
<img width="1680" alt="19" src="https://user-images.githubusercontent.com/54615666/72495748-91531580-384d-11ea-8bfc-028bacd847c9.png">

Fill all the credentials asked and login.
Now Click VCS>Git>Fetch 
<img width="1680" alt="20" src="https://user-images.githubusercontent.com/54615666/72495749-91531580-384d-11ea-9d79-163ebb3b73e3.png">
<img width="1680" alt="21" src="https://user-images.githubusercontent.com/54615666/72495750-91531580-384d-11ea-977b-eaba9c209d80.png">
Now select the upstream to pull changes from develop 
Select upstream in pull changes dialog>select branch develop> Pull
you will find all the new changes pulled from develop in version control update info. 
<img width="1680" alt="23" src="https://user-images.githubusercontent.com/54615666/72498473-7ab0bc80-3855-11ea-823d-a99e10d157a1.png">
## Merge and resolve conflict 

select remote branch as upstream>select develop branch>select your branch name> pull
<img width="1680" alt="25" src="https://user-images.githubusercontent.com/54615666/72498476-7b495300-3855-11ea-93cb-5389101cb168.png">
<img width="1680" alt="27" src="https://user-images.githubusercontent.com/54615666/72498479-7b495300-3855-11ea-8d2f-0a89f3beac08.png">

You will get the dialog of conflict files.You need to resolve the conflicts by merging the file changes.
select file>merge
<img width="1680" alt="28" src="https://user-images.githubusercontent.com/54615666/72498480-7be1e980-3855-11ea-8c7d-cb02efd22e07.png">

Now the local changes ,Result , and remote changes are displayed should merge these changes.
<img width="1680" alt="29" src="https://user-images.githubusercontent.com/54615666/72498481-7be1e980-3855-11ea-8905-c8041cceb027.png">
Once all conflicts are resolved you will get a popup like this "All changes have been processed"
Now click on Apply
<img width="1680" alt="30" src="https://user-images.githubusercontent.com/54615666/72498482-7be1e980-3855-11ea-95ff-03fd97c30e2e.png">
Now you will find the merged file changes locally in your android studio code.
<img width="1680" alt="31" src="https://user-images.githubusercontent.com/54615666/72498484-7c7a8000-3855-11ea-8ae8-13cdcc1cf48e.png">

