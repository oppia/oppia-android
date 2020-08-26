package org.oppia.domain.oppialogger

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

interface ChildWorkerFactory {
  fun create(context: Context, params: WorkerParameters): Worker
}