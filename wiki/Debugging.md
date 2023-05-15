We do a lot of debugging at Oppia-Android, whether because tests are failing on a PR or because of a tricky bug that was found during release testing. This guide will help you get started with debugging and offer useful tips:

## How do I find the root cause of a bug?

Generally, finding the root cause of a bug is a question of strategically checking various things so that you can narrow down the "search space" of its possible causes. Here is an approach that you can use to get started:

1. First, try to reproduce the bug. If you can, that's great; that means you have a way to check whether your fix works. Otherwise, see if you can figure out what circumstances cause the bug to appear (is it on a particular device, does it happen only on either the prod or dev build, etc.). This is important because you will need to find a way to reliably reproduce the bug, in order to test whether your fix worked.
2. If this issue arose in your PR, then check whether the bug appears in develop. If so, then you know that it's not due to changes in your PR, and can do further investigation directly on develop. Otherwise, you can narrow down the cause to a change in your PR.
3. Based on the environment you've identified in steps 1 + 2, write a test case which the bug causes to fail (if one doesn't already exist). This gives you an automatic way to check whether or not the bug is fixed.
4. Then, work towards the solution, until the test case you wrote in step 3 passes.

Unless the bug you are trying to fix is trivial, we recommend creating a debugging doc to organize your work and easily communicate it to other team members. See [Debugging Docs](https://github.com/oppia/oppia-android/wiki/Debugging-Docs) for guidance on how to write a debugging doc.
