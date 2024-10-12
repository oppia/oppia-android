package org.oppia.android.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ReadingTextSizeFragmentArguments
import org.oppia.android.app.model.ReadingTextSizeFragmentStateBundle
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

private const val FRAGMENT_ARGUMENTS_KEY = "ReadingTextSizeFragment.arguments"
private const val FRAGMENT_SAVED_STATE_KEY = "ReadingTextSizeFragment.saved_state"

/** The fragment to change the text size of the reading content in the app. */
class ReadingTextSizeFragment : InjectableFragment(), TextSizeRadioButtonListener {
  @Inject
  lateinit var readingTextSizeFragmentPresenter: ReadingTextSizeFragmentPresenter

  companion object {
    fun newInstance(readingTextSize: ReadingTextSize): ReadingTextSizeFragment {
      val protoArguments = ReadingTextSizeFragmentArguments.newBuilder().apply {
        this.readingTextSize = readingTextSize
      }.build()
      return ReadingTextSizeFragment().apply {
        arguments = Bundle().apply {
          putProto(FRAGMENT_ARGUMENTS_KEY, protoArguments)
        }
      }
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
    val readingTextSize =
      savedInstanceState?.retrieveStateBundle()?.selectedReadingTextSize
        ?: retrieveFragmentArguments().readingTextSize
    return readingTextSizeFragmentPresenter.handleOnCreateView(inflater, container, readingTextSize)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val stateBundle = ReadingTextSizeFragmentStateBundle.newBuilder().apply {
      selectedReadingTextSize = readingTextSizeFragmentPresenter.getTextSizeSelected()
    }.build()
    outState.putProto(FRAGMENT_SAVED_STATE_KEY, stateBundle)
  }

  override fun onTextSizeSelected(selectedTextSize: ReadingTextSize) {
    readingTextSizeFragmentPresenter.onTextSizeSelected(selectedTextSize)
  }

  /** Returns the [ReadingTextSizeFragmentArguments] stored in the fragment's arguments. */
  fun retrieveFragmentArguments(): ReadingTextSizeFragmentArguments {
    return checkNotNull(arguments) {
      "Expected arguments to be passed to ReadingTextSizeFragment"
    }.getProto(FRAGMENT_ARGUMENTS_KEY, ReadingTextSizeFragmentArguments.getDefaultInstance())
  }

  private fun Bundle.retrieveStateBundle(): ReadingTextSizeFragmentStateBundle {
    return getProto(
      FRAGMENT_SAVED_STATE_KEY, ReadingTextSizeFragmentStateBundle.getDefaultInstance()
    )
  }
}
