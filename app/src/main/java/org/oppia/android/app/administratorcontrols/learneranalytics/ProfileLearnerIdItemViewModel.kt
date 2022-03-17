package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileListViewModel.ProfileListItemViewModel
import org.oppia.android.app.model.Profile
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.clipboard.ClipboardController
import org.oppia.android.domain.clipboard.ClipboardController.CurrentClip
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/**
 * [ProfileListItemViewModel] that represents the portion of the learner analytics admin page which
 * shows, and allows the user to copy, the learner ID associated with a specific profile.
 *
 * @property profile the [Profile] associated with this particular model instance
 */
class ProfileLearnerIdItemViewModel private constructor(
  val profile: Profile,
  private val clipboardController: ClipboardController,
  private val resourceHandler: AppLanguageResourceHandler
) : ProfileListItemViewModel(ProfileListViewModel.ProfileListItemViewType.LEARNER_ID) {
  /** The current ID copied to the user's clipboard, or ``null`` if there isn't one. */
  val currentCopiedId: LiveData<String?> by lazy {
    Transformations.map(clipboardController.getCurrentClip().toLiveData(), this::processCurrentClip)
  }

  /**
   * Copies the analytics learner ID associated with this model's [profile] to the user's clipboard.
   */
  fun copyLearnerId() {
    clipboardController.setCurrentClip(
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.learner_analytics_learner_id_clipboard_label_description, profile.name
      ),
      profile.learnerId
    )
  }

  private fun processCurrentClip(result: AsyncResult<CurrentClip>): String? {
    return if (result.isSuccess()) {
      when (val clip = result.getOrThrow()) {
        is CurrentClip.SetWithAppText -> clip.text
        CurrentClip.SetWithOtherContent, CurrentClip.Unknown -> null
      }
    } else null
  }

  /** Factory for creating new [ProfileLearnerIdItemViewModel]s. */
  class Factory @Inject constructor(
    private val clipboardController: ClipboardController,
    private val resourceHandler: AppLanguageResourceHandler
  ) {
    /** Returns a new [ProfileLearnerIdItemViewModel] corresponding to the specified [profile]. */
    fun create(profile: Profile): ProfileLearnerIdItemViewModel =
      ProfileLearnerIdItemViewModel(profile, clipboardController, resourceHandler)
  }
}
