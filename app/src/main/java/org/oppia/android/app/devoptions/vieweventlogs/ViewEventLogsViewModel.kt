package org.oppia.android.app.devoptions.vieweventlogs

import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.logging.firebase.DebugEventLogger
import org.oppia.android.util.system.OppiaDateTimeFormatter
import javax.inject.Inject

/**
 * [ViewModel] for [ViewEventLogsFragment]. It populates the recyclerview with a list of
 * [EventLogItemViewModel] which in turn display the event log.
 */
@FragmentScope
class ViewEventLogsViewModel @Inject constructor(
  debugEventLogger: DebugEventLogger,
  private val oppiaDateTimeFormatter: OppiaDateTimeFormatter
) : ObservableViewModel() {

  private val eventList = debugEventLogger.getEventList()

  /**
   * List of [EventLogItemViewModel] used to populate recyclerview of [ViewEventLogsFragment]
   * to display event logs.
   */
  val eventLogsList: List<EventLogItemViewModel> by lazy {
    processEventLogsList()
  }

  private fun processEventLogsList(): List<EventLogItemViewModel> {
    return eventList.map {
      EventLogItemViewModel(it, oppiaDateTimeFormatter)
    }.reversed()
  }
}
