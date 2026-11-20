package org.oppia.android.app.devoptions.viewlogs

import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** Holds Logcat screen state independently of its Activity views. */
@FragmentScope
class ViewLogcatViewModel @Inject constructor() : ObservableViewModel() {
  val query = MutableLiveData("")
  val priority = MutableLiveData("V")
  val entries = MutableLiveData<List<LogcatItemViewModel>>(emptyList())
  val status = MutableLiveData("")

  private var allLines: List<String> = emptyList()

  fun setOutput(output: String, emptyMessage: String) {
    allLines = output.lines().filter { it.isNotBlank() }
    status.value = if (allLines.isEmpty()) emptyMessage else ""
    applySearch()
  }

  fun setQuery(value: String) {
    query.value = value
    applySearch()
  }

  private fun applySearch() {
    val search = query.value.orEmpty().trim()
    entries.value = allLines.asSequence()
      .filter { search.isEmpty() || it.contains(search, ignoreCase = true) }
      .map(::LogcatItemViewModel).toList()
  }
}
