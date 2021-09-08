package org.oppia.android.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentComponentImpl

private const val READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE_ARGUMENT_KEY =
  "ReadingTextSizeFragment.reading_text_size_preference_summary_value"
private const val SELECTED_READING_TEXT_SIZE_SAVED_KEY =
  "ReadingTextSizeFragment.selected_text_size"

/** The fragment to change the text size of the reading content in the app. */
class ReadingTextSizeFragment : InjectableFragment(), TextSizeRadioButtonListener {
  @Inject
  lateinit var readingTextSizeFragmentPresenter: ReadingTextSizeFragmentPresenter

  companion object {
    fun newInstance(readingTextSize: String): ReadingTextSizeFragment {
      val args = Bundle()
      args.putString(READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE_ARGUMENT_KEY, readingTextSize)
      val fragment = ReadingTextSizeFragment()
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to ReadingTextSizeFragment" }
    val readingTextSize = if (savedInstanceState == null) {
      args.get(READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE_ARGUMENT_KEY) as String
    } else {
      savedInstanceState.get(SELECTED_READING_TEXT_SIZE_SAVED_KEY) as String
    }
    return readingTextSizeFragmentPresenter.handleOnCreateView(inflater, container, readingTextSize)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      SELECTED_READING_TEXT_SIZE_SAVED_KEY,
      readingTextSizeFragmentPresenter.getTextSizeSelected()
    )
  }

  override fun onTextSizeSelected(selectedTextSize: String) {
    readingTextSizeFragmentPresenter.onTextSizeSelected(selectedTextSize)
  }
}
