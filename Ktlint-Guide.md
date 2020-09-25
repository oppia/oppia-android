# Installation 
Install `Ktlint` from [here](https://github.com/pinterest/ktlint#installation). Note that we specifically recommend using the command-line interface, not the Gradle integration.

# Commands 
* `ktlint --version` - Check the version of your installed ktlint. As of now on CircleCI we are using `0.37.0`.

* `ktlint --android "path/of/your/kotlin/file"` - Android Kotlin Style Guide using `--android`

   * Examples <br>
       * Specific File - <br>`ktlint --android "app/src/sharedTest/java/org/oppia/app/walkthrough/WalkthroughWelcomeFragmentTest.kt"`<br>
       * Specific Directory - `ktlint --android "utility/src/**/*.kt"`

* `ktlint -F --android "path/of/your/kotlin/file"` - Android Kotlin Style Guide using `--android` with Ktlint Auto Formatter `-F`. This will fix some of the issues automatically. 

   * Examples <br>
       * Specific File - <br>`ktlint -F --android "app/src/sharedTest/java/org/oppia/app/walkthrough/WalkthroughWelcomeFragmentTest.kt"`<br>
       * Specific Directory - `ktlint -F --android "utility/src/**/*.kt"`

# Macros
In Android Studio we have a feature called Macros which helps you convert multiple tasks in one shortcut.

There are two major tasks when we talk about style formatting, One is the `Reformat Code` and another one is `Optimize imports`. 

* `Reformat Code` - this will reformat your code with proper indentation and newlines wherever needed.
* `Optimize imports` - this will remove all the unused import from your file. Also, this will rearrange your imports in lexicographical order and all those imports which are starting with `java`, `javax` or `Kotlin` will get shifted at the end of the import list which is a correct order. 

### Steps to create a macro: 
1. Double shift
2. Search for "macros" 
3. Click on "Macros" 
4. Click on "start macro reading" 
5. Menu Toolbar -> Code -> Reformat Code -> Menu Toolbar -> Code -> Optimize Import -> click on stop macro reading at bottom 
6. Now you can give whatever shortcut you want and these above two steps will get performed

# How to fix the most common issues?
* `Wildcard Imports` - If you had imported anything which directs to just the package `import java.util.*`, it will give you an error saying there should not be any wildcard imports. So, you had to use the path completely for what you need in your file. 

   * Example - `import java.util.Date` or `import java.util.Locale`

* `Exceeding 100 char limit` - This means that there is a line of code which is more than 100 char in one line. You must have noticed a grey line in the editor area, the code should not cross that line. 

    There are some cases like the name of the tests where the code crosses that line and we cannot rearrange it as it is the name of the function. This does not apply to comments. There you should put a ktlint comment using which ktlint disable the check for 100 char limit.  `// ktlint-disable max-line-length`

    If you want to disable for a block of code/ multiple lines, you can put the blocky comment as well. At starting `/* ktlint-disable max-line-length */` and at the end `/* ktlint-enable max-line-length */`

    * Example - <br>
       * `fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicSelected_clickNoButton_worksCorrectly() { // ktlint-disable max-line-length`
       
       * ```
         /* ktlint-disable max-line-length */ 
         fun testWalkthroughWelcomeFragment_recyclerViewIndex1_topicSelected_clickNoButton_worksCorrectly() {
         ....
         .... 
         }
         /* ktlint-enable max-line-length */
         ```