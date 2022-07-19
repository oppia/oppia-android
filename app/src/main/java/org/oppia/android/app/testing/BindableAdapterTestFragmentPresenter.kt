package org.oppia.android.app.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.TestFragmentBinding
import javax.inject.Inject

/** The test-only fragment presenter corresponding to [BindableAdapterTestFragment]. */
class BindableAdapterTestFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val singleTypeBuilder: BindableAdapter.SingleTypeBuilder.Factory,
  private val multiTypeBuilder: BindableAdapter.MultiTypeBuilder.Factory,
  private val testBindableAdapterFactory: BindableAdapterFactory,
  @VisibleForTesting val viewModel: BindableAdapterTestViewModel
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TestFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.testRecyclerView.apply {
      adapter = testBindableAdapterFactory.create(singleTypeBuilder, multiTypeBuilder)
    }
    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  /** Factory for creating new [BindableAdapter]s for the current fragment. */
  interface BindableAdapterFactory {
    fun create(
      singleTypeBuilder: BindableAdapter.SingleTypeBuilder.Factory,
      multiTypeBuilder: BindableAdapter.MultiTypeBuilder.Factory
    ): BindableAdapter<BindableAdapterTestDataModel>
  }
}
