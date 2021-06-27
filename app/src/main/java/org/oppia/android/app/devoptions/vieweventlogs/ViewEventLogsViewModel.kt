package org.oppia.android.app.devoptions.vieweventlogs

import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.logging.DebugEventLogger
import org.oppia.android.util.system.OppiaDateTimeFormatter
import javax.inject.Inject

/** The ViewModel for [ViewEventLogsFragment]. */
@FragmentScope
class ViewEventLogsViewModel @Inject constructor(
  debugEventLogger: DebugEventLogger,
  private val oppiaDateTimeFormatter: OppiaDateTimeFormatter
) : ObservableViewModel() {

  val eventList = debugEventLogger.getEventList()

  val eventLogsList: List<EventLogItemViewModel> by lazy {
    processEventLogsList()
  }

  private fun processEventLogsList(): List<EventLogItemViewModel> {
    val itemViewModelList: MutableList<EventLogItemViewModel> = ArrayList()
    eventList.forEach { eventLog ->
      itemViewModelList.add(EventLogItemViewModel(eventLog, oppiaDateTimeFormatter))
    }
    return itemViewModelList
  }
}
