package org.oppia.android.testing.activity

import android.content.Context
import android.content.Intent
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.datetime.DateTimeUtil

// TODO: document the two uses of this activity (as a superclass & as a dedicated)
// TODO: file an issue to migrate other tests to just use this & remove the open contract
open class TestActivity: InjectableAppCompatActivity() {
  companion object {
    fun createIntent(context: Context): Intent = Intent(context, TestActivity::class.java)
  }

  fun getAppLanguageResourceHandler(): AppLanguageResourceHandler =
    activityComponent.getAppLanguageResourceHandler()

  fun getDateTimeUtil(): DateTimeUtil = activityComponent.getDateTimeUtil()
}
