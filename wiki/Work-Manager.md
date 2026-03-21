## Table of Contents

- [What is `WorkManager`?](#what-is-workmanager)
- [When to use `WorkManager`](#when-to-use-workmanager)
- [How to use `WorkManager`](#how-to-use-workmanager)
- [Writing tests with `WorkManager`](#writing-tests-with-workmanager)
- [Debugging `WorkManager`](#debugging-workmanager)

# What is WorkManager?
`WorkManager` is part of Android Jetpack and an Architecture Component for background work that needs a combination of opportunistic and guaranteed execution. Opportunistic execution means that `WorkManager` will do your background work as soon as it can. Guaranteed execution means that `WorkManager` will take care of the logic to start your work under a variety of situations, even if you navigate away from your app.

`WorkManager` is an incredibly flexible library that has many additional benefits. These include:
- Support for both asynchronous one-off and periodic tasks
- Support for constraints such as network conditions, storage space, and charging status
- Chaining of complex work requests, including running work in parallel
- Output from one work request used as input for the next
- Handling API level compatibility back to API level 14
- Working with or without Google Play services
- Following system health best practices

The `WorkManager` library is a good choice for tasks that are useful to complete, even if the user navigates away from the particular screen or your app. Some examples of tasks that are a good use of `WorkManager`:
- Uploading logs
- Periodically syncing local data with the network

`WorkManager` offers guaranteed execution, and not all tasks require that. As such, it is not a catch-all for running every task off of the main thread.

# When to use WorkManager
`WorkManager` is used for long-running periodic tasks such as collecting or uploading analytics, and synchronizing platform parameters with the Oppia backend. These tasks can't be performed in any other way because they need to be able to run even when the user isn't actively using the device (particularly in the case of platform parameters which we want to synchronize for the next app open). Thus, only work that needs to be able to happen even when the app is closed, or under very specific network situations (like the device connecting to cellular or wifi triggering the app needing to do something) should use `WorkManager`.

Other types of background tasks (those are, tasks done off the main thread) are all handled using a background coroutine dispatcher and are generally coerced into a specific data flow pattern called `DataProvider`s. For more context on those, see the corresponding [wiki page](https://github.com/oppia/oppia-android/wiki/DataProvider-&-LiveData).

# How to use WorkManager
The Oppia Android app implements exactly one worker: `BootstrapOppiaWorker`. This worker is responsible for several things:
- Determining if an invalid worker is trying to be run (such as an old worker that's no longer valid) and, if so, cancelling it.
- Identifying the specific type of work a worker wants to do.
- Deferring worker construction until platform parameters are guaranteed to be loaded (so that both workers and their factories can depend on platform parameters either directly or via their other dependencies).
- Delegating work to an actual defined `OppiaWorker` implementation.

With this architecture in place, you'll never need to directly implement a `ListenableWorker`. Instead, you must do the following:
- Introduce a new `OppiaWorker` implementation (along with its factory and task type).
- Introduce a scheduler class that can automatically schedule the new worker for periodic work.
- Introduce a module that creates the necessary Dagger bindings for both the worker and schedule to automatically be used. Note that this includes adding the new module to the corresponding application component classes (e.g. `DeveloperApplicationComponent`) so that the bindings are enabled.

That's it! From there you can customize which tasks the job can do and adapt the work and scheduling accordingly. It's recommended to look at existing implementations for how they're set up, but the simplest is the debug worker which is designed to specifically analyze `WorkManager` behavior and can be seen in the [`org.oppia.android.domain.workmanager.debug` package](https://github.com/oppia/oppia-android/tree/develop/domain/src/main/java/org/oppia/android/domain/workmanager/debug).

# Writing tests with WorkManager

Writing worker tests generally means testing one of three things:
- The actual worker (i.e. the class that implements `OppiaWorker`).
- Testing the class that automatically schedules the worker on application startup (i.e. the class that implements `StartupWorkerScheduleReadinessListener`).
- Testing the Dagger module that correctly binds both the worker and the scheduler.

For all three of these, look at the following references (plus their tested class implementations):
- [`DebugWorkerTest`](https://github.com/oppia/oppia-android/blob/develop/domain/src/test/java/org/oppia/android/domain/workmanager/debug/DebugWorkerTest.kt)
- [`DebugWorkerSchedulerTest`](https://github.com/oppia/oppia-android/blob/develop/domain/src/test/java/org/oppia/android/domain/workmanager/debug/DebugWorkerSchedulerTest.kt)
- [`DebugWorkerDebugModuleTest`](https://github.com/oppia/oppia-android/blob/develop/domain/src/test/java/org/oppia/android/domain/workmanager/debug/DebugWorkerDebugModuleTest.kt)

These classes are specifically designed to serve as examples for creating and testing Oppia workers.

At a higher level, tests never interact with `WorkManager` directly. Instead, they make use of [`OppiaWorkManagerTestDriver`](https://github.com/oppia/oppia-android/blob/develop/domain/src/main/java/org/oppia/android/domain/workmanager/testing/OppiaWorkManagerTestDriver.kt) to do all of:
- Initializing the test so that it can properly integrate with `WorkManager`.
- Running `OppiaWorker`s as either one-off or periodic jobs.
- Inspect the latest state of a job.
- Force job constraints to test constrained scenarios (such as when internet connectivity is required).

Cumulatively, these provide all the necessary functionality for testing both workers and worker schedulers. Validating the module is done using standard module testing techniques.

Setting up the test environment properly is highly complex which is why the helper must be used (and, in fact, file content checks prohibit using `WorkManager` or its testing utilities directly).

# Debugging WorkManager

Verifying that `WorkManager` is behaving correctly can be difficult since it relies heavily on OS constraints and thus cannot necessarily guarantee certain behaviors, especially for periodic tasks. Here are some important aspects to note when trying to determine if a worker is running correctly:
- `WorkManager` won't allow periodic tasks to run more frequently than 15 minutes unless they are explicitly initiated with a one-time work request.
- Periodic tasks will run immediately upon opening the app if their constraints are met and they haven't yet run within their configured time period.
- Tasks will run even if the app is closed and this can be validated by force closing the app using: `adb shell am force-stop org.oppia.android`
- There's a debug worker that's configured to run every 15 minutes, 20 minutes, and 6 hours (to align with other long periodic workers). This only runs in `//:oppia_dev` builds but simply opening the developer app at least once and watching ADB (for debug logs) should provide a basis to verify that `WorkManager` is actually running.
- You can use tests to verify that your worker will run when expected. See `DebugWorkerTest` for an example of how to test periodic workers.
- Finally, you can actually force a periodic task to run through ADB (see below).

**Important**: Many of the following commands may require using a root ADB shell. You can enable that using `adb root` but it may not be available on all Android devices. It should always be available for emulators so long as they're running a developer version of the Android OS (make sure you aren't using an image that has Google Play, but you may need Google APIs for testing certain jobs like Firebase analytics).

To force a specific worker to run we must first figure out what's currently enqueued. To do that run the following ADB command:

```sh
adb shell am broadcast -a "androidx.work.diagnostics.REQUEST_DIAGNOSTICS" org.oppia.android -f 32
```

(Note that the `-f 32` will allow the diagnostic request to wake the app if it was force stopped or never opened).

View logcat for info logs. You should see something like the following:

```
Enqueued work:
Id  Class Name  Job Id  State   Unique Name Tags
8126cd00-2c35-4704-8239-77abda3dd12e    org.oppia.android.domain.workmanager.BootstrapOppiaWorker   2109    ENQUEUED    DebugWorker.RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY    DebugWorker.RUN_EVERY_SIX_HOURS_WITH_OR_WITHOUT_CONNECTIVITY,org.oppia.android.domain.workmanager.BootstrapOppiaWorker
e7b9b00c-e742-4688-b004-14cf07698405    org.oppia.android.domain.workmanager.BootstrapOppiaWorker   2110    ENQUEUED    PlatformParameterSyncUpWorker.refresh_platform_parameters   org.oppia.android.domain.workmanager.BootstrapOppiaWorker,PlatformParameterSyncUpWorker.refresh_platform_parameters
240ffbc1-3001-478b-947b-3549f9e91007    org.oppia.android.domain.workmanager.BootstrapOppiaWorker   2112    ENQUEUED    LogUploadWorker.upload_performance_metrics  org.oppia.android.domain.workmanager.BootstrapOppiaWorker,LogUploadWorker.upload_performance_metrics
21048af0-8b1b-4c85-899e-c252ecd4c2fa    org.oppia.android.domain.workmanager.BootstrapOppiaWorker   2113    ENQUEUED    LogUploadWorker.upload_exceptions   org.oppia.android.domain.workmanager.BootstrapOppiaWorker,LogUploadWorker.upload_exceptions
88be6627-bc44-45ab-993f-9dcb9b6a756d    org.oppia.android.domain.workmanager.BootstrapOppiaWorker   2114    ENQUEUED    LogUploadWorker.upload_firestore_data   org.oppia.android.domain.workmanager.BootstrapOppiaWorker,LogUploadWorker.upload_firestore_data
9aecbfd4-405c-4f29-9467-a6e434350ded    org.oppia.android.domain.workmanager.BootstrapOppiaWorker   2115    ENQUEUED    LogUploadWorker.upload_events   org.oppia.android.domain.workmanager.BootstrapOppiaWorker,LogUploadWorker.upload_events
9de8273f-6c12-4684-94a3-a75b4b36aa0b    org.oppia.android.domain.workmanager.BootstrapOppiaWorker   2123    ENQUEUED    DebugWorker.RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY   org.oppia.android.domain.workmanager.BootstrapOppiaWorker,DebugWorker.RUN_EVERY_TWENTY_MINUTES_WITH_OR_WITHOUT_CONNECTIVITY
9e070daa-c323-499f-b45f-00d2932f8809    org.oppia.android.domain.workmanager.BootstrapOppiaWorker   2131    ENQUEUED    DebugWorker.RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY org.oppia.android.domain.workmanager.BootstrapOppiaWorker,DebugWorker.RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY
```

This forces `WorkManager` to reconcile its internal state which will automatically kick off stale jobs. We can leverage that to force a job that's run within its recent time period to run again by making `WorkManager` forget about it. This even bypasses its own internal 15 minute limit. The following command can be used to achieve this:

```sh
adb shell "run-as org.oppia.android sqlite3 no_backup/androidx.work.workdb \"UPDATE WorkSpec SET period_start_time = 0 WHERE id = '<ID>'; DELETE FROM SystemIdInfo WHERE work_spec_id = '<ID>';\""
```

`<ID>` is updated using one of the IDs in the diagnostics table above, for example `9e070daa-c323-499f-b45f-00d2932f8809` would correspond to the periodic 15-minute `DebugWorker` job. These IDs are long-lived unlike the job IDs above.

Once this command finishes you can re-run the diagnostics command above and the corresponding job should run.

For simplicity, here are Bash functions that take either a parameter of the worker's unique name (e.g. 'DebugWorker.RUN_EVERY_FIFTEEN_MINUTES_WITH_CONNECTIVITY') or the worker's ID performs all of the necessary commands:

```bash
function PrintOppiaWorkerDiagnostics() {
  adb shell am broadcast -a "androidx.work.diagnostics.REQUEST_DIAGNOSTICS" org.oppia.android -f 32
}

function ForceRunOppiaJobById() {
  echo "Attempting to force run Oppia worker with ID: $1"
  local worker_id="$1"
  adb shell "run-as org.oppia.android sqlite3 no_backup/androidx.work.workdb \"UPDATE WorkSpec SET period_start_time = 0 WHERE id = '$worker_id'; DELETE FROM SystemIdInfo WHERE work_spec_id = '$worker_id';\""
  PrintOppiaWorkerDiagnostics
}

function ForceRunOppiaJobWithUniqueName() {
  local unique_name="$1"
  echo "Attempting to find an Oppia worker with unique name: $unique_name"
  PrintOppiaWorkerDiagnostics
  local app_pid=$(adb shell pidof -s org.oppia.android)
  local worker_id=$(adb logcat --pid=$app_pid -d | grep "WM-DiagnosticsWrkr" | grep "$unique_name" | tail -1 | awk '{for(i=1;i<=NF;i++) if($i=="WM-DiagnosticsWrkr:") {print $(i+1); exit}}')
  echo "Worker has unique ID: $worker_id"
  ForceRunOppiaJobById $worker_id
}
```

Caveat: these may only run successfully if there's exactly one device available.
