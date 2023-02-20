package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that contains Profile Edit Screen. */
class ProfileEditFragment :
  InjectableFragment(),
  ProfileEditDialogInterface,
  LoadProfileEditDeletionDialogListener {
  @Inject
  lateinit var profileEditFragmentPresenter: ProfileEditFragmentPresenter

  companion object {
    /** Argument key for the Multipane in tablet mode for [ProfileEditFragment]. */
    const val IS_MULTIPANE_EXTRA_KEY = "ProfileEditActivity.isMultipane"

    /** This creates the new instance of [ProfileEditFragment]. */
    fun newInstance(
      profileId: ProfileId,
      isMultipane: Boolean
    ): ProfileEditFragment {
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
      args.putBoolean(IS_MULTIPANE_EXTRA_KEY, isMultipane)
      val fragment = ProfileEditFragment()
      fragment.arguments = args
      return fragment
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
    val args = checkNotNull(arguments) {
      "Expected variables to be passed to ProfileEditFragment"
    }
    val profileId = args.extractCurrentUserProfileId()
    val isMultipane = args.getBoolean(IS_MULTIPANE_EXTRA_KEY)
    return profileEditFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      profileId,
      isMultipane
    )
  }

  override fun deleteProfileByInternalProfileId(profileId: ProfileId) {
    profileEditFragmentPresenter.deleteProfile(profileId)
  }

  override fun loadProfileEditDeletionDialog(profileId: ProfileId) {
    profileEditFragmentPresenter.handleLoadProfileDeletionDialog(profileId)
  }
}
