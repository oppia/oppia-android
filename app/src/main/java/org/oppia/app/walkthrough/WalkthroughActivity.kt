package org.oppia.app.walkthrough

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that contains the walkthrough flow for users. */
class WalkthroughActivity : InjectableAppCompatActivity() {
  @Inject lateinit var walkthroughActivityPresenter: WalkthroughActivityPresenter

  companion object {
    fun createWalkthroughActivity(context: Context): Intent {
      val intent = Intent(context, WalkthroughActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    walkthroughActivityPresenter.handleOnCreate()
  }
}
