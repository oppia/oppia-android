package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.profile.ProfileInputView
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ProfileRenameActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [ProfileRenameActivity]. */
@ActivityScope
class ProfileRenameActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<ProfileRenameViewModel>
) {
  private val renameViewModel: ProfileRenameViewModel by lazy {
    getProfileRenameViewModel()
  }

  fun handleOnCreate() {
    activity.title = activity.getString(R.string.profile_rename_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding =
      DataBindingUtil.setContentView<ProfileRenameActivityBinding>(
        activity,
        R.layout.profile_rename_activity
      )
    val profileId = activity.intent.getIntExtra(PROFILE_RENAME_PROFILE_ID_EXTRA_KEY, 0)

    binding.apply {
      viewModel = renameViewModel
      lifecycleOwner = activity
    }

    binding.profileRenameToolbar.setNavigationOnClickListener {
      (activity as ProfileRenameActivity).finish()
    }

    binding.profileRenameSaveButton.setOnClickListener {
      renameViewModel.nameErrorMsg.set("")
      val imm = activity.getSystemService(
        Context.INPUT_METHOD_SERVICE
      ) as? InputMethodManager
      imm?.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
      val name = binding.inputName.getInput()
      if (name.isEmpty()) {
        renameViewModel.nameErrorMsg.set(
          activity.resources.getString(
            R.string.add_profile_error_name_empty
          )
        )
        return@setOnClickListener
      }
      profileManagementController
        .updateName(ProfileId.newBuilder().setInternalId(profileId).build(), name).toLiveData()
        .observe(
          activity,
          Observer {
            handleAddProfileResult(it, profileId)
          }
        )
    }

    binding.inputName.post {
      addTextChangedListener(binding.inputName) { name ->
        name?.let {
          renameViewModel.nameErrorMsg.set("")
          renameViewModel.inputName.set(it.toString())
        }
      }
    }

    binding.inputName.setInput(renameViewModel.inputName.get().toString())
  }

  private fun handleAddProfileResult(result: AsyncResult<Any?>, profileId: Int) {
    if (result.isSuccess()) {
      val intent = ProfileEditActivity.createProfileEditActivity(activity, profileId)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      activity.startActivity(intent)
    } else if (result.isFailure()) {
      when (result.getErrorOrNull()) {
        is ProfileManagementController.ProfileNameNotUniqueException ->
          renameViewModel.nameErrorMsg.set(
            activity.resources.getString(
              R.string.add_profile_error_name_not_unique
            )
          )
        is ProfileManagementController.ProfileNameOnlyLettersException ->
          renameViewModel.nameErrorMsg.set(
            activity.resources.getString(
              R.string.add_profile_error_name_only_letters
            )
          )
      }
    }
  }

  private fun addTextChangedListener(
    profileInputView: ProfileInputView,
    onTextChanged: (CharSequence?) -> Unit
  ) {
    profileInputView.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        onTextChanged(p0)
      }

      override fun afterTextChanged(p0: Editable?) {}
      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    })
  }

  private fun getProfileRenameViewModel(): ProfileRenameViewModel {
    return viewModelProvider.getForActivity(activity, ProfileRenameViewModel::class.java)
  }
}
