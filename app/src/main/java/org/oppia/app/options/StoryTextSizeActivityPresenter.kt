package org.oppia.app.options

import android.content.Intent
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.StoryTextSizeActivityBinding
import org.oppia.app.model.StoryTextSize
import javax.inject.Inject

/** The presenter for [StoryTextSizeActivity]. */
@ActivityScope
class StoryTextSizeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private var fontSize: String = getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE)

  fun handleOnCreate(prefSummaryValue: String) {
    val binding =
      DataBindingUtil.setContentView<StoryTextSizeActivityBinding>(activity, R.layout.story_text_size_activity)

    fontSize = prefSummaryValue

    val toolbar = binding.storyTextSizeActivityToolbar as Toolbar
    activity.setSupportActionBar(toolbar)

    toolbar.setNavigationOnClickListener {
      val intent = Intent()
      intent.putExtra(KEY_MESSAGE_STORY_TEXT_SIZE, fontSize)
      (activity as StoryTextSizeActivity).setResult(REQUEST_CODE_TEXT_SIZE, intent)
      activity.finish()
    }

    when (prefSummaryValue) {
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
        binding.previewTextview.textSize = getStoryTextSizeInFloat(StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
      }
    }

    binding.storyTextSizeSeekBar.max = 15

    binding.storyTextSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        var progressValue = progress
        progressValue /= 5
        progressValue *= 5

        when (progressValue) {
          0 -> {
            fontSize = getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE)
            binding.previewTextview.textSize = getStoryTextSizeInFloat(StoryTextSize.SMALL_TEXT_SIZE)
          }
          5 -> {
            fontSize = getStoryTextSize(StoryTextSize.MEDIUM_TEXT_SIZE)
            binding.previewTextview.textSize = getStoryTextSizeInFloat(StoryTextSize.MEDIUM_TEXT_SIZE)
          }
          10 -> {
            fontSize = getStoryTextSize(StoryTextSize.LARGE_TEXT_SIZE)
            binding.previewTextview.textSize = getStoryTextSizeInFloat(StoryTextSize.LARGE_TEXT_SIZE)
          }
          else -> {
            fontSize = getStoryTextSize(StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
            binding.previewTextview.textSize = getStoryTextSizeInFloat(StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
          }
        }
        seekBar.progress = progressValue
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {
      }

      override fun onStopTrackingTouch(seekBar: SeekBar) {
      }
    })
  }

  fun geSelectedStoryTextSize(): String {
    return fontSize
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
