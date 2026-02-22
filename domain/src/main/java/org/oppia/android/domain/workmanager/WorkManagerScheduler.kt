package org.oppia.android.domain.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerScheduler @Inject constructor(context: Context) {
  private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

  fun schedulePeriodicWorker(
    workerName: String,
    taskType: OppiaWorker.TaskType,
    repeatInterval: Long,
    intervalUnit: TimeUnit,
    requireNetworkConnectivity: Boolean = true
  ) {
    val workName = "$workerName.${taskType.persistentName}"
    val taskTypeKey = BootstrapOppiaWorker.constructTaskTypeKey(workerName)
    val inputData = Data.Builder().apply {
      putString(BootstrapOppiaWorker.DELEGATED_WORKER_NAME_INPUT_KEY, workerName)
      putString(taskTypeKey, taskType.persistentName)
    }.build()
    val repeatIntervalMs = intervalUnit.toMillis(repeatInterval)
    val adjustedIntervalMs =
      repeatIntervalMs.coerceIn(MINIMUM_JOB_INTERVAL_MILLIS..MAXIMUM_JOB_INTERVAL_MILLIS)
    val request = PeriodicWorkRequest.Builder(
      BootstrapOppiaWorker::class.java, adjustedIntervalMs, TimeUnit.MILLISECONDS
    ).apply {
      addTag(workName)
      setConstraints(
        Constraints.Builder().apply {
          if (requireNetworkConnectivity) {
            setRequiredNetworkType(NetworkType.CONNECTED)
          } else setRequiredNetworkType(NetworkType.NOT_REQUIRED)
          setRequiresBatteryNotLow(true)
        }.build()
      )
      setInputData(inputData)
    }.build()
    // Note that UPDATE is used here so that new app versions or platform parameter configurations
    // can update the constraints and timed period of jobs.
    // TODO: File issue to use UPDATE here instead of KEEP (need 2.9.0).
    workManager.enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, request)
  }

  private companion object {
    private val MINIMUM_JOB_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(15)
    private val MAXIMUM_JOB_INTERVAL_MILLIS = TimeUnit.DAYS.toMillis(14)
  }
}
