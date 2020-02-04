package org.oppia.app.options

import android.content.Intent
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.StoryTextSizeActivityBinding
import javax.inject.Inject

/** The presenter for [StoryTextSizeActivity]. */
@ActivityScope
class StoryTextSizeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private var fontSize: String = STORY_TEXT_SIZE_SMALL

  fun handleOnCreate(prefSummaryValue: String) {
    val binding =
      DataBindingUtil.setContentView<StoryTextSizeActivityBinding>(activity, R.layout.story_text_size_activity)

    fontSize = prefSummaryValue

    binding.storyTextSizeToolbar.setNavigationOnClickListener {
      val intent = Intent()
      intent.putExtra(KEY_MESSAGE_STORY_TEXT_SIZE, fontSize)
      (activity as StoryTextSizeActivity).setResult(1, intent)
      activity.finish()
    }

    when (prefSummaryValue) {
      STORY_TEXT_SIZE_SMALL -> {
        binding.storyTextSizeSeekBar.progress = 0
        binding.previewTextview.textSize = SMALL_TEXT_SIZE_VALUE
      }
      STORY_TEXT_SIZE_MEDIUM -> {
        binding.storyTextSizeSeekBar.progress = 5
        binding.previewTextview.textSize = MEDIUM_TEXT_SIZE_VALUE
      }
      STORY_TEXT_SIZE_LARGE -> {
        binding.storyTextSizeSeekBar.progress = 10
        binding.previewTextview.textSize = LARGE_TEXT_SIZE_VALUE
      }
      STORY_TEXT_SIZE_EXTRA_LARGE -> {
        binding.storyTextSizeSeekBar.progress = 15
        binding.previewTextview.textSize = EXTRA_LARGE_TEXT_SIZE_VALUE
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
            fontSize = STORY_TEXT_SIZE_SMALL
            binding.previewTextview.textSize = SMALL_TEXT_SIZE_VALUE
          }
          5 -> {
            fontSize = STORY_TEXT_SIZE_MEDIUM
            binding.previewTextview.textSize = MEDIUM_TEXT_SIZE_VALUE
          }
          10 -> {
            fontSize = STORY_TEXT_SIZE_LARGE
            binding.previewTextview.textSize = LARGE_TEXT_SIZE_VALUE
          }
          else -> {
            fontSize = STORY_TEXT_SIZE_EXTRA_LARGE
            binding.previewTextview.textSize = EXTRA_LARGE_TEXT_SIZE_VALUE
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
}
