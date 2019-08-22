package org.oppia.app.utility

import android.content.Context
import android.util.Log
import com.hypertrack.hyperlog.HyperLog

object Logger {

    fun i(context: Context, tag: String, message: String) {

        Log.i(tag, message)
        HyperLog.setLogLevel(Log.INFO)
        HyperLog.i(tag, message)
        val file = HyperLog.getDeviceLogsInFile(context)
    }

    fun e(context: Context, tag: String, message: String) {

        Log.e(tag, message)
        HyperLog.setLogLevel(Log.ERROR)
        HyperLog.e(tag, message)
        val file = HyperLog.getDeviceLogsInFile(context)

    }

    fun w(context: Context, tag: String, message: String) {

        Log.w(tag, message)
        HyperLog.setLogLevel(Log.WARN)
        HyperLog.w(tag, message)
        val file = HyperLog.getDeviceLogsInFile(context)

    }

    fun v(context: Context, tag: String, message: String) {

        Log.v(tag, message)
        HyperLog.setLogLevel(Log.VERBOSE)
        HyperLog.v(tag, message)
        val file = HyperLog.getDeviceLogsInFile(context)

    }

    fun d(context: Context, tag: String, message: String) {

        Log.d(tag, message)
        HyperLog.setLogLevel(Log.DEBUG)
        HyperLog.d(tag, message)
        val file = HyperLog.getDeviceLogsInFile(context)

        Log.d(tag, file.absolutePath);

    }


}
