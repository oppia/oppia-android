package org.oppia.android.testing.robolectric

import androidx.core.text.BidiFormatter
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadow.api.Shadow
import org.robolectric.util.ReflectionHelpers
import java.util.Locale

/**
 * A custom Robolectric shadow for tracking interactions with [BidiFormatter].
 *
 * This is the main way to ensure that the wrapper actually calls through to Android's
 * bidirectional formatter. Note that the reason this isn't used broadly is for better
 * compatibility:
 * - Shadows are complicated to set up (versus using existing Dagger module mechanism with
 *   ubiquitous examples)
 * - Shadows don't work when running shared app layer tests with Espresso
 * - Custom shadows are discouraged, so this restricts use
 * - The wrapper can be replaced with a test double which can more easily be configured to rely on
 *   the real implementation & perform test-only tracking that's a bit more complicated with the
 *   shadow
 * - Shadow static state can leak between tests whereas singleton state in Dagger is properly
 *   recreated between test application instances
 */
@Implements(BidiFormatter::class)
class ShadowBidiFormatter {
  companion object {
    private val trackedFormatters = mutableMapOf<Locale, BidiFormatter>()

    /**
     * Returns new instances of [BidiFormatter] based on the specified [Locale]. This is called by
     * Robolectric as a replacement to [BidiFormatter.getInstance].
     *
     * Note that Android may reuse formatters internally, so there's no guarantee a new
     * implementation will be returned for subsequent calls (even if different [Locale]s are used).
     */
    @Implementation
    @JvmStatic
    fun getInstance(locale: Locale): BidiFormatter {
      return trackedFormatters.getOrPut(locale) {
        Shadow.directlyOn(
          BidiFormatter::class.java, "getInstance",
          ReflectionHelpers.ClassParameter.from(Locale::class.java, locale)
        )
      }
    }

    /**
     * Returns the [ShadowBidiFormatter] corresponding to the specified [Locale] (as created with
     * [getInstance]) or null if none exists.
     */
    fun lookUpFormatter(locale: Locale): ShadowBidiFormatter? =
      trackedFormatters[locale]?.let { shadowOf(it) }

    /**
     * Returns all [ShadowBidiFormatter]s created via [getInstance] since the last call to [reset].
     */
    fun lookUpFormatters(): Map<Locale, ShadowBidiFormatter> =
      trackedFormatters.mapValues { (_, formatter) -> shadowOf(formatter) }

    /**
     * Resets all tracked formatters up to now. This should always be called in a tear-down method
     * to avoid leaking state between tests.
     */
    fun reset() {
      // The tracked formatters are cleared to make it seem like new formatters are being created.
      // Similarly each individual shadow needs to be reset since Android only creates a couple of
      // Bidi formatters & Robolectric will keep a 1:1 relationship between classes and their
      // shadows.
      lookUpFormatters().values.forEach { it.wrappedSequences.clear() }
      trackedFormatters.clear()
    }

    private fun shadowOf(bidiFormatter: BidiFormatter): ShadowBidiFormatter =
      Shadow.extract(bidiFormatter) as ShadowBidiFormatter
  }

  @RealObject
  private lateinit var realObject: BidiFormatter

  private val wrappedSequences = mutableListOf<CharSequence>()

  /**
   * Robolectric shadow override of [BidiFormatter.unicodeWrap]. Note that only the [CharSequence]
   * version of unicode wrap is implemented here, so callers should make sure to only use that
   * overload.
   */
  @Suppress("unused") // Incorrect warning; Robolectric uses this via reflection.
  @Implementation
  fun unicodeWrap(str: CharSequence): CharSequence {
    wrappedSequences += str
    return Shadow.directlyOn(
      realObject, BidiFormatter::class.java, "unicodeWrap",
      ReflectionHelpers.ClassParameter.from(CharSequence::class.java, str)
    )
  }

  /** Returns the most recent wrapped sequence as per the call to [unicodeWrap]. */
  fun getLastWrappedSequence(): CharSequence? = wrappedSequences.lastOrNull()

  /** Returns all sequences passed to [unicodeWrap] for this formatter. */
  fun getAllWrappedSequences(): List<CharSequence> = wrappedSequences
}
