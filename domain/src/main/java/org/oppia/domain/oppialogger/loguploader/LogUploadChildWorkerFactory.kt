package org.oppia.domain.oppialogger.loguploader

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

interface LogUploadChildWorkerFactory {
  fun create(context: Context, params: WorkerParameters): Worker
}
