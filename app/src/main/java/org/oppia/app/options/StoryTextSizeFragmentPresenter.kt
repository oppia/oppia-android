package org.oppia.app.options

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.StoryTextSizeFragmentBinding
import org.oppia.app.model.StoryTextSize
import javax.inject.Inject

/** The presenter for [StoryTextSizeFragment]. */
class StoryTextSizeFragmentPresenter @Inject constructor(private val fragment: Fragment) {
  private var fontSize: String = getStoryTextSize(StoryTextSize.MEDIUM_TEXT_SIZE)

  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    storyTextSize: String
  ): View? {
    val binding = StoryTextSizeFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    fontSize = storyTextSize
    updateTextSize(fontSize)

    // TODO(#1200): Stop the toolbar functionality in the multipane (add non-null receiver (?)).
    binding.storyTextSizeToolbar.setNavigationOnClickListener {
      val intent = Intent()
      intent.putExtra(KEY_MESSAGE_STORY_TEXT_SIZE, fontSize)
      (fragment.activity as StoryTextSizeActivity).setResult(REQUEST_CODE_TEXT_SIZE, intent)
      (fragment.activity as StoryTextSizeActivity).finish()
    }

    when (storyTextSize) {
      getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE) -> {
        binding.storyTextSizeSeekBar.progress = 0
        binding.previewTextview.textSize = getStoryTextSizeInFloat(StoryTextSize.SMALL_TEXT_SIZE)
      }
      getStoryTextSize(StoryTextSize.MEDIUM_TEXT_SIZE) -> {
        binding.storyTextSizeSeekBar.progress = 5
        binding.previewTextview.textSize = getStoryTextSizeInFloat(StoryTextSize.MEDIUM_TEXT_SIZE)
      }
      getStoryTextSize(StoryTextSize.LARGE_TEXT_SIZE) -> {
        binding.storyTextSizeSeekBar.progress = 10
        binding.previewTextview.textSize = getStoryTextSizeInFloat(StoryTextSize.LARGE_TEXT_SIZE)
      }
      getStoryTextSize(StoryTextSize.EXTRA_LARGE_TEXT_SIZE) -> {
        binding.storyTextSizeSeekBar.progress = 15
        binding.previewTextview.textSize =
          getStoryTextSizeInFloat(StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
      }
    }

    binding.storyTextSizeSeekBar.max = 15

    binding.storyTextSizeSeekBar.setOnSeekBarChangeListener(
      object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          var progressValue = progress
          progressValue /= 5
          progressValue *= 5

          when (progressValue) {
            0 -> {
              fontSize = getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE)
              binding.previewTextview.textSize =
                getStoryTextSizeInFloat(StoryTextSize.SMALL_TEXT_SIZE)
            }
            5 -> {
              fontSize = getStoryTextSize(StoryTextSize.MEDIUM_TEXT_SIZE)
              binding.previewTextview.textSize =
                getStoryTextSizeInFloat(StoryTextSize.MEDIUM_TEXT_SIZE)
            }
            10 -> {
              fontSize = getStoryTextSize(StoryTextSize.LARGE_TEXT_SIZE)
              binding.previewTextview.textSize =
                getStoryTextSizeInFloat(StoryTextSize.LARGE_TEXT_SIZE)
            }
            else -> {
              fontSize = getStoryTextSize(StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
              binding.previewTextview.textSize =
                getStoryTextSizeInFloat(StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
            }
          }
          seekBar.progress = progressValue
          updateTextSize(fontSize)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
      })

    return binding.root
  }

  fun updateTextSize(textSize: String) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity -> parentActivity.optionActivityPresenter.updateStoryTextSize(textSize)
      is StoryTextSizeActivity ->
        parentActivity.storyTextSizeActivityPresenter.setSelectedStoryTextSize(textSize)
    }
  }

  fun getStoryTextSizeInFloat(storyTextSize: StoryTextSize): Float {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> 16f
      StoryTextSize.MEDIUM_TEXT_SIZE -> 18f
      StoryTextSize.LARGE_TEXT_SIZE -> 20f
      else -> 22f
    }
  }

  // TODO(#1584): Update this function to use multiplier instead of direct font size.
  fun getStoryTextSize(storyTextSize: StoryTextSize): String {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> "Small"
      StoryTextSize.MEDIUM_TEXT_SIZE -> "Medium"
      StoryTextSize.LARGE_TEXT_SIZE -> "Large"
      else -> "Extra Large"
    }
  }
}
