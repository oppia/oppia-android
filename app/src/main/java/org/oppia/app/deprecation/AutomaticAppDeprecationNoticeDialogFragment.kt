package org.oppia.app.deprecation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/**
 * Dialog fragment to be shown when the pre-release version of the app should no longer be playable
 * due to it being expired.
 *
 * This notice protects the app from unexpected issues being permanently shipped to individuals, and
 * from the app being accessible to audience members for long-periods of time when it may not yet be
 * ready for broad use or distribution.
 */
class AutomaticAppDeprecationNoticeDialogFragment: InjectableDialogFragment() {
  companion object {
    /** Returns a new instance of [AutomaticAppDeprecationNoticeDialogFragment]. */
    fun newInstance(): AutomaticAppDeprecationNoticeDialogFragment {
      return AutomaticAppDeprecationNoticeDialogFragment()
    }
  }

  @Inject lateinit var automaticAppDeprecationNoticeDialogFragmentPresenter:
    AutomaticAppDeprecationNoticeDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return automaticAppDeprecationNoticeDialogFragmentPresenter.handleOnCreateDialog()
  }
}
