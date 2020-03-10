package org.oppia.app.help.faq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The FAQ page activity for placement of different FAQs. */
class FAQActivity : InjectableAppCompatActivity() {

  @Inject lateinit var faqActivityPresenter: FAQActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    faqActivityPresenter.handleOnCreate()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_faq_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  companion object {
    fun createFAQActivityIntent(context: Context): Intent {
      val intent = Intent(context, FAQActivity::class.java)
      return intent
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}
