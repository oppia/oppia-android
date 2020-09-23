package org.oppia.android.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import org.oppia.android.databinding.TestFragmentBinding
import org.oppia.android.app.model.TestModel
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The test-only fragment presenter corresponding to [BindableAdapterTestFragment]. */
class BindableAdapterTestFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<BindableAdapterTestViewModel>
) {
  @VisibleForTesting
  val viewModel: BindableAdapterTestViewModel by lazy {
    getBindableAdapterTestViewModel()
  }

  companion object {
    // TODO(#59): Move away from this fragile static testing state by leveraging a test-only DI graph that can be
    //  configured within tests to provide the bindable adapter to be used by this presenter.
    var testBindableAdapter: BindableAdapter<TestModel>? = null
  }

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TestFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    checkNotNull(testBindableAdapter) {
      "Expected a bindable adapter to be provided in a test module"
    }
    binding.testRecyclerView.apply {
      adapter = testBindableAdapter
    }
    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getBindableAdapterTestViewModel(): BindableAdapterTestViewModel {
    return viewModelProvider.getForFragment(fragment, BindableAdapterTestViewModel::class.java)
  }
}
