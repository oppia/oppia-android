package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment

/** Fragment that resets the profile pin of the user
 */
class ProfileResetPinFragment : InjectableFragment() {
  @Inject
  lateinit var profileResetPinFragmentPresenter: ProfileResetPinFragmentPresenter

  companion object {
    private const val PROFILE_RESET_PIN_PROFILE_ID_EXTRA_KEY =
      "ProfileResetPinActivity.profile_reset_pin_profile_id"
    private const val PROFILE_RESET_PIN_IS_ADMIN_EXTRA_KEY =
      "ProfileResetPinActivity.profile_reset_pin_is_admin"
  }

  /** Returns instance of [ProfileResetPinFragment]. */
  fun newInstance(
    profileResetPinProfileId: Int,
    profileResetPinIsAdmin: Int
  ): ProfileResetPinFragment {
    val fragment = ProfileResetPinFragment()
    val args = Bundle()
    args.putInt(PROFILE_RESET_PIN_PROFILE_ID_EXTRA_KEY, profileResetPinProfileId)
    args.putInt(PROFILE_RESET_PIN_IS_ADMIN_EXTRA_KEY, profileResetPinIsAdmin)
    fragment.arguments = args
    return fragment
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
      "Expected arguments to be passed to ProfileResetPinFragment"
    }
    val profileResetPinProfileId = args.getInt(PROFILE_RESET_PIN_PROFILE_ID_EXTRA_KEY)
    val profileResetPinIsAdmin = args.getInt(PROFILE_RESET_PIN_IS_ADMIN_EXTRA_KEY)
    return profileResetPinFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileResetPinProfileId,
      profileResetPinIsAdmin
    )
  }
}
}