Please follow the following style rules when writing code, in order to minimize unnecessary back-and-forth during code review.

## General
- In general, we follow [Google's Java style guide](https://google.github.io/styleguide/javaguide.html#s4.2-block-indentation) (but please look at it from a Kotlin perspective).
- Use 2 spaces for indentation and 4 spaces for continuation, per https://google.github.io/styleguide/javaguide.html#s4.2-block-indentation. (This should be configured at the project level for Kotlin. Ensure that you're using the project configuration for Kotlin in your IDE, so that you can reformat the code via the IDE if needed.)
- Never commit a PR which includes commented-out code.
- Ensure that your code looks consistent with the code surrounding it.
- Ensure that the indentation of your code is correct.
- In general, avoid leaving multiple consecutive blank lines. Collapse them into a single one.
- The last character in each file should always be a newline. (If it's not, you'll see a red symbol at the end of the file when you look at the "Files Changed" tab in GitHub.)
- Make sure to remove temporary code (e.g. log statements or toasts to help with local debugging) before pushing to GitHub.
- Do not check any build artifacts into GitHub.

## Code Formatting
Reformat all edited files automatically in android studio using the following command.
- Windows: `Ctrl + Alt + L`
- Linux: `Ctrl + Shift + Alt + L`
- macOS: `Option + Command + L`
- Ubuntu users might face issue because `Ctrl + Alt + L` locks the screen by default nature. Refer to this [stackoverflow-link](https://stackoverflow.com/questions/16580171/code-formatting-shortcut-in-android-studio) on how to solve this.

NOTE: This does not guarantee 100% formatting of code as per guidelines but will be very helpful in indentation and extra spaces.

## Comments
- Ensure that comments have correct grammar, spelling and capitalization.
- Vertically align the `*`s on the left side of a multi-line comment.
- Wrap Javadoc comments to the 120 character limit. 
- Put new lines between Javadoc paragraphs.
- Do not leave any spaces between a Javadoc and the class/method/field that it's documenting.
- When writing TODOs, refer to an issue number on the GitHub issue tracker. E.g. `TODO(#1234): Do X, Y and Z.`

## XML files
- Do not declare values directly in the XML file; use e.g. a dimens.xml file instead. In general, avoid using hard-coded strings. Similar case for colors and strings

## Java/Kotlin files
- Separate adjacent functions or blocks of code by a single blank line.
- Order imports alphabetically. Remove unused imports.
- Do not use "magic numbers" in code. Declare constants instead (typically at the module level).

## Layout files
- Each layout file should be named according to how they are used, where all layouts fall in the following buckets:
  - Activities: _screen\_name\_activity.xml_ (all end with ``_activity.xml``)
  - Fragments: _subscreen\_name\_fragment.xml_ (all end with ``fragment.xml``)
  - Custom views: _custom\_widget\_name\_view.xml_ (all end with ``_activity.xml``)
  - RecyclerView items: _element\_name\_item.xml_ (all end with ``_item.xml``)
  - Toolbars: _screen\_location\_toolbar.xml_ (all end with ``_toolbar.xml``)
- Any layouts not associated with the above that should be shared across multiple layouts should instead be associated with a custom view (including a corresponding Kotlin file). This means the ``include`` directive won't be included in any layouts.
- Since widget IDs within layout files are global scoped, they should be named based on their location, value, and widget type.
  - The general format for this is: _<parent\_file\_name>\_<view\_name>\_<widget\_type>_
  - The following are some recognized widget types (this list is not comprehensive):
    - ``TextView``
    - ``EditText``
    - ``RecyclerView``
    - ``Button``
    - ``View``
    - Custom views (the full name of the view should be spelled out for the widget type in identifiers)
  - Layout elements should be named as follows:
    - ``container``: if the element contains other elements within the same layout
    - ``placeholder``: if the element will be replaced at runtime with new elements (such as a fragment layout)
  - Here are some examples of valid IDs:
    - ``recently_played_activity_recently_played_fragment_placeholder`` (a ``FrameLayout`` in ``recently_played_activity.xml``)
    - ``recently_played_fragment_ongoing_story_recycler_view`` (a ``RecyclerView`` in ``recently_played_fragment.xml``)

## build.gradle file
- Arrange lists in alphabetical order unless there's a good reason not to.
- Combine `implementation`, `androidTestImplementation` and `testImplementation` to declare all similar dependencies in one block.