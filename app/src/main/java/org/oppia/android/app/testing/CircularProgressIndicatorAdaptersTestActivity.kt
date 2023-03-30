package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.databinding.CircularProgressIndicatorAdaptersTestActivityBinding

/** Test activity for [org.oppia.android.app.databinding.CircularProgressIndicatorAdaptersTest]. */
class CircularProgressIndicatorAdaptersTestActivity : InjectableAppCompatActivity() {
  /** View model to use in data-bound circular progress indicators in XML. */
  val viewModel by lazy { CircularProgressIndicatorAdaptersTestViewModel() }

  /** The [ViewDataBinding] corresponding to this activity's content view layout. */
  lateinit var binding: ViewDataBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView<CircularProgressIndicatorAdaptersTestActivityBinding>(
      this, R.layout.circular_progress_indicator_adapters_test_activity
    ).apply {
      this.viewModel = this@CircularProgressIndicatorAdaptersTestActivity.viewModel
      this.lifecycleOwner = this@CircularProgressIndicatorAdaptersTestActivity
    }
  }

  companion object {
    /**
     * Returns a new [Intent] for the given [Context] to launch new
     * [CircularProgressIndicatorAdaptersTestActivity]s.
     */
    fun createIntent(context: Context): Intent =
      Intent(context, CircularProgressIndicatorAdaptersTestActivity::class.java)
  }
}
