
## Problem Statement
Android Code Style improves productivity. When working alone on your apps you don’t have problems with your manual code formatting. Whereas while multiple developers, with multiple opinions, multiple operating systems and multiple screen sizes work on the same code base, if you don’t let a tool format your code, discussions about the formatting may arise or worse, different formatting and sorting increases diffs and can cause completely unnecessary merge conflicts. This will slow down development for no reason.

It’s professional to have common formatting throughout the project. It helps the project to be ready to scale to two or more developers.
## Steps to do
* **Android Studio**>**Preferences(in mac)**
**Open Settings** > **Editor** >**Code Style** > **XML** > **Others**
![8](https://user-images.githubusercontent.com/54615666/80364656-ea4a3500-88a3-11ea-9b7b-ab804e8bbf55.png)
![pasted image 0](https://user-images.githubusercontent.com/54615666/80364659-eae2cb80-88a3-11ea-819a-f1d96beaea8b.png)
* Keep blank lines: 2  change it to 0

* **Open Settings** > **Editor** > **Code Style** > **XML** > **Android** Click **Set from...** text.
* Select **Predefined Styles** > **Android** Click **Apply button**

![2](https://user-images.githubusercontent.com/54615666/80364643-e5858100-88a3-11ea-8a15-7a6e9ecf785c.png)

## Final Settings

![3](https://user-images.githubusercontent.com/54615666/80364649-e7e7db00-88a3-11ea-97c4-46b3bc2096eb.png)
![4](https://user-images.githubusercontent.com/54615666/80364650-e8807180-88a3-11ea-818b-effa8e4e2897.png)

## Other Settings 
![5](https://user-images.githubusercontent.com/54615666/80364651-e9190800-88a3-11ea-82ce-874110ff53c3.png)
![6](https://user-images.githubusercontent.com/54615666/80364653-e9b19e80-88a3-11ea-8489-1b6fafeb518a.png)
![7](https://user-images.githubusercontent.com/54615666/80364655-e9b19e80-88a3-11ea-90f4-15662f9f04c0.png)
![8](https://user-images.githubusercontent.com/54615666/80364656-ea4a3500-88a3-11ea-9b7b-ab804e8bbf55.png)


## Share rules
Formatting files makes even more sense when multiple people are working on a project so of course, should share formatting rules with the team. It’s a good start if we use Android Studio’s default code formatting. But that’s unsafe! The default settings have changed in the past and will change in the future. Our team should use a code style independent from the IntelliJ version one is working on. So if we want any custom changes that would help all, please do update in this doc and share rules.
Find current rules in CodeStyleRules.xml
## Beyond Setting Changes
After following all these steps in android studio, now it is easy to use standard code guidelines in XML files. 

Reformat all edited files automatically in android studio using the following command.
* **Windows:** Ctrl + Alt + L
* **Linux:** Ctrl + Shift + Alt + L
* **mac:** Option + Command + L
* **Ubuntu** users might face the issue because Ctrl + Alt + L locks the screen by default nature. Refer to this stackoverflow-link on how to solve this.
