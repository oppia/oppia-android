package org.oppia.app.option

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
  var fontSize: String = "Small"

  fun handleOnCreate(prefSummaryValue: String) {
    val binding =
      DataBindingUtil.setContentView<StoryTextSizeActivityBinding>(activity, R.layout.story_text_size_activity)

    fontSize = prefSummaryValue

    binding.toolbar.setNavigationOnClickListener {
      val intent = Intent()
      intent.putExtra("MESSAGE", fontSize)
      activity.setResult(1, intent)
      activity.finish()//finishing activity
    }
    when {
      prefSummaryValue.equals("Small") -> binding.seekBar.progress = 0
      prefSummaryValue.equals("Medium") -> binding.seekBar.progress = 5
      prefSummaryValue.equals("Large") -> binding.seekBar.progress = 10
      prefSummaryValue.equals("Extra Large") -> binding.seekBar.progress = 15
    }

    binding.seekBar.max = 15

    binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        var progress = progress
        progress = progress / 5
        progress = progress * 5
        when (progress) {
          0 -> fontSize = "Small"
          5 -> fontSize = "Medium"
          10 -> fontSize = "Large"
          else -> fontSize = "Extra Large"
        }
        seekBar.progress = progress
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {
      }

      override fun onStopTrackingTouch(seekBar: SeekBar) {
      }
    })
  }
}
