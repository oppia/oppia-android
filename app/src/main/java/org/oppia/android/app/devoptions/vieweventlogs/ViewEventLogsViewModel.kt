package org.oppia.android.app.devoptions.vieweventlogs

import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.firebase.DebugAnalyticsEventLogger
import org.oppia.android.util.logging.firebase.DebugFirestoreEventLoggerImpl
import javax.inject.Inject

/**
 * [ViewModel] for [ViewEventLogsFragment]. It populates the recyclerview with a list of
 * [EventLogItemViewModel] which in turn display the event log.
 */
@FragmentScope
class ViewEventLogsViewModel @Inject constructor(
  debugAnalyticsEventLogger: DebugAnalyticsEventLogger,
  debugFirestoreEventLogger: DebugFirestoreEventLoggerImpl,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  // Retrieves events from cache that are meant to be uploaded to Firebase Firestore.
  private val firestoreEvents = debugFirestoreEventLogger.getEventList()

  // Retrieves events from cache that are meant to be uploaded to Firebase Analytics.
  private val analyticsEvents = debugAnalyticsEventLogger.getEventList()

  /**
   * List of [EventLogItemViewModel] used to populate recyclerview of [ViewEventLogsFragment]
   * to display event logs.
   */
  val eventLogsList: List<EventLogItemViewModel> by lazy {
    processEventLogsList()
  }

  private fun processEventLogsList(): List<EventLogItemViewModel> {
    return (analyticsEvents + firestoreEvents)
      .map {
        EventLogItemViewModel(it, machineLocale, resourceHandler)
      }
      .sortedByDescending { it.eventLog.timestamp }
  }
}
