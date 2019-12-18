package org.oppia.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.StoryTextSizeActivityBinding
import javax.inject.Inject
import android.content.Intent
import android.widget.SeekBar

/** The presenter for [StoryTextSizeActivity]. */
@ActivityScope
class StoryTextSizeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private var fontSize: String = "Small"

  fun handleOnCreate(prefSummaryValue: String) {
    val binding =
      DataBindingUtil.setContentView<StoryTextSizeActivityBinding>(activity, R.layout.story_text_size_activity)

    fontSize = prefSummaryValue

    binding.storyTextSizeToolbar.setNavigationOnClickListener {
      val intent = Intent()
      intent.putExtra("MESSAGE", fontSize)
      (activity as StoryTextSizeActivity).setResult(1, intent)
      activity.finish()
    }

    when (prefSummaryValue) {
      "Small" -> {
        binding.storyTextSizeSeekBar.progress = 0
        binding.previewTextview.textSize = 16f
      }
      "Medium" -> {
        binding.storyTextSizeSeekBar.progress = 5
        binding.previewTextview.textSize = 18f
      }
      "Large" -> {
        binding.storyTextSizeSeekBar.progress = 10
        binding.previewTextview.textSize = 20f
      }
      "Extra Large" -> {
        binding.storyTextSizeSeekBar.progress = 15
        binding.previewTextview.textSize = 22f
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
            fontSize = "Small"
            binding.previewTextview.textSize = 16f
          }
          5 -> {
            fontSize = "Medium"
            binding.previewTextview.textSize = 18f
          }
          10 -> {
            fontSize = "Large"
            binding.previewTextview.textSize = 20f
          }
          else -> {
            fontSize = "Extra Large"
            binding.previewTextview.textSize = 22f
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
