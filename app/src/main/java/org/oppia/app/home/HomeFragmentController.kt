package org.oppia.app.home

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.UserAppHistoryController
import javax.inject.Inject
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import org.oppia.app.R
import org.oppia.app.customview.inputInteractionView.TextInputInteractionView

/** The controller for [HomeFragment]. */
@FragmentScope
class HomeFragmentController @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<UserAppHistoryViewModel>,
  private val userAppHistoryController: UserAppHistoryController
) {
  private var llRoot: LinearLayout? = null

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.viewModel = getUserAppHistoryViewModel()
      it.lifecycleOwner = fragment as LifecycleOwner?
    }
    userAppHistoryController.markUserOpenedApp()
    llRoot = binding.root.findViewById(R.id.llRoot)
    addContentCard("edit_text_background_border",1)
    addContentCard("edit_text_background_border_",2)
    return binding.root
  }

  private fun addContentCard(placeholder: String, rows: Int) {
    val contentComponent = TextInputInteractionView(
      fragment.context!!,
      placeholder,
      rows
    )
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    )

    params.setMargins( dpToPx(8), dpToPx(8),  dpToPx(8), dpToPx(8))


    llRoot!!.addView(contentComponent, params)



  }

  fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().getDisplayMetrics().density).toInt()
  }

  private fun getUserAppHistoryViewModel(): UserAppHistoryViewModel {
    return viewModelProvider.getForFragment(fragment, UserAppHistoryViewModel::class.java)
  }
}
