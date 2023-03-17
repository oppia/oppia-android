# What is WorkManager?
WorkManager is part of Android Jetpack and an Architecture Component for background work that needs a combination of opportunistic and guaranteed execution. Opportunistic execution means that WorkManager will do your background work as soon as it can. Guaranteed execution means that WorkManager will take care of the logic to start your work under a variety of situations, even if you navigate away from your app.

WorkManager is an incredibly flexible library that has many additional benefits. These include:
- Support for both asynchronous one-off and periodic tasks
- Support for constraints such as network conditions, storage space, and charging status
- Chaining of complex work requests, including running work in parallel
- Output from one work request used as input for the next
- Handling API level compatibility back to API level 14 (see note)
- Working with or without Google Play services
- Following system health best practices
- LiveData support to easily display work request state in UI

The WorkManager library is a good choice for tasks that are useful to complete, even if the user navigates away from the particular screen or your app. Some examples of tasks that are a good use of WorkManager:
- Uploading logs
- Applying filters to images and saving the image
- Periodically syncing local data with the network

WorkManager offers guaranteed execution, and not all tasks require that. As such, it is not a catch-all for running every task off of the main thread.

# Its Usage in Oppia Android
There are a few WorkManager classes you need to know about:

- `Worker`: This is where you put the code for the actual work you want to perform in the background. You'll extend this class and override the doWork() method.
- `WorkRequest`: This represents a request to do some work. You'll pass in your Worker as part of creating your WorkRequest. When making the WorkRequest you can also specify things like Constraints on when the Worker should run. There are two types of work supported by WorkManager: OneTimeWorkRequest and PeriodicWorkRequest.
- `WorkManager`: This class actually schedules your WorkRequest and makes it run. It schedules WorkRequests in a way that spreads out the load on system resources, while honoring the constraints you specify.


In Oppia we are using WorkManager in two scenarios :
- To upload cached Logs (for Analytics) over FirebaseAnalytics whenever data connection and battery requirements are met. This was implemented by @Sarthak2601 during GSoC'20, for more details you can go through the [proposal idea](https://github.com/oppia/oppia/wiki/pdfs/GSoC2020SarthakAgarwal.pdf) 
- To sync up the PlatformParameters from OppiaBackend whenever the app starts and the data + battery requirements are met. This was implemented by @ARJUPTA during GSoC'21, for more details you can go through the [proposal idea](https://github.com/oppia/oppia/wiki/pdfs/GSoC2021ArjunGupta.pdf)

# How to use WorkManager
If you want to introduce a new feature or any change to the existing WorkManager implementation in oppia-android, here is the basic structure of files you need to keep in mind :

1. Start with creating a Worker class (we have used `ListenableWorker` till now everywhere) for eg - MyFeatureWorker.
  
  ```
    class LogUploadWorker private constructor(
      context: Context,
      params: WorkerParameters,
      ...
      @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
    ) : ListenableWorker(context, params) {
        @ExperimentalCoroutinesApi
        override fun startWork(): ListenableFuture<Result> {
          val backgroundScope = CoroutineScope(backgroundDispatcher)
          val result = backgroundScope.async {...}
          return if(checkWorkDone(result)) Result.success() else Result.failure()
        }
    }
  ```
  
2. Then after implementing all the functionality in MyFeatureWorker, create a custom WorkerFactory class (for eg- MyFeatureWorkerFactory) so that we can provide any extra parameters if needed.
  
  ```
    class LogUploadWorkerFactory @Inject constructor(
      private val workerFactory: LogUploadWorker.Factory
    ) : WorkerFactory() {

    /** Returns a new [LogUploadWorker] for the given context and parameters. */
      override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
      ): ListenableWorker? {
        return workerFactory.create(appContext, workerParameters)
      }
    }
  ```
  
3. Provide an instance of this WorkerFactory class in the `WorkManagerConfigurationModule` so that a singular work manager configuration can be made for the entire app.
  
  ```
    @Module
    class WorkManagerConfigurationModule {

      @Singleton
      @Provides
      fun provideWorkManagerConfiguration(
        logUploadWorkerFactory: LogUploadWorkerFactory,
        platformParameterSyncUpWorkerFactory: PlatformParameterSyncUpWorkerFactory
      ): Configuration {
        val delegatingWorkerFactory = DelegatingWorkerFactory()
        delegatingWorkerFactory.addFactory(logUploadWorkerFactory)
        delegatingWorkerFactory.addFactory(platformParameterSyncUpWorkerFactory)
        return Configuration.Builder().setWorkerFactory(delegatingWorkerFactory).build()
      }
    }
  ```
  
