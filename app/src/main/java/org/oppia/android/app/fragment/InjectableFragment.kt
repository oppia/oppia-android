package org.oppia.android.app.fragment

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import org.oppia.android.app.view.ViewComponent
import org.oppia.android.app.view.ViewComponentBuilderInjector
import org.oppia.android.app.view.ViewComponentFactory

/**
 * A fragment that facilitates field injection to children. This fragment can only be used with
 * [org.oppia.android.app.utility.activity.InjectableAppCompatActivity] contexts.
 */
abstract class InjectableFragment : Fragment(), ViewComponentFactory {
  /**
   * The [FragmentComponent] corresponding to this fragment. This cannot be used before [onAttach]
   * is called, and can be used to inject lateinit fields in child fragments during fragment
   * attachment (which is recommended to be done in an override of [onAttach]).
   */
  lateinit var fragmentComponent: FragmentComponent

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent =
      (requireActivity() as FragmentComponentFactory).createFragmentComponent(this)
  }

  override fun createViewComponent(view: View): ViewComponent {
    val builderInjector = fragmentComponent as ViewComponentBuilderInjector
    return builderInjector.getViewComponentBuilderProvider().get().setView(view).build()
  }
}
