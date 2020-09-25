package org.oppia.android.app.fragment

import android.content.Context
import androidx.fragment.app.DialogFragment
import org.oppia.android.app.activity.InjectableAppCompatActivity

/**
 * A fragment that facilitates field injection to children. This fragment can only be used with
 * [InjectableAppCompatActivity] contexts.
 */
abstract class InjectableDialogFragment : DialogFragment() {
  /**
   * The [FragmentComponent] corresponding to this fragment. This cannot be used before [onAttach] is called, and can be
   * used to inject lateinit fields in child fragments during fragment attachment (which is recommended to be done in an
   * override of [onAttach]).
   */
  lateinit var fragmentComponent: FragmentComponent

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent =
      (requireActivity() as InjectableAppCompatActivity).createFragmentComponent(this)
  }
}
