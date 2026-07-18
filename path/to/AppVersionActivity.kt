# complete code
package org.oppia.android.app.administratorcontrols.appversion

import org.oppia.android.app.injector.InjectableAppCompatActivity

class AppVersionActivity : InjectableAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_version)
    }
}