package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileEditFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that contains Profile Edit Screen. */
class ProfileEditFragment :
  InjectableFragment(),
  ProfileEditDialogInterface,
  LoadProfileEditDeletionDialogListener {
  @Inject
  lateinit var profileEditFragmentPresenter: ProfileEditFragmentPresenter

  companion object {
    /** Arguments key for ProfileEditFragment. */
    const val PROFILE_EDIT_FRAGMENT_ARGUMENTS_KEY = "ProfileEditFragment.arguments"

    /** This creates the new instance of [ProfileEditFragment]. */
    fun newInstance(
      internalProfileId: Int,
      isMultipane: Boolean
    ): ProfileEditFragment {
      val args = ProfileEditFragmentArguments.newBuilder().apply {
        this.internalProfileId = internalProfileId
        this.isMultipane = isMultipane
      }.build()

      return ProfileEditFragment().apply {
        arguments = Bundle().apply {
          putProto(PROFILE_EDIT_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val arguments = checkNotNull(arguments) {
      "Expected variables to be passed to ProfileEditFragment"
    }
    val args = arguments.getProto(
      PROFILE_EDIT_FRAGMENT_ARGUMENTS_KEY,
      ProfileEditFragmentArguments.getDefaultInstance()
    )

    val internalProfileId = args.internalProfileId
    val isMultipane = args.isMultipane
    return profileEditFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      internalProfileId,
      isMultipane
    )
  }

  override fun deleteProfileByInternalProfileId(internalProfileId: Int) {
    profileEditFragmentPresenter.deleteProfile(internalProfileId)
  }

  override fun loadProfileEditDeletionDialog(profileId: ProfileId) {
    profileEditFragmentPresenter.handleLoadProfileDeletionDialog(profileId.internalId)
  }
}
