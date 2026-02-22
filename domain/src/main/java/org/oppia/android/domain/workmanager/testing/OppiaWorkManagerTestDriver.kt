package org.oppia.android.domain.workmanager.testing

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import androidx.work.impl.model.WorkSpec
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.guava.asDeferred
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.util.threading.BackgroundDispatcher

// TODO: Ban TestDriver interactions?
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class OppiaWorkManagerTestDriver @Inject constructor(
  private val context: Context,
  private val testCoroutineDispatchers: TestCoroutineDispatchers,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {
  private lateinit var workManager: WorkManager
  private val workDatabase get() = (workManager as WorkManagerImpl).workDatabase
  private val testDriver by lazy {
    checkNotNull(WorkManagerTestInitHelper.getTestDriver(context))
  }

  fun initialize(workManager: WorkManager) {
    check(!this::workManager.isInitialized) { "Attempting to re-initialize test driver." }
    this.workManager = workManager
  }

  // Must be called to ensure a worker is run when it has constraints, but won't override the
  // time-based scheduling constraint (the clock still needs to be advanced).
  fun forceConstraintsMet(id: UUID) {
    // There's no way to configure WorkManager to set specific constraints or to follow both
    // constraints and fixed time.
    testDriver.setAllConstraintsMet(id)
  }

  // Must be run on main thread.
  fun lookUpWorkInfo(id: UUID): WorkInfo? =
    runInBackground { workManager.getWorkInfoById(id).asDeferred().await() }

  fun lookUpWorkSpec(id: UUID): WorkSpec? {
    // This is a very hacky solution since it relies on WorkManagerImpl, but there's no other way to
    // access some of the worker's properties (such as its scheduled period) without this. That
    // could be observed behaviorally by leveraging clock management, but WorkManager already makes
    // that challenging. See ... TODO: Link to new issue here to fix timing. Seems simple to just make this observational rather than using the database hack.
    return workDatabase.workSpecDao().getWorkSpec(id.toString())
  }

  fun findUniqueId(workerName: String, taskType: OppiaWorker.TaskType): UUID {
    val workName = "$workerName.${taskType.persistentName}"
    return runInBackground {
      workManager.getWorkInfosForUniqueWork(workName).asDeferred().await().single().id
    }
  }

  private fun <T> runInBackground(func: suspend () -> T): T {
    val resultDeferred = CoroutineScope(backgroundDispatcher).async { func() }
    testCoroutineDispatchers.runCurrent()
    Truth.assertThat(resultDeferred.isCompleted).isTrue()
    return resultDeferred.getCompleted()
  }
}
