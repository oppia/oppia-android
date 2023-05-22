package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    // TODO(#4986): Remove the constants corresponding to bundles.
    private const val IS_MULTIPANE_EXTRA_KEY = "ProfileEditActivity.isMultipane"
    private const val AUTO_UPDATE_ACTIVITY_TITLE = "ProfileEditActivity.autoUpdateActivityTitle"
    private const val PROFILE_EDIT_PROFILE_ID_EXTRA_KEY =
      "ProfileEditActivity.profile_edit_profile_id"

    /** This creates the new instance of [ProfileEditFragment]. */
    fun newInstance(
      internalProfileId: Int,
      isMultipane: Boolean,
      autoUpdateActivityTitle: Boolean
    ): ProfileEditFragment {
      val args = Bundle()
      args.putInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, internalProfileId)
      args.putBoolean(IS_MULTIPANE_EXTRA_KEY, isMultipane)
      args.putBoolean(AUTO_UPDATE_ACTIVITY_TITLE, autoUpdateActivityTitle)
      val fragment = ProfileEditFragment()
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
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
    val autoUpdateActivityTitle = args.getBoolean(IS_MULTIPANE_EXTRA_KEY)
    return profileEditFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      internalProfileId,
      isMultipane,
      autoUpdateActivityTitle
    )
  }

  override fun deleteProfileByInternalProfileId(internalProfileId: Int) {
    profileEditFragmentPresenter.deleteProfile(internalProfileId)
  }

  override fun loadProfileEditDeletionDialog(internalProfileId: Int) {
    profileEditFragmentPresenter.handleLoadProfileDeletionDialog(internalProfileId)
  }

  /** Dagger injector for [ProfileEditFragment]. */
  interface Injector {
    /** Injects dependencies into the [fragment]. */
    fun inject(fragment: ProfileEditFragment)
  }
}
