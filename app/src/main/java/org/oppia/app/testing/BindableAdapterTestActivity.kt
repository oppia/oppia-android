package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.ui.R
import org.oppia.app.activity.InjectableAppCompatActivity

// TODO(#59): Make this activity only included in relevant tests instead of all prod builds.
/** A test activity for the bindable RecyclerView adapter. */
class BindableAdapterTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    setContentView(R.layout.test_activity)
    supportFragmentManager.beginTransaction()
      .add(
        R.id.test_fragment_placeholder,
        BindableAdapterTestFragment(),
        BINDABLE_TEST_FRAGMENT_TAG
      )
      .commitNow()
  }
}
