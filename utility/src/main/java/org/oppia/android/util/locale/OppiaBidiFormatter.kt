package org.oppia.android.util.locale

import java.util.Locale
import org.oppia.android.util.locale.OppiaBidiFormatter.Factory

/**
 * A custom wrapper to Android's bidirectional formatter so that interactions with this class can be
 * tested.
 *
 * Instances of this class are created via its [Factory].
 *
 * Note that this class is only meant to be used by select packages, not broadly. Use [OppiaLocale]
 * for actual locale-safe string formatting.
 */
interface OppiaBidiFormatter {
  /** Wraps the provided text for bidirectional formatting & returns the result. */
  fun wrapText(unicode: CharSequence): CharSequence

  /**
   * Factory for creating new [OppiaBidiFormatter]s. This class can be injected at the application
   * component and below.
   */
  interface Factory {
    /** Returns a new [OppiaBidiFormatter] corresponding to the specified Android locale. */
    fun createFormatter(locale: Locale): OppiaBidiFormatter
  }
}
