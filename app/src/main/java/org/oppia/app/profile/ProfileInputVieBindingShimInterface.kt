package org.oppia.app.profile

import android.renderscript.ScriptGroup
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.ViewDataBinding

interface ProfileInputVieBindingShimInterface {

  fun provideProfileInputViewBindingLabelText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): TextView

  fun provideProfileInputViewBindingInput(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): EditText

  fun provideProfileInputViewBindingErrorText(
    inflater: LayoutInflater,
    parent: ViewGroup,
    attachToParent: Boolean): TextView

}