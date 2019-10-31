package org.oppia.app.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.view.ViewComponent

/**
 * A fragment that facilitates field injection to children. This fragment can only be used with
 * [InjectableAppCompatActivity] contexts.
 */
abstract class InjectableFragment: Fragment() {
  /**
   * The [FragmentComponent] corresponding to this fragment. This cannot be used before [onAttach] is called, and can be
   * used to inject lateinit fields in child fragments during fragment attachment (which is recommended to be done in an
   * override of [onAttach]).
   */
  lateinit var fragmentComponent: FragmentComponent

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent = (requireActivity() as InjectableAppCompatActivity).createFragmentComponent(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  fun createViewComponent(view: View): ViewComponent {
    return fragmentComponent.getViewComponentBuilderProvider().get().setView(view).build()
  }
}
