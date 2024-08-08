package org.oppia.android.app.settings.profile

import android.content.Intent
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.databinding.ProfileRenameFragmentBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [ProfileRenameFragment]. */
@FragmentScope
class ProfileRenameFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val renameViewModel: ProfileRenameViewModel,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding: ProfileRenameFragmentBinding

  /** Handles onCreateView() method of [ProfileRenameFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: Int
  ): View? {
    binding = ProfileRenameFragmentBinding.inflate(
      inflater,
      container,
      false
    )
    binding.apply {
      viewModel = renameViewModel
      lifecycleOwner = fragment
    }
    binding.profileRenameSaveButton.setOnClickListener {
      renameViewModel.nameErrorMsg.set("")
      if (binding.profileRenameInputEditText.text.toString().isEmpty()) {
        renameViewModel
          .nameErrorMsg
          .set(
            resourceHandler.getStringInLocale(
              R.string.add_profile_error_name_empty
            )
          )
        return@setOnClickListener
      }
      profileManagementController
        .updateName(
          ProfileId.newBuilder().setLoggedInInternalProfileId(profileId).build(),
          binding.profileRenameInputEditText.text.toString()
        ).toLiveData()
        .observe(
          fragment,
          {
            handleAddProfileResult(it, profileId)
          }
        )
    }

    binding.profileRenameInputEditText.onTextChanged { name ->
      name?.let {
        if (
          renameViewModel.nameErrorMsg.get()?.isNotEmpty()!! &&
          renameViewModel.inputName.get() == it
        ) {
          renameViewModel.inputName.set(it)
        } else {
          renameViewModel.nameErrorMsg.set("")
          renameViewModel.inputName.set(it)
        }
      }
    }

    binding.profileRenameInputEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        binding.profileRenameSaveButton.callOnClick()
      }
      false
    }
    return binding.root
  }

  private fun handleAddProfileResult(result: AsyncResult<Any?>, profileId: Int) {
    if (result is AsyncResult.Success) {
      val intent = ProfileEditActivity.createProfileEditActivity(activity, profileId)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      activity.startActivity(intent)
    } else if (result is AsyncResult.Failure) {
      when (result.error) {
        is ProfileManagementController.ProfileNameNotUniqueException ->
          renameViewModel.nameErrorMsg.set(
            resourceHandler.getStringInLocale(
              R.string.add_profile_error_name_not_unique
            )
          )
        is ProfileManagementController.ProfileNameOnlyLettersException ->
          renameViewModel.nameErrorMsg.set(
            resourceHandler.getStringInLocale(
              R.string.add_profile_error_name_only_letters
            )
          )
      }
    }
  }
}
