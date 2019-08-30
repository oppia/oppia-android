package org.oppia.app.application

import android.app.Application
import android.content.Context

/** Called when the application is starting, before any activity, service, or receiver objects (excluding content providers) have been created.
 */
class OppiaApplication : Application() {

  init {
    instance = this
  }

  companion object {
    private var instance: OppiaApplication? = null

    fun applicationContext() : Context {
      return instance!!.applicationContext
    }
  }

  override fun onCreate() {
    super.onCreate()
    // initialize for any

    // Use ApplicationContext.
    // example: SharedPreferences etc...
    val context: Context = applicationContext()
  }
}

