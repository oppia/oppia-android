package org.oppia.app.options

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ReadingTextSizeFragmentBinding
import org.oppia.app.model.ReadingTextSize
import javax.inject.Inject

/** The presenter for [ReadingTextSizeFragment]. */
class ReadingTextSizeFragmentPresenter @Inject constructor(private val fragment: Fragment) {
  private var fontSize: String = getReadingTextSize(ReadingTextSize.MEDIUM_TEXT_SIZE)

  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    readingTextSize: String
  ): View? {
    val binding = ReadingTextSizeFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    fontSize = readingTextSize
    updateTextSize(fontSize)

    // TODO(#1200): Stop the toolbar functionality in the multipane (add non-null receiver (?)).
    binding.readingTextSizeToolbar.setNavigationOnClickListener {
      val intent = Intent()
      intent.putExtra(KEY_MESSAGE_READING_TEXT_SIZE, fontSize)
      (fragment.activity as ReadingTextSizeActivity).setResult(REQUEST_CODE_TEXT_SIZE, intent)
      (fragment.activity as ReadingTextSizeActivity).finish()
    }

    when (readingTextSize) {
      getReadingTextSize(ReadingTextSize.SMALL_TEXT_SIZE) -> {
        binding.readingTextSizeSeekBar.progress = 0
        binding.previewTextview.textSize =
          getReadingTextSizeInFloat(ReadingTextSize.SMALL_TEXT_SIZE)
      }
      getReadingTextSize(ReadingTextSize.MEDIUM_TEXT_SIZE) -> {
        binding.readingTextSizeSeekBar.progress = 5
        binding.previewTextview.textSize =
          getReadingTextSizeInFloat(ReadingTextSize.MEDIUM_TEXT_SIZE)
      }
      getReadingTextSize(ReadingTextSize.LARGE_TEXT_SIZE) -> {
        binding.readingTextSizeSeekBar.progress = 10
        binding.previewTextview.textSize =
          getReadingTextSizeInFloat(ReadingTextSize.LARGE_TEXT_SIZE)
      }
      getReadingTextSize(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE) -> {
        binding.readingTextSizeSeekBar.progress = 15
        binding.previewTextview.textSize =
          getReadingTextSizeInFloat(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE)
      }
    }

    binding.readingTextSizeSeekBar.max = 15

    binding.readingTextSizeSeekBar.setOnSeekBarChangeListener(
      object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          var progressValue = progress
          progressValue /= 5
          progressValue *= 5

          when (progressValue) {
            0 -> {
              fontSize = getReadingTextSize(ReadingTextSize.SMALL_TEXT_SIZE)
              binding.previewTextview.textSize =
                getReadingTextSizeInFloat(ReadingTextSize.SMALL_TEXT_SIZE)
            }
            5 -> {
              fontSize = getReadingTextSize(ReadingTextSize.MEDIUM_TEXT_SIZE)
              binding.previewTextview.textSize =
                getReadingTextSizeInFloat(ReadingTextSize.MEDIUM_TEXT_SIZE)
            }
            10 -> {
              fontSize = getReadingTextSize(ReadingTextSize.LARGE_TEXT_SIZE)
              binding.previewTextview.textSize =
                getReadingTextSizeInFloat(ReadingTextSize.LARGE_TEXT_SIZE)
            }
            else -> {
              fontSize = getReadingTextSize(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE)
              binding.previewTextview.textSize =
                getReadingTextSizeInFloat(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE)
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
      is OptionsActivity -> parentActivity.optionActivityPresenter.updateReadingTextSize(textSize)
      is ReadingTextSizeActivity ->
        parentActivity.readingTextSizeActivityPresenter.setSelectedReadingTextSize(textSize)
    }
  }

  fun getReadingTextSizeInFloat(readingTextSize: ReadingTextSize): Float {
    return when (readingTextSize) {
      ReadingTextSize.SMALL_TEXT_SIZE -> 16f
      ReadingTextSize.MEDIUM_TEXT_SIZE -> 18f
      ReadingTextSize.LARGE_TEXT_SIZE -> 20f
      else -> 22f
    }
  }

  // TODO(#1584): Update this function to use multiplier instead of direct font size.
  fun getReadingTextSize(readingTextSize: ReadingTextSize): String {
    return when (readingTextSize) {
      ReadingTextSize.SMALL_TEXT_SIZE -> "Small"
      ReadingTextSize.MEDIUM_TEXT_SIZE -> "Medium"
      ReadingTextSize.LARGE_TEXT_SIZE -> "Large"
      else -> "Extra Large"
    }
  }
}
