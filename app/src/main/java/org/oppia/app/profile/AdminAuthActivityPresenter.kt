package org.oppia.app.profile

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AdminAuthActivityBinding
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [AdminAuthActivity]. */
@ActivityScope
class AdminAuthActivityPresenter @Inject constructor(
  private val context: Context,
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<AdminAuthViewModel>
) : TextView.OnEditorActionListener, View.OnKeyListener {

  private lateinit var binding: AdminAuthActivityBinding

  private val authViewModel by lazy {
    getAdminAuthViewModel()
  }

  /** Binds ViewModel and sets up text and button listeners. */
  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView<AdminAuthActivityBinding>(activity, R.layout.admin_auth_activity)
    binding.adminAuthToolbar.setNavigationOnClickListener {
      (activity as AdminAuthActivity).finish()
    }
    val adminPin = activity.intent.getStringExtra(KEY_ADMIN_AUTH_ADMIN_PIN)
    binding.apply {
      lifecycleOwner = activity
      viewModel = authViewModel
    }
    binding.inputPin.setOnKeyListener({ v, keyCode, event ->
      binding.submitButton.performClick()
    })
    binding.inputPin.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
        confirmPin?.let {
          authViewModel.errorMessage.set("")
        }
      }

      override fun afterTextChanged(confirmPin: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    binding.submitButton.setOnClickListener {
      val inputPin = binding.inputPin.getInput()
      if (inputPin.isEmpty()) {
        return@setOnClickListener
      }
      if (inputPin == adminPin) {
        activity.startActivity(
          AddProfileActivity.createAddProfileActivityIntent(
            context, activity.intent.getIntExtra(KEY_ADMIN_AUTH_COLOR_RGB, -10710042)
          )
        )
      } else {
        authViewModel.errorMessage.set(activity.resources.getString(R.string.admin_auth_incorrect))
      }
    }
  }

  override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
    return if (actionId == EditorInfo.IME_ACTION_DONE) {
      binding.submitButton.performClick()
      true
    } else false
  }

  override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
    binding.submitButton.performClick()
    return true
  }

  private fun getAdminAuthViewModel(): AdminAuthViewModel {
    return viewModelProvider.getForActivity(activity, AdminAuthViewModel::class.java)
  }
}
