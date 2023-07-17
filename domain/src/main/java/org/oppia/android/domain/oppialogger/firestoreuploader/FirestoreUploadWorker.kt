package org.oppia.android.domain.oppialogger.firestoreuploader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import org.oppia.android.domain.util.getStringFromData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.threading.BackgroundDispatcher

/**
 * Worker class that extracts log reports from the cache store and logs them to the
 * remote service.
 */
class FirestoreUploadWorker private constructor(
  context: Context,
  params: WorkerParameters,
  private val dataController: FirestoreDataController,
  private val consoleLogger: ConsoleLogger,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ListenableWorker(context, params) {

  companion object {
    const val WORKER_CASE_KEY = "firestore_worker_case_key"
    const val TAG = "FirestoreUploadWorker.tag"
    const val FIRESTORE_WORKER = "firestore_worker"
  }

  @ExperimentalCoroutinesApi
  override fun startWork(): ListenableFuture<Result> {
    val backgroundScope = CoroutineScope(backgroundDispatcher)
    val result = backgroundScope.async {
      when (inputData.getStringFromData(WORKER_CASE_KEY)) {
        FIRESTORE_WORKER -> uploadFirestoreData()
        else -> Result.failure()
      }
    }

    val future = SettableFuture.create<Result>()
    result.invokeOnCompletion { failure ->
      if (failure != null) {
        future.setException(failure)
      } else {
        future.set(result.getCompleted())
      }
    }
    // TODO(#3715): Add withTimeout() to avoid potential hanging.
    return future
  }

  /** Extracts data from offline Firestore and logs them to the remote service. */
  private suspend fun uploadFirestoreData(): Result {
    return try {
      dataController.uploadData()
      dataController.removeFirstEventLogFromStore()
      Result.success()
    } catch (e: Exception) {
      consoleLogger.e(TAG, e.toString(), e)
      Result.failure()
    }
  }

  /** Creates an instance of [FirestoreUploadWorker] by properly injecting dependencies. */
  class Factory @Inject constructor(
    private val dataController: FirestoreDataController,
    private val consoleLogger: ConsoleLogger,
    @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
  ) {
    fun create(context: Context, params: WorkerParameters): ListenableWorker {
      return FirestoreUploadWorker(
        context,
        params,
        dataController,
        consoleLogger,
        backgroundDispatcher
      )
    }
  }
}
