package org.oppia.app.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.databinding.ProfileInputViewBinding

class ProfileInputView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
  private lateinit var label_text: TextView
  private lateinit var input: EditText
  private lateinit var error_text: TextView

  init {
    View.inflate(context, R.layout.profile_input_view, this)
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(
      LayoutInflater.from(context),
      R.layout.profile_input_view, this,
      /* attachToRoot= */ false)
    label_text = binding.labelText
    input = binding.input
    error_text = binding.errorText
    orientation = VERTICAL
  }

  fun setLabelText(label: String) {
    label_text.text = label
  }
}

@BindingAdapter("profile:label")
fun setLabelText(profileInputView: ProfileInputView, label: String) {
  profileInputView.setLabelText(label)
}