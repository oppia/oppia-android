Please follow the following style rules when writing code, in order to minimize unnecessary back-and-forth during code review.

## General
- We follow a hybrid of [JetBrain's Kotlin style guide](https://developer.android.com/kotlin/style-guide) and [Google's Java style guide](https://google.github.io/styleguide/javaguide.html#s4.2-block-indentation).
- Use 2 spaces for indentation and 4 spaces for continuation, per https://google.github.io/styleguide/javaguide.html#s4.2-block-indentation. (This should be configured at the project level for Kotlin. Ensure that you're using the project configuration for Kotlin in your IDE, so that you can reformat the code via the IDE if needed.)
- Never commit a PR which includes commented-out code.
- Ensure that your code looks consistent with the code surrounding it.
- Ensure that the indentation of your code is correct.
- In general, avoid leaving multiple consecutive blank lines. Collapse them into a single one.
- The last character in each file should always be a newline. (If it's not, you'll see a red symbol at the end of the file when you look at the "Files Changed" tab in GitHub.)
- Make sure to remove temporary code (e.g. log statements or toasts to help with local debugging) before pushing to GitHub.
- Do not check any build artifacts into GitHub.

## Comments
- Ensure that comments have correct grammar, spelling and capitalization.
- Vertically align the `*`s on the left side of a multi-line comment.
- Wrap Javadoc comments to the 120 character limit. 
- Put new lines between Javadoc paragraphs.
- Do not leave any spaces between a Javadoc and the class/method/field that it's documenting.
- When writing TODOs, refer to an issue number on the GitHub issue tracker. E.g. `TODO(#1234): Do X, Y and Z.`

## XML files
- Do not declare values directly in the XML file; use e.g. a dimens.xml file instead. In general, avoid using hard-coded strings.

## Java/Kotlin files
- Separate adjacent functions or blocks of code by a single blank line.
- Order imports alphabetically. Remove unused imports.
- Do not use "magic numbers" in code. Declare constants instead (typically at the module level).

## build.gradle file
- Arrange lists in alphabetical order unless there's a good reason not to.