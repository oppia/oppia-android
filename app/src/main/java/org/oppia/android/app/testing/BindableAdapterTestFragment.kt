package org.oppia.app.testing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

const val BINDABLE_TEST_FRAGMENT_TAG = "bindable_adapter_test_fragment"

// TODO(#59): Make this fragment only included in relevant tests instead of all prod builds.
/** A test fragment for the bindable RecyclerView adapter. */
class BindableAdapterTestFragment : InjectableFragment() {
  @Inject
  lateinit var bindableAdapterTestFragmentPresenter: BindableAdapterTestFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return bindableAdapterTestFragmentPresenter.handleCreateView(inflater, container)
  }
}
