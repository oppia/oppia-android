package org.oppia.android.app.devoptions.forcenetworktype

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for Force Network Type. */
class ForceNetworkTypeActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var forceNetworkTypeActivityPresenter: ForceNetworkTypeActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    forceNetworkTypeActivityPresenter.handleOnCreate()
    title = getString(R.string.force_network_type_activity_title)
  }

  companion object {
    /** Returns [Intent] for [ForceNetworkTypeActivity]. */
    fun createForceNetworkTypeActivityIntent(context: Context): Intent {
      return Intent(context, ForceNetworkTypeActivity::class.java)
    }
  }
}
