package org.oppia.app.application

import android.app.Application
import android.util.Log

import com.hypertrack.hyperlog.HyperLog

class OppiaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        HyperLog.initialize(this)
//        HyperLog.setURL("<Set URL>")    // if log has to be posted to server. Set the url here
    }
}
