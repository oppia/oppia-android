package org.oppia.android.app.testing

import android.content.Context
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.utility.SplitScreenManager
import javax.inject.Inject

/** Test-only activity that's used in conjunction with [ExplorationTestActivity]. */
class ExplorationTestActivityFragment : InjectableFragment() {
  @Inject
  lateinit var splitScreenManager: SplitScreenManager

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  /** Dagger injector for [ExplorationTestActivityFragment]. */
  interface Injector {
    /** Injects dependencies into the [fragment]. */
    fun inject(fragment: ExplorationTestActivityFragment)
  }
}
