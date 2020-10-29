package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

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
        BINDABLE_TEST_ARGUMENT_KEY
      )
      .commitNow()
  }
}
