package org.oppia.android.app.fragment

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.oppia.android.app.view.ViewComponent
import org.oppia.android.app.view.ViewComponentBuilderInjector
import org.oppia.android.app.view.ViewComponentFactory

/**
 * A fragment that facilitates field injection to children. This fragment can only be used with
 * [org.oppia.android.app.activity.InjectableAppCompatActivity] contexts.
 */
abstract class InjectableBottomSheetDialogFragment :
  BottomSheetDialogFragment(),
  ViewComponentFactory {
  /**
   * The [FragmentComponent] corresponding to this fragment. This cannot be used before [onAttach]
   * is called, and can be used to inject lateinit fields in child fragments during fragment
   * attachment (which is recommended to be done in an override of [onAttach]).
   */
  lateinit var bottomSheetFragmentComponent: FragmentComponent

  override fun onAttach(context: Context) {
    super.onAttach(context)
    bottomSheetFragmentComponent =
      (requireActivity() as FragmentComponentFactory).createFragmentComponent(this)
  }

  override fun createViewComponent(view: View): ViewComponent {
    val builderInjector = bottomSheetFragmentComponent as ViewComponentBuilderInjector
    return builderInjector.getViewComponentBuilderProvider().get().setView(view).build()
  }
}
