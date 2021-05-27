package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

const val PROFILE_EDIT_PROFILE_ID_EXTRA_KEY = "ProfileEditFragment.profile_edit_profile_id"
const val IS_MULTIPANE_EXTRA_KEY = "ProfileEditFragment.is_multipane"

/** Activity that allows user to edit a profile. */
class ProfileEditFragment : InjectableFragment() {
  @Inject
  lateinit var profileEditFragmentPresenter: ProfileEditFragmentPresenter

  private var profileListInterface: ProfileListInterface? = null
  private var isDialogVisible = false

  companion object {
    fun newInstance(
      profileId: Int,
      isMultipane: Boolean = false
    ): ProfileEditFragment {
      val args = Bundle()
      args.putInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, profileId)
      args.putBoolean(IS_MULTIPANE_EXTRA_KEY, isMultipane)
      val fragment = ProfileEditFragment()
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
    if (context is ProfileListActivity)
      profileListInterface = context
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected variables to be passed to ProfileListFragment"
    }
    val isMultipane = args.getBoolean(IS_MULTIPANE_EXTRA_KEY)
    val internalProfileId = args.getInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, -1)
    savedInstanceState?.let {
      isDialogVisible = it.getBoolean("Dialog", false)
    }
    return profileEditFragmentPresenter.handleOnCreate(
      inflater,
      container,
      isMultipane,
      internalProfileId,
      profileListInterface,
      isDialogVisible
    )
  }

  /*override fun onSupportNavigateUp(): Boolean {
    val isMultipane = intent.extras!!.getBoolean(IS_MULTIPANE_EXTRA_KEY, false)
    if (isMultipane) {
      super.onBackPressed()
    } else {
      val intent = Intent(this, ProfileListActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    }
    return false
  }

  override fun onBackPressed() {
    val isMultipane = intent.extras!!.getBoolean(IS_MULTIPANE_EXTRA_KEY, false)
    if (isMultipane) {
      super.onBackPressed()
    } else {
      val intent = Intent(this, ProfileListActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    }
  }*/

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    isDialogVisible = profileEditFragmentPresenter.getIsDialogVisible()
    outState.putBoolean("Dialog", isDialogVisible)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    profileEditFragmentPresenter.dismissAlertDialog()
  }
}
