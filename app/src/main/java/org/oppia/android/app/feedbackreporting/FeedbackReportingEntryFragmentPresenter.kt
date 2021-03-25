package org.oppia.android.app.feedbackreporting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.FeedbackReportingEntryFragmentBinding
import javax.inject.Inject

/** Presenter for the [FeedbackReportingEntryFragment] */
class FeedbackReportingEntryFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<FeedbackReportingEntryViewModel>
) {
  private lateinit var binding: FeedbackReportingEntryFragmentBinding
  private lateinit var menu: PopupMenu

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = FeedbackReportingEntryFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot = */ false
    )

    menu = PopupMenu(activity, binding.feedbackReportTypeDropdown)
    menu.inflate(R.menu.feedback_report_type_dropdown_menu)
    val viewModel = getFeedbackReportingEntryViewModel()

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  fun showPopup() {
    menu.show()
  }

  private fun getFeedbackReportingEntryViewModel(): FeedbackReportingEntryViewModel {
    return viewModelProvider.getForFragment(fragment, FeedbackReportingEntryViewModel::class.java)
  }
}
