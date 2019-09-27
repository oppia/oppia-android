package org.oppia.app.player.state

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.customview.inputInteractionView.FractionInputInteractionView
import org.oppia.app.customview.inputInteractionView.NumberInputInteractionView
import org.oppia.app.customview.inputInteractionView.TextInputInteractionView
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<StateViewModel>
) {
  private var llRoot: LinearLayout? = null

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.viewModel = getStateViewModel()
    }
    llRoot = binding.root.findViewById(R.id.llRoot)
    addTextInputContentCard("text", 1)
    addNumberInputContentCard("number 1", 2)
    addFractionInputContentCard("fraction 1/1", 3)
    return binding.root
  }

  /** The function for adding [TextInputInteractionView]*/
  fun addTextInputContentCard(placeholder: String, type: Int) {
    var contentComponent = TextInputInteractionView(
      fragment.context!!,
      placeholder,
      1
    )
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
    llRoot!!.addView(contentComponent, params)
  }

  /** The function for adding [NumberInputInteractionView]*/
  fun addNumberInputContentCard(placeholder: String, type: Int) {
    var contentComponent = NumberInputInteractionView(
      fragment.context!!,
      placeholder,
      1
    )
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
    llRoot!!.addView(contentComponent, params)

  }

  /** The function for adding [FractionInputInteractionView]*/
  fun addFractionInputContentCard(placeholder: String, type: Int) {
    var contentComponent = FractionInputInteractionView(
      fragment.context!!,
      placeholder,
      1
    )
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
    llRoot!!.addView(contentComponent, params)
  }

  fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().getDisplayMetrics().density).toInt()
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    getStateViewModel().setAudioFragmentVisible(isVisible)
  }
}
