package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/** Returns an instance of the worker class after injecting the needed dependencies. */
interface LogUploadChildWorkerFactory {
  fun create(context: Context, params: WorkerParameters): Worker
}
