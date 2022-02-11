package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
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
      internalProfileId: Int,
      isMultipane: Boolean
    ): ProfileEditFragment {
      val args = Bundle()
      args.putInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, internalProfileId)
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
    val internalProfileId = args.getInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY)
    val isMultipane = args.getBoolean(IS_MULTIPANE_EXTRA_KEY)
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

  override fun loadProfileEditDeletionDialog(internalProfileId: Int) {
    profileEditFragmentPresenter.handleLoadProfileDeletionDialog(internalProfileId)
  }
}
