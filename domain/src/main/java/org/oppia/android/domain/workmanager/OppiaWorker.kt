package org.oppia.android.domain.workmanager

interface OppiaWorker<T : OppiaWorker.TaskType> {
  suspend fun doWork(taskType: T): Result

  enum class Result {
    SUCCESS,
    FAILURE
  }

  interface TaskType {
    // TODO: Document that this needs to survive across different obfuscation sessions. It doesn't necessarily need to be stable across job runs, though.
    val persistentName: String
  }

  interface Factory<T : TaskType> {
    val supportedTaskTypes: List<T>

    fun createWorker(): OppiaWorker<T>

    suspend fun doWorkForTaskName(taskName: String): Result? {
      val taskType = supportedTaskTypes.find { taskName == it.persistentName } ?: return null
      return createWorker().doWork(taskType)
    }
  }
}
