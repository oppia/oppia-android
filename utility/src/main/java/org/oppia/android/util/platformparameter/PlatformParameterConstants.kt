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
val SPLASH_SCREEN_WELCOME_MSG_VALUE = true

@Qualifier
annotation class MyDownloads

val MY_DOWNLOADS_FLAG = "my_downloads_flag"
val MY_DOWNLOADS_IS_ENABLED_DEFAULT = true
val MY_DOWNLOADS_IS_DISABLE = false