4. After all these steps create an Initializer class (for eg- MyFeatureWorkerInitializer) that will prepare and enqueue a WorkRequest for you at the time when app starts.
  
  ```
    @Singleton
    class LogUploadWorkManagerInitializer @Inject constructor(
      private val context: Context,
      private val logUploader: LogUploader
    ) : ApplicationStartupListener {
      override fun onCreate() {
        val workManager = WorkManager.getInstance(context)
        logUploader.enqueueWorkRequestForEvents(workManager, workRequestForUploadingEvents)
        logUploader.enqueueWorkRequestForExceptions(workManager, workRequestForUploadingExceptions)
      }
    }
  ```
  
**Note** - All the parts of WorkManager implementation entirely lie in the domain layer, but there are few functionalities that you may need to acquire from other layers for eg- if you need to make a network request you would probably need to interact with data layer also.

# Writing tests with WorkManager
For writing any test with WorkManager you will need to interact with
- *WorkManagerTestInitHelper* so that you can emulate the enquing and running of WorkRequests.

 ```
  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    context = InstrumentationRegistry.getInstrumentation().targetContext
    val config = Configuration.Builder()
      .setExecutor(SynchronousExecutor())
      .setWorkerFactory(logUploadWorkerFactory)
      .build()
    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
  }
 ```
- *TestCoroutinesDispatcher* so that you can block the code execution up untill WorkRequest(s) are running. (ie. working with suspend functions)
- You might also need to introduce some fakes so that you can make sure the entities (object, classes, varaibles & constants etc.) over which you MyFeatureWorker depends doesn't have any bugs.

Here is an exemplar test that is using WorkManager to enqueue a WorkRequest with any inputData (if needed). After we enqueue a request, the next step is to wait until its execution is completed and for that we are using testCoroutineDispatchers - 
```
  @Test
  fun testWorker_logEvent_withoutNetwork_enqueueRequest_verifySuccess() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logTransitionEvent(
      eventLogTopicContext.timestamp,
      eventLogTopicContext.actionName,
      oppiaLogger.createTopicContext(TEST_TOPIC_ID)
    )

    val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())

    val inputData = Data.Builder().putString(
      LogUploadWorker.WORKER_CASE_KEY,
      LogUploadWorker.EVENT_WORKER
    ).build()

    val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
      .setInputData(inputData)
      .build()

    workManager.enqueue(request)
    testCoroutineDispatchers.runCurrent()
    val workInfo = workManager.getWorkInfoById(request.id)

    assertThat(workInfo.get().state).isEqualTo(WorkInfo.State.SUCCEEDED)
    assertThat(fakeEventLogger.getMostRecentEvent()).isEqualTo(eventLogTopicContext)
  }
```

In Oppia we write tests for both the Worker and its Initializer class. You can take a reference for the same from these files:

Worker Tests - *[PlatformParameterSyncUpWorkerTest](https://github.com/oppia/oppia-android/blob/develop/domain/src/test/java/org/oppia/android/domain/platformparameter/syncup/PlatformParameterSyncUpWorkerTest.kt) OR [LogUploadWorkerTest](https://github.com/oppia/oppia-android/blob/develop/domain/src/test/java/org/oppia/android/domain/oppialogger/loguploader/LogUploadWorkerTest.kt)*

Initializer Tests - *[PlatformParameterSyncUpWorkManagerInitializerTest](https://github.com/oppia/oppia-android/blob/develop/domain/src/test/java/org/oppia/android/domain/platformparameter/syncup/PlatformParameterSyncUpWorkManagerInitializerTest.kt) OR [LogUploadWorkManagerInitializerTest](https://github.com/oppia/oppia-android/blob/develop/domain/src/test/java/org/oppia/android/domain/oppialogger/loguploader/LogUploadWorkManagerInitializerTest.kt)*