package org.oppia.android.app.player.exploration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.InjectableBottomSheetDialogFragment
import org.oppia.android.databinding.BottomSheetOptionsMenuFragmentBinding

/** Bottom sheet fragment for displaying options menu */
class BottomSheetOptionsMenu : InjectableBottomSheetDialogFragment() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val binding = BottomSheetOptionsMenuFragmentBinding.inflate(inflater, container, false)
    setUpOnClickListeners(binding)
    return binding.root
  }

  private fun setUpOnClickListeners(binding: BottomSheetOptionsMenuFragmentBinding) {
    val bottomSheetItemClickListener = activity as BottomSheetOptionsMenuItemClickListener
    binding.actionHelp.setOnClickListener {
      bottomSheetItemClickListener.handleOnOptionsItemSelected(R.id.action_help)
      dismiss()
    }
    binding.actionOptions.setOnClickListener {
      bottomSheetItemClickListener.handleOnOptionsItemSelected(R.id.action_options)
      dismiss()
    }
    binding.actionClose.setOnClickListener {
      dismiss()
    }
  }
}
