package org.oppia.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.state_fragment.view.*
import org.oppia.app.application.ApplicationContext
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.data.backends.gae.model.GaeCustomizationArgs
import javax.inject.Inject

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<StateViewModel>
) {
  private lateinit var binding: StateFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
     binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.viewModel = getStateViewModel()
    }
    showInputInteractions()
    return binding.root
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    getStateViewModel().setAudioFragmentVisible(isVisible)
  }

  private fun showInputInteractions() {

    //Todo(Veena): Remove static initialization and use values from constructor for gaeCustomizationArgsMap and interactionInstanceId
    var gaeCustomizationArgsMap = HashMap<String, GaeCustomizationArgs>()
    var interactionInstanceId = "MultipleChoiceInput"
    var sampleData: GaeCustomizationArgs =
      GaeCustomizationArgs(true, "<p>The numerator.</p>, <p>The denominator.</p>, <p>I can't remember!</p>]")
    gaeCustomizationArgsMap?.put("choices", sampleData)
    interactionInstanceId = "MultipleChoiceInput"

    // Todo(veena): Keep the below code for actual implementation
    val gaeCustomizationArgs: Any? = gaeCustomizationArgsMap!!.get("choices")?.value

    if (interactionInstanceId.equals("MultipleChoiceInput")) {
      val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
      var items = gaeCustomArgsInString.split(",").toTypedArray()
      addRadioButtons(items)
    } else if (interactionInstanceId.equals("ItemSelectionInput") || interactionInstanceId.equals("SingleChoiceInput")) {
      val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
      var items = gaeCustomArgsInString.split(",").toTypedArray()
      addCheckbox(items)
    } else {
      //Do no show any view
    }
  }

  fun addCheckbox(optionsArray: Array<String>) {
    for (row in 0..0) {
      for (i in 0..optionsArray.size - 1) {
        val cb = CustomCheckbox(context, optionsArray[i])
        binding.root.interactionContainer.addView(cb)
      }
    }
  }

  fun addRadioButtons(optionsArray: Array<String>) {
    for (row in 0..0) {
      val rg = RadioGroup(context)
      rg.orientation = LinearLayout.VERTICAL

      for (i in 0..optionsArray.size - 1) {
        val rdbtn = CustomRadioButton(context, optionsArray[i])

        rg.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
          override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
            for (i in 0 until group.childCount) {
              val btn = group.getChildAt(i) as RadioButton
              if (btn.id == checkedId) {
                val text = btn.text
                Toast.makeText(context, "" + text, Toast.LENGTH_LONG).show()
                return
              }
            }
          }
        })
        rg.addView(rdbtn)
      }
      binding.root.interactionRadioGroup.addView(rg)
    }
  }

}
