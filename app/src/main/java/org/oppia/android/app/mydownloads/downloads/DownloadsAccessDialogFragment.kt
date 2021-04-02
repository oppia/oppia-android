package org.oppia.android.app.mydownloads.downloads

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

const val ADMIN_PIN_SAVED_KEY = "DownloadsFragment.admin_pin"
const val INTERNAL_PROFILE_ID_SAVED_KEY = "DownloadsFragment.internal_profile_id"
const val ALLOW_DOWNLOAD_ACCESS_SAVED_KEY = "DownloadsFragment.allow_download_access"

class DownloadsAccessDialogFragment : InjectableDialogFragment() {

  companion object {
    fun newInstance(
      adminPin: String,
      internalProfileId: Int,
      allowDownloadAccess: Boolean
    ): DownloadsAccessDialogFragment {
      val downloadsAccessDialogFragment = DownloadsAccessDialogFragment()
      val args = Bundle()
      args.putString(ADMIN_PIN_SAVED_KEY, adminPin)
      args.putInt(INTERNAL_PROFILE_ID_SAVED_KEY, internalProfileId)
      args.putBoolean(ALLOW_DOWNLOAD_ACCESS_SAVED_KEY, allowDownloadAccess)
      downloadsAccessDialogFragment.arguments = args
      return downloadsAccessDialogFragment
    }
  }

  @Inject
  lateinit var downloadsAccessDialogFragmentPresenter: DownloadsAccessDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val adminPin = arguments?.getString(ADMIN_PIN_SAVED_KEY)
    checkNotNull(adminPin) { "Admin Pin must not be null" }
    val internalProfileId = arguments?.getInt(INTERNAL_PROFILE_ID_SAVED_KEY) ?: -1
    val allowDownloadAccess = arguments?.getBoolean(ALLOW_DOWNLOAD_ACCESS_SAVED_KEY) ?: false
    return downloadsAccessDialogFragmentPresenter.handleOnCreateDialog(
      adminPin,
      internalProfileId,
      allowDownloadAccess
    )
  }
}
