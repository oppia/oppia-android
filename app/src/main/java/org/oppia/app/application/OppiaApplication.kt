package org.oppia.app.application

import android.Manifest.permission.*
import android.app.Application
import android.os.Environment
import org.oppia.app.utility.PermissionUtils

import java.io.File
import java.io.IOException

/** Called when the application is starting, before any activity, service, or receiver objects (excluding content providers) have been created.
 */
class OppiaApplication : Application() {

  /* Checks if external storage is available for read and write */
  val isExternalStorageWritable: Boolean
    get() {
      val state = Environment.getExternalStorageState()
      return if (Environment.MEDIA_MOUNTED == state) {
        true
      } else false
    }

  /* Checks if external storage is available to at least read */
  val isExternalStorageReadable: Boolean
    get() {
      val state = Environment.getExternalStorageState()
      return if (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state) {
        true
      } else false
    }

  override fun onCreate() {
    super.onCreate()
    PermissionUtils.checkPermissions(applicationContext, *permissions)
    PermissionUtils.requestPermissions(applicationContext, 1, *permissions)

    if (isExternalStorageWritable) {
      val appDirectory = File(applicationContext.getExternalFilesDir(null), "OppiaLogFile")
      val logDirectory = File("$appDirectory/log")
      val logFile = File(logDirectory, "logcat" + System.currentTimeMillis() + ".txt")

      // create app folder
      if (!appDirectory.exists()) {
        appDirectory.mkdir()
      }

      // create log folder
      if (!logDirectory.exists()) {
        logDirectory.mkdir()
      }

      // clear the previous logcat and then write the new one to the file
      try {
        var process = Runtime.getRuntime().exec("logcat -c")
        process = Runtime.getRuntime().exec("logcat -f $logFile")
      } catch (e: IOException) {
        e.printStackTrace()
      }

    } else if (isExternalStorageReadable) {
      // only readable
    } else {
      // not accessible
    }
  }

  companion object {
    internal var permissions = arrayOf(READ_LOGS, WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE)
  }
}

