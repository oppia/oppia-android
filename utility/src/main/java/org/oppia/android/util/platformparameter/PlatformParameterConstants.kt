package org.oppia.android.util.platformparameter

import javax.inject.Qualifier

/**
 * This file contains all the constants that are associated with individual Platform Parameters.
 * These constants are:
 *  - Qualifier Annotation
 *  - Platform Parameter Name
 *  - Platform Parameter Default Value
 */

@Qualifier
annotation class SplashScreenWelcomeMsg

val SPLASH_SCREEN_WELCOME_MSG = "splash_screen_welcome_msg"
val SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE = false

/**
 * Qualifier for the platform parameter that defines the time period in hours, after which the
 * [PlatformParameterSyncUpWorker] will run again.
 */
@Qualifier
annotation class SyncUpWorkerTimePeriod

/**
 * Name of the platform parameter that defines the time period in hours, after which the
 * [PlatformParameterSyncUpWorker] will run again.
 */
val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS = "sync_up_worker_time_period"

/**
 * Default value of the platform parameter that defines the time period in hours, after which the
 * [PlatformParameterSyncUpWorker] will run again.
 */
val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE = 12
