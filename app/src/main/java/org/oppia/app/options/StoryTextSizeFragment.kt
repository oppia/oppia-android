package org.oppia.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE =
  "STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE"

/** The fragment to change the text size of the story content in the app. */
class StoryTextSizeFragment : InjectableFragment() {
  @Inject
  lateinit var storyTextSizeFragmentPresenter: StoryTextSizeFragmentPresenter

  companion object {
    fun newInstance(storyTextSize: String): StoryTextSizeFragment {
      val args = Bundle()
      args.putString(KEY_STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE, storyTextSize)
      val fragment = StoryTextSizeFragment()
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to StoryTextSizeFragment" }
    val storyTextSize = args.get(KEY_STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE) as String
    return storyTextSizeFragmentPresenter.handleOnCreateView(inflater, container, storyTextSize)
  }
}
