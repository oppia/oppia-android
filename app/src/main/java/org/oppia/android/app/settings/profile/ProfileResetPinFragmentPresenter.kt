package org.oppia.android.app.settings.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatViewInflater
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler

class ProfileResetPinFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var binding:ProfileResetPinFragment

  /** Handles onCreateView() method of the [ProfileResetPinFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container:ViewGroup?,
    profileResetPinProfileId: Int,

  )
}