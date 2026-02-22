package org.oppia.android.domain.workmanager.testing

import android.content.Context
import androidx.room.InvalidationTracker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import androidx.work.impl.model.WorkSpec
import androidx.work.testing.WorkManagerTestInitHelper
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.util.threading.BackgroundDispatcher

// TODO: Document that is VERY limited and doesn't implement the more complex work manager features like back-off. It also only handles periodic jobs and doesn't interop with running jobs directly outside this tool. It doesn't support initial delays since we don't use them. Document the initialization needs and how/why/when to use this.
@Singleton
class TestJobSchedulerMixin @Inject constructor(
  private val context: Context,
  private val testDriver: OppiaWorkManagerTestDriver,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {
  private lateinit var workManager: WorkManager
  private val workDatabase get() = (workManager as WorkManagerImpl).workDatabase
  private val trackedWorkers = mutableSetOf<UUID>()

  fun startWatchingForWorkers(workManager: WorkManager) {
    this.workManager = workManager
    workDatabase.invalidationTracker.addObserver(
      object : InvalidationTracker.Observer("WorkSpec") {
        override fun onInvalidated(tables: MutableSet<String>) {
          rescanWorkers()
        }
      })
    rescanWorkers()
  }

  private fun rescanWorkers() {
    // Check if any workers have changed.
    for (workSpec in workDatabase.workSpecDao().run { getWorkSpecs(allWorkSpecIds) }) {
      val id = UUID.fromString(workSpec.id)
      if (workSpec.isPeriodic && id !in trackedWorkers) {
        trackedWorkers += id
        maybeScheduleNextJob(id, workSpec)
      }
    }
  }

  private fun maybeScheduleNextJob(id: UUID, workSpec: WorkSpec? = testDriver.lookUpWorkSpec(id)) {
    if (workSpec != null && workSpec.state != WorkInfo.State.CANCELLED) {
      check(workSpec.isPeriodic)
      val intervalMs = workSpec.intervalDuration
      CoroutineScope(backgroundDispatcher).launch {
        delay(intervalMs)
        val wmTestDriver = checkNotNull(WorkManagerTestInitHelper.getTestDriver(context))
        if (testDriver.isAutoForcingConstraints(id)) wmTestDriver.setAllConstraintsMet(id)
        wmTestDriver.setPeriodDelayMet(id)
        maybeScheduleNextJob(id)
      }
    }
  }
}
