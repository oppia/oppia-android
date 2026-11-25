package org.oppia.android.app.devoptions.viewlogs

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [ViewLogcatViewModel]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ViewLogcatViewModelTest {
  @Test
  fun setOutput_withLines_displaysEveryNonBlankLine() {
    val viewModel = ViewLogcatViewModel()

    viewModel.setOutput("first line\n\nsecond line", "No logs")

    assertThat(viewModel.entries.value?.map { it.line }).containsExactly(
      "first line", "second line"
    ).inOrder()
    assertThat(viewModel.status.value).isEmpty()
  }

  @Test
  fun setQuery_filtersLinesCaseInsensitively() {
    val viewModel = ViewLogcatViewModel()
    viewModel.setOutput("Info: startup\nError: disk", "No logs")

    viewModel.setQuery("ERROR")

    assertThat(viewModel.entries.value?.map { it.line }).containsExactly("Error: disk")
  }

  @Test
  fun setOutput_withOnlyBlankLines_setsEmptyStatus() {
    val viewModel = ViewLogcatViewModel()

    viewModel.setOutput(" \n\n", "No logs available")

    assertThat(viewModel.entries.value).isEmpty()
    assertThat(viewModel.status.value).isEqualTo("No logs available")
  }
}
