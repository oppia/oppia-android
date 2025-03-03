package org.oppia.android.domain.platformparameter

import javax.inject.Qualifier
import org.oppia.android.app.model.PlatformParameterId

// NOTE: Injections will look something like this (assuming direct import of the ID):
//   @PlatformParameter(SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS) val syncUpWorkerTimePeriodInHours: Int

@Qualifier
annotation class PlatformParameter(val id: PlatformParameterId)
