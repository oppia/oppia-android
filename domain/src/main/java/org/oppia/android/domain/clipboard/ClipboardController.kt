package org.oppia.android.domain.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

private const val CURRENT_CLIP_PROVIDER_ID = "ClipboardController.current_clip"
private const val SET_CLIP_PROVIDER_ID = "ClipboardController.set_clip"

/**
 * Controller for checking the state of, and copying text to, the user's system clipboard so that
 * they can easily copy text into other apps.
 *
 * Note that this controller is designed specifically with privacy in mind: it does not allow
 * exposing the actual contents of the clipboard unless they originated from Oppia (even
 * accidentally). See [getCurrentClip] and [setCurrentClip] for specifics.
 */
@Singleton
class ClipboardController @Inject constructor(
  private val dataProviders: DataProviders,
  context: Context
) {
  // Note that this has to be initialized upon construction to ensure that it occurs on the main
  // thread (since older versions of Android assume that ClipboardManager is only ever created on
  // the main thread).
  private val clipboardManager =
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).also {
      it.addPrimaryClipChangedListener(this::maybeRecomputeCurrentClip)
    }
  private val state = MutableStateFlow<CurrentClip>(CurrentClip.Unknown)

  /**
   * Returns a [DataProvider] that represents the [CurrentClip] copied to the user's clipboard.
   *
   * The returned [DataProvider] will generally automatically update if the clipboard is modified
   * inside or outside the app, however there are a few caveats:
   * - It will not restore its state after a process death (instead defaulting per [CurrentClip]).
   * - It may not receive notice of clipboard changes from outside Oppia if Android decides that
   *   Oppia doesn't have focus (though Android seems to eventually notify the app of the change
   *   when it's foregrounded, so no additional effort is taken by the controller in these cases).
   *
   * The current clip can be changed via [setCurrentClip].
   *
   * Note that observing the clipboard state is inherently prone to data races since both the app
   * and system can change the clipboard simultaneously, so some effort is taken by the controller
   * to reduce data races. However, it cannot guarantee notification order, or eventual consistency
   * in cases where the clipboard is changed in quick succession by the app and then the system.
   * However, since it's expected for the clipboard to only ever be changed manually by the user,
   * this case ought to be rare and possibly due to another app misbehaving.
   */
  fun getCurrentClip(): DataProvider<CurrentClip> = dataProviders.run {
    state.convertToAutomaticDataProvider(CURRENT_CLIP_PROVIDER_ID)
  }

  /**
   * Copies the specified [text] with a specified human-readable [label] to the user's clipboard,
   * updating the [DataProvider] returned by [getCurrentClip] in the process.
   *
   * Note that this method returns a [DataProvider] that **must** be observed via the UI in order
   * for the text to actually copy. This is a mechanism to ensure that text can only ever be copied
   * from the app layer with the app in the foreground.
   */
  fun setCurrentClip(label: String, text: String): DataProvider<Any?> {
    val operationCompleted = AtomicBoolean()
    return dataProviders.createInMemoryDataProviderAsync(SET_CLIP_PROVIDER_ID) {
      // Only copy the text if it hasn't been copied yet.
      if (!operationCompleted.get()) {
        while (!operationCompleted.compareAndSet(/* expect= */ false, /* update= */ true)) {
          // Spin until the operation is reported as completed to avoid copying content multiple
          // times.
        }

        // This must use the setter since property syntax seems to break on SDK 30.
        @Suppress("UsePropertyAccessSyntax")
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
        state.emit(CurrentClip.SetWithAppText(label, text))
      }

      return@createInMemoryDataProviderAsync AsyncResult.Success(null)
    }
  }

  private fun maybeRecomputeCurrentClip() {
    // This is a loop to ensure that the atomic value is updated in cases when the state actually
    // changes, and tries to account for data races (such as when the clipboard changes mid-loop).
    do {
      val currentPrimaryClipText = clipboardManager.primaryClip?.extractText()
      val oldState = state.value
      val maybeNewState = when (oldState) {
        is CurrentClip.SetWithAppText -> {
          // Check if the current clip is actually what's been set from the Oppia app, or if it's
          // different (including null which might indicate a non-text clip, or a cleared
          // clipboard).
          if (currentPrimaryClipText == oldState.text) oldState else CurrentClip.SetWithOtherContent
        }
        // The clipboard state has potentially changed, but the app isn't interested in the change.
        // Note that this keeps 'Unknown' as 'Unknown' until Oppia sets a clipboard value.
        CurrentClip.SetWithOtherContent, CurrentClip.Unknown -> oldState
      }

      // If nothing has changed, don't bother updating the atomic.
      if (oldState == maybeNewState) return
    } while (!state.compareAndSet(oldState, maybeNewState))

    // No explicit notification is needed since changes to the value will automatically notify
    // observers.
  }

  /**
   * Represents all possible values which may be reported by [ClipboardController] as the current
   * state of the user's system clipboard.
   *
   * See the subclasses for specific states.
   */
  sealed class CurrentClip {
    /**
     * Indicates that the current clipboard contents aren't known.
     *
     * Note that this always indicates that the app has yet to copy anything to the clipboard, and
     * nothing can be assumed about the current contents of the user's clipboard (e.g. it might
     * contain actual content from the Oppia app from a previous instance).
     */
    object Unknown : CurrentClip()

    /**
     * Indicates that the current clipboard contents originated from the app.
     *
     * Note that per the caveats mentioned in [getCurrentClip] this may not represent the exact
     * current clipboard state, only what the app thinks is the current clipboard state.
     *
     * @property label the human-readable label of the clipboard contents
     * @property text the plaintext contents that is currently on the user's clipboard
     */
    data class SetWithAppText(val label: String, val text: String) : CurrentClip()

    /**
     * Indicates that the clipboard's contents are currently content defined by another app.
     *
     * This case implies that the user has copied text from the Oppia app at least once during the
     * lifetime of the current app instance since otherwise the reported clip state would be
     * [Unknown].
     */
    object SetWithOtherContent : CurrentClip()
  }

  private companion object {
    private fun ClipData.extractText() = if (itemCount > 0) getItemAt(0).text else null
  }
}
