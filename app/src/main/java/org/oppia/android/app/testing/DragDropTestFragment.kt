package org.oppia.android.app.testing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

const val DRAG_DROP_TEST_FRAGMENT_TAG = "drag_drop_adapter_test_fragment"

class DragDropTestFragment : InjectableFragment() {

  @Inject
  lateinit var dragDropTestFragmentPresenter: DragDropTestFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as TestInjector).inject(this)

  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return dragDropTestFragmentPresenter.handleCreateView(inflater, container)
  }

  /** Test-only injector for the fragment that needs to be set up in the test. */
  interface TestInjector {
    fun inject(dragDropTestFragment: DragDropTestFragment)
  }
}
