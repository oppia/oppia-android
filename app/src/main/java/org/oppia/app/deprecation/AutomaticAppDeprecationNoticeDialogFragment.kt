package org.oppia.app.deprecation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.app.fragment.InjectableDialogFragment
import javax.inject.Inject

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
