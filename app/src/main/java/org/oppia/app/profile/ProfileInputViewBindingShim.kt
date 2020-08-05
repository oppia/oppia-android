package org.oppia.app.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.oppia.app.R
import org.oppia.app.databinding.ProfileInputViewBinding

class ProfileInputViewBindingShim : ProfileInputVieBindingShimInterface {

  override fun provideProfileInputViewBindingLabelText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): TextView {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(inflater,
      R.layout.profile_input_view,parent,attachToParent)
    return binding.labelText
  }

  override fun provideProfileInputViewBindingInput(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): EditText {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(inflater,
      R.layout.profile_input_view,parent,attachToParent)
    return binding.input
  }

  override fun provideProfileInputViewBindingErrorText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): TextView {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(inflater,
      R.layout.profile_input_view,parent,attachToParent)
    return binding.errorText
  }

}
