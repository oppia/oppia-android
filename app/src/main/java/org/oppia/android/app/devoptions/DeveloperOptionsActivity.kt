package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity

class DeveloperOptionsActivity: InjectableAppCompatActivity() {
  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(DEVELOPER_OPTIONS_ACTIVITY_PROFILE_ID_KEY, -1)
  }

  companion object {
    const val DEVELOPER_OPTIONS_ACTIVITY_PROFILE_ID_KEY =
      "DeveloperOptionsActivity.internal_profile_id"

    fun createDeveloperOptionsActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, DeveloperOptionsActivity::class.java)
      intent.putExtra(DEVELOPER_OPTIONS_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}