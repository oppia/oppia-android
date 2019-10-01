package org.oppia.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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

  private var items: Array<String>? = null
  var gaeCustomizationArgsMap = HashMap<String, GaeCustomizationArgs>()
  var interactionInstanceId: String? = null
  private val entity_type: String = "exploration"
  private val entity_id: String = "umPkwp0L1M0-"

  var interactionAdapter: InteractionAdapter? = null

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.viewModel = getStateViewModel()
    }
    binding.rvInteractions.apply {
      binding.rvInteractions.layoutManager = LinearLayoutManager(context)
    }
    createDummyData()
    showInputInteractions(binding)
    return binding.root
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    getStateViewModel().setAudioFragmentVisible(isVisible)
  }

  private fun createDummyData() {
    interactionInstanceId = "MultipleChoiceInput"
    var sampleData: GaeCustomizationArgs =
      GaeCustomizationArgs(true, "<p>The numerator.</p>, <p>The denominator.</p>, <p>I can't remember!</p>]")
    gaeCustomizationArgsMap?.put("choices", sampleData)
  }

  private fun showInputInteractions(binding: StateFragmentBinding) {
    val gaeCustomizationArgs: Any? = gaeCustomizationArgsMap!!.get("choices")?.value
    if (interactionInstanceId.equals("MultipleChoiceInput")) {
      val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
      items = gaeCustomArgsInString.split(",").toTypedArray()
      interactionAdapter = InteractionAdapter(context, entity_type, entity_id, items, interactionInstanceId);
      binding.rvInteractions.adapter = interactionAdapter

    } else if (interactionInstanceId.equals("ItemSelectionInput") || interactionInstanceId.equals("SingleChoiceInput")) {
      val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
      items = gaeCustomArgsInString.split(",").toTypedArray()
      interactionAdapter = InteractionAdapter(context, entity_type, entity_id, items, interactionInstanceId);
      binding.rvInteractions.adapter = interactionAdapter
    } else {
      //Do no show any view
    }
  }
}
