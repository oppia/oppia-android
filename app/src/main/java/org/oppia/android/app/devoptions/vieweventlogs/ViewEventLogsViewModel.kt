package org.oppia.android.app.devoptions.vieweventlogs

import com.google.common.base.Optional
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
  debugAnalyticsEventLogger: Optional<DebugAnalyticsEventLogger>,
  debugFirestoreEventLogger: DebugFirestoreEventLoggerImpl,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  // Retrieves events from cache that are meant to be uploaded to Firebase Firestore.
  private val firestoreEvents = debugFirestoreEventLogger.getEventList()

  // Retrieves events from cache that are meant to be uploaded to Firebase Analytics.
  private val analyticsEvents = debugAnalyticsEventLogger.transform { it.getEventList() }

  /**
   * List of [EventLogItemViewModel] used to populate recyclerview of [ViewEventLogsFragment]
   * to display event logs.
   */
  val eventLogsList: List<EventLogItemViewModel> by lazy {
    processEventLogsList()
  }

  private fun processEventLogsList(): List<EventLogItemViewModel> {
    return (analyticsEvents.or(emptyList()) + firestoreEvents)
      .map {
        EventLogItemViewModel(it, machineLocale, resourceHandler)
      }
      .sortedByDescending { it.eventLog.timestamp }
  }
}
