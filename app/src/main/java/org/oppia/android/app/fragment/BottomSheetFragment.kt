package org.oppia.android.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.oppia.android.R
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.databinding.FragmentBottomSheetBinding

class BottomSheetFragment(val internalProfileId : Int) : BottomSheetDialogFragment() {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val binding = FragmentBottomSheetBinding.inflate(inflater,container,false)
    setUpOnClickListeners(binding)
    return binding.root
  }

  private fun setUpOnClickListeners(binding: FragmentBottomSheetBinding){
    binding.actionHelp.setOnClickListener {

    }
    binding.actionOptions.setOnClickListener {
      val intent = OptionsActivity.createOptionsActivity(
        requireActivity(),
        internalProfileId,
        /* isFromNavigationDrawer= */ false
      )
//      fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE.name)
//      context.startActivity(intent)
    }
  }
}