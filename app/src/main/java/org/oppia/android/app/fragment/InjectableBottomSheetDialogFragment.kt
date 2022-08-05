package org.oppia.android.app.fragment

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.oppia.android.app.view.ViewComponent
import org.oppia.android.app.view.ViewComponentBuilderInjector
import org.oppia.android.app.view.ViewComponentFactory

abstract class InjectableBottomSheetDialogFragment : BottomSheetDialogFragment(), ViewComponentFactory {

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