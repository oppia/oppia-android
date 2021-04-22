package org.oppia.android.app.mydownloads.downloads

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/** DialogFragment that ask user to input admin PIN in order to delete the downloaded topic. */
class DownloadsAccessDialogFragment : InjectableDialogFragment() {

  companion object {
    internal const val ADMIN_PIN_SAVED_KEY = "DownloadsFragment.admin_pin"
    internal const val ALLOW_DOWNLOAD_ACCESS_SAVED_KEY = "DownloadsFragment.allow_download_access"
    fun newInstance(
      adminPin: String,
      allowDownloadAccess: Boolean
    ): DownloadsAccessDialogFragment {
      val downloadsAccessDialogFragment = DownloadsAccessDialogFragment()
      val args = Bundle()
      args.putString(ADMIN_PIN_SAVED_KEY, adminPin)
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
    val allowDownloadAccess = arguments?.getBoolean(ALLOW_DOWNLOAD_ACCESS_SAVED_KEY) ?: false
    return downloadsAccessDialogFragmentPresenter.handleOnCreateDialog(
      adminPin,
      allowDownloadAccess
    )
  }
}
