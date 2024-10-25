package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ReadingTextSizeActivityParams
import org.oppia.android.app.model.ReadingTextSizeActivityResultBundle
import org.oppia.android.app.model.ReadingTextSizeActivityStateBundle
import org.oppia.android.app.model.ScreenName.READING_TEXT_SIZE_ACTIVITY
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

private const val ACTIVITY_PARAMS_KEY = "ReadingTextSizeActivity.params"
private const val ACTIVITY_SAVED_STATE_KEY = "ReadingTextSizeActivity.saved_state"

/** The activity to change the text size of the reading content in the app. */
class ReadingTextSizeActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var readingTextSizeActivityPresenter: ReadingTextSizeActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val readingTextSize =
      savedInstanceState?.retrieveStateBundle()?.selectedReadingTextSize
        ?: retrieveActivityParams().readingTextSize
    readingTextSizeActivityPresenter.handleOnCreate(readingTextSize)

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(/* enabled = */ true) {
        override fun handleOnBackPressed() {
          val resultBundle = ReadingTextSizeActivityResultBundle.newBuilder().apply {
            selectedReadingTextSize = readingTextSizeActivityPresenter.getSelectedReadingTextSize()
          }.build()
          val intent = Intent().apply {
            putProtoExtra(MESSAGE_READING_TEXT_SIZE_RESULTS_KEY, resultBundle)
          }
          setResult(RESULT_OK, intent)
          finish()
        }
      }
    )
  }

  companion object {
    /** Returns a new [Intent] to route to [ReadingTextSizeActivity]. */
    fun createReadingTextSizeActivityIntent(
      context: Context,
      initialReadingTextSize: ReadingTextSize
    ): Intent {
      val params = ReadingTextSizeActivityParams.newBuilder().apply {
        readingTextSize = initialReadingTextSize
      }.build()
      return Intent(context, ReadingTextSizeActivity::class.java).apply {
        putProtoExtra(ACTIVITY_PARAMS_KEY, params)
        decorateWithScreenName(READING_TEXT_SIZE_ACTIVITY)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val stateBundle = ReadingTextSizeActivityStateBundle.newBuilder().apply {
      selectedReadingTextSize = readingTextSizeActivityPresenter.getSelectedReadingTextSize()
    }.build()
    outState.putProto(ACTIVITY_SAVED_STATE_KEY, stateBundle)
  }

  private fun retrieveActivityParams() =
    intent.getProtoExtra(ACTIVITY_PARAMS_KEY, ReadingTextSizeActivityParams.getDefaultInstance())

  private fun Bundle.retrieveStateBundle(): ReadingTextSizeActivityStateBundle {
    return getProto(
      ACTIVITY_SAVED_STATE_KEY, ReadingTextSizeActivityStateBundle.getDefaultInstance()
    )
  }
}
