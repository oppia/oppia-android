## Table of Contents

- [How to find error message for Failing CI checks](#how-to-find-error-message-for-failing-ci-checks)
- [Developer Video - Understanding CI check failures](#developer-video---understanding-ci-check-failures)

## How to find error message for Failing CI checks

Creating a pr or updating a pr runs all the CI checks, which can sometimes fail if the code changes have affected some other part of the app or if the code changes don’t need some reformatting and docs. In these cases understanding the error and fixing it requires how to find the error.

![image](https://user-images.githubusercontent.com/54740946/135907913-3e542b99-ff10-420e-819c-dac818033679.png)


All the checks of the latest commit in the pull request are displayed at the bottom of the pr. Scroll to all the checks and open details for the failing checks which display logs of each check.

Each check contains multiple jobs and now select the job with the failure.
Example in the below check the second job has some error or failure

![image](https://user-images.githubusercontent.com/54740946/135908001-eb46d5f1-2c1c-43ec-be62-8fab58bb00ec.png)


Navigate to logs or search the keyword ‘error’ to find the error message to understand what might have caused the failure in the checks.

### Developer Video - Understanding CI check failures
Learn how to interpret and troubleshoot oppia-android CI check failures in this insightful [developer video](https://youtu.be/I2bRf6fvgJ0?si=35sAagbUFSk6bOBA).