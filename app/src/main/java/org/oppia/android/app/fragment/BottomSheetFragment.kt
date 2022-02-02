package org.oppia.android.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.oppia.android.R
import org.oppia.android.app.player.exploration.BottomSheetItemClickListener
import org.oppia.android.databinding.FragmentBottomSheetBinding

class BottomSheetFragment(val internalProfileId: Int) : BottomSheetDialogFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
    setUpOnClickListeners(binding)
    return binding.root
  }

  private fun setUpOnClickListeners(binding: FragmentBottomSheetBinding) {
    val bottomSheetItemClickListener = activity as BottomSheetItemClickListener
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
