package org.oppia.android.domain.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders

private const val CURRENT_CLIP_PROVIDER_ID = "ClipboardController.current_clip"

@Singleton
class ClipboardController @Inject constructor(
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  context: Context
) {
  private val clipboardManager by lazy {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).also {
      it.addPrimaryClipChangedListener(this::maybeRecomputeCurrentClip)
    }
  }
  private var state = AtomicReference<CurrentClip>(CurrentClip.Unknown)

  fun getCurrentClip(): DataProvider<CurrentClip> =
    dataProviders.createInMemoryDataProvider(CURRENT_CLIP_PROVIDER_ID) { state.get() }

  fun setCurrentClip(label: String, text: String) {
    // This must use the setter since property syntax seems to break on SDK 30.
    @Suppress("UsePropertyAccessSyntax")
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
    state.set(CurrentClip.SetWithAppText(label, text))
    asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_CLIP_PROVIDER_ID)
  }

  private fun maybeRecomputeCurrentClip() {
    // This is a loop to ensure that the atomic value is updated in cases when the state actually
    // changes, and tries to account for data races (such as when the clipboard changes mid-loop).
    do {
      val currentPrimaryClipText = clipboardManager.primaryClip?.extractText()
      val oldState = state.get()
      val maybeNewState = when (oldState) {
        is CurrentClip.SetWithAppText -> {
          // Check if the current clip is actually what's been set from the Oppia app, or if it's
          // different (including null which might indicate a non-text clip, or a cleared clipboard).
          if (currentPrimaryClipText == oldState.text) oldState else CurrentClip.SetWithOtherContent
        }
        // The clipboard state has potentially changed, but the app isn't interested in the change.
        // Note that this keeps 'Unknown' as 'Unknown' until Oppia sets a clipboard value.
        CurrentClip.SetWithOtherContent, CurrentClip.Unknown -> oldState
      }

      // If nothing has changed, don't bother updating the atomic.
      if (oldState == maybeNewState) return
    } while (!state.compareAndSet(oldState, maybeNewState))

    // Since the method hasn't ended, the atomic reference has changed and the data provider should
    // be notified.
    asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_CLIP_PROVIDER_ID)
  }

  sealed class CurrentClip {
    object Unknown: CurrentClip()

    // TODO: mention that this might not represent the *current* clipboard state, only what the app is aware of.
    data class SetWithAppText(val label: String, val text: String): CurrentClip()

    // TODO: mention will never revert back to SetWithAppText
    object SetWithOtherContent: CurrentClip()
  }

  private companion object {
    private fun ClipData.extractText(): CharSequence? {
      return if (itemCount > 0) getItemAt(0).text else null
    }
  }
}
