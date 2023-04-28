package org.oppia.android.app.administratorcontrols.learneranalytics

import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * [ObservableViewModel] that represents an element in the profile list shown by
 * [ProfileListViewModel].
 *
 * See this class's subclasses for the specific view models that may show content the list.
 *
 * @property viewType the [ProfileListItemViewType] corresponding to this model.
 */
abstract class ProfileListItemViewModel(
  val viewType: ProfileListItemViewType
) : ObservableViewModel() {

  /** Represents the different types of views that may be shown by [ProfileListViewModel]. */
  enum class ProfileListItemViewType {
    /** Corresponds to [DeviceIdItemViewModel]. */
    DEVICE_ID,

    /** Corresponds to [ProfileLearnerIdItemViewModel]. */
    LEARNER_ID,

    /** Corresponds to [SyncStatusItemViewModel]. */
    SYNC_STATUS,

    /** Corresponds to [ControlButtonsViewModel]. */
    SHARE_IDS
  }
}
