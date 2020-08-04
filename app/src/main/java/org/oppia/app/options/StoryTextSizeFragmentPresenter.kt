package org.oppia.app.options

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

  private var fontSize: String = getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE)

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
    (fragment.activity as OptionsActivity).optionActivityPresenter.updateStoryTextSize(textSize)
  }

  fun getStoryTextSizeInFloat(storyTextSize: StoryTextSize): Float {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> 16f
      StoryTextSize.MEDIUM_TEXT_SIZE -> 18f
      StoryTextSize.LARGE_TEXT_SIZE -> 20f
      else -> 22f
    }
  }

  fun getStoryTextSize(storyTextSize: StoryTextSize): String {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> "Small"
      StoryTextSize.MEDIUM_TEXT_SIZE -> "Medium"
      StoryTextSize.LARGE_TEXT_SIZE -> "Large"
      else -> "Extra Large"
    }
  }
}
