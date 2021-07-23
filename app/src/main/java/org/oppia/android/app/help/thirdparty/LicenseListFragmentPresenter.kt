package org.oppia.android.app.help.thirdparty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.LicenseItemBinding
import org.oppia.android.databinding.LicenseListFragmentBinding

/** The presenter for [LicenseListFragment]. */
@FragmentScope
class LicenseListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private lateinit var binding: LicenseListFragmentBinding

  /** Handles onCreateView() method of the [LicenseListFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    index: Int
  ): View? {
    val viewModel = LicenseListViewModel(activity, index)

    binding = LicenseListFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.licenseListFragmentRecyclerView.apply {
      layoutManager = LinearLayoutManager(activity.applicationContext)
      adapter = createRecyclerViewAdapter()
    }

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<LicenseItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<LicenseItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = LicenseItemBinding::inflate,
        setViewModel = LicenseItemBinding::setViewModel
      )
      .build()
  }
}