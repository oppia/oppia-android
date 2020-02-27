package org.oppia.app.help.FAQ

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class FAQActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var FAQActivityPresenter: FAQActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    FAQActivityPresenter.handleOnCreate()
    title = getString(R.string.frequently_asked_questions)
  }

  companion object {
    fun createFAQActivityIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, FAQActivity::class.java)
      return intent
    }
  }
}
