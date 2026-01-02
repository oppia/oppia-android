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
    constraints: Constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .build(),
    existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP
  ) {
    val workName = "$workerName.${taskType.persistentName}"
    val taskTypeKey = BootstrapOppiaWorker.constructTaskTypeKey(workerName)
    val inputData = Data.Builder().apply {
      putString(BootstrapOppiaWorker.DELEGATED_WORKER_NAME_INPUT_KEY, workerName)
      putString(taskTypeKey, taskType.persistentName)
    }.build()
    val request = PeriodicWorkRequest.Builder(
      BootstrapOppiaWorker::class.java, repeatInterval, intervalUnit
    ).addTag(workName)
      .setConstraints(constraints)
      .setInputData(inputData)
      .build()
    workManager.enqueueUniquePeriodicWork(workName, existingPeriodicWorkPolicy, request)
  }
}
