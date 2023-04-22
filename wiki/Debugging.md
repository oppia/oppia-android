We do a lot of debugging at Oppia-Android, whether because tests are failing on a PR or because of a tricky bug that was found during release testing. These guides will help you get started debugging and offer useful tips:

## How to approach Bugs?
1. First try to find the exact reasons of the bugs.
2. Once you have identified the reason, write a test case which actually fails because of the bug. (This will be needed in future).
3. Now work towards solution.
4. Now, the test cases which were written in step 2 should pass and that way we can be sure that the bug has been fixed.
So at this stage you should focus on step 1.

Unless the bug you are trying to fix is trivial, we recommend creating a debugging doc to organize your work:
- [Debugging Docs](https://github.com/oppia/oppia-android/wiki/Get-Help#before-you-ask-a-general-question)