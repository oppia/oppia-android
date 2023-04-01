package org.oppia.android.util.locale

import android.content.res.Resources
import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion

/**
 * Represents a locale in the app. This is similar to Android's locale in that all locale-based
 * operations should go through this class.
 *
 * However, it's different in that:
 * 1. Some locales must be retrieved asynchronously through domain controllers since the validity of
 *    the locale is verified against the app's own language & region configurations
 * 2. Because of (1), instances of this class are guaranteed to be supported by the app
 * 3. Different subclasses of the locale exist to serve different purposes (such as processing
 *    strings, or computing user-displayable strings). This is conceptually similar Android's locale
 *    categories introduced in API 24.
 * 4. All code in the app must use this class for locale-based operations (as backed by enforced
 *    regex CI checks) to ensure locale correctness in different contexts
 *
 * See [org.oppia.android.domain.locale.LocaleController] for retrieving instances of this class,
 * and at its subclasses below for the different locale APIs that are available.
 *
 * Finally, all implementations of this class will be hashable & have equals implementations such
 * different instances of this class will be equivalent so long as they are _functionally_ the same
 * locale (which generally means that they are of the same class type and have the same
 * [localeContext]). Further, subclasses will have custom [toString] implementations for improved
 * logging messaging.
 */
sealed class OppiaLocale {
  /** The deterministic context that represents this exact locale instance. */
  abstract val localeContext: OppiaLocaleContext

  /**
   * Returns the [OppiaLanguage] corresponding to this locale. Locale instances can only correspond
   * to a single language.
   */
  fun getCurrentLanguage(): OppiaLanguage = localeContext.languageDefinition.language

  /**
   * Returns the primary [LanguageId] corresponding to this locale (to be used when making
   * interoperable decisions about matching a language-specific string or resource).
   */
  fun getLanguageId(): LanguageId = localeContext.getLanguageId()

  /**
   * Returns the fallback [LanguageId] corresponding to this locale which may be used as a
   * replacement to [getLanguageId] in contexts where the [LanguageId] returned by that method is
   * incompatible or unavailable.
   */
  fun getFallbackLanguageId(): LanguageId = localeContext.getFallbackLanguageId()

  /**
   * Returns the [OppiaRegion] corresponding to this locale. Note that this region may not always
   * represent where the user actually lives. Instead, it's a best approximation for the user's
   * region based on the system-reported locale & the list of officially supported regions by the
   * app. Further, the user may always change their region based on system settings (which means
   * it's entirely up to the user's discretion to indicate that the locale-reported region actually
   * corresponds to their geographical location).
   *
   * There are other methods of representing or computing regions that may be more generic and
   * accurate (such as using region codes for the former and telephony services for the latter), but
   * the trade-off here is predictability and simpler implementation.
   */
  fun getCurrentRegion(): OppiaRegion = localeContext.regionDefinition.region

  abstract override fun equals(other: Any?): Boolean

  abstract override fun hashCode(): Int

  abstract override fun toString(): String

  /**
   * An [OppiaLocale] that is used only for machine-readable strings and should *never* be used for
   * human-readable strings (with the exception of logging statements since those are only ever read
   * by developers). This locale exists solely for string processing. All user-displayable strings
   * should be processed using [DisplayLocale].
   *
   * Implementations of this class guarantee consistency in string formatting across instances of
   * the app (including app upgrades), and regardless of whatever locale the user has set up on
   * their system or the particular version of Android or Android device that they're using.
   *
   * Unless otherwise noted, all methods of this class will not perform any bidirectional wrapping
   * or locale-aware case changing/checking.
   *
   * Implementations of this class are available for injection starting that the application
   * component. All code that depends on this locale can also leverage the fake Oppia clock to
   * influence test-only behavior for time & date specific operations.
   *
   * Finally, some of the methods of this class are extension methods which means instances of the
   * locale should be run in a contextual way to ensure the function receivers are set up correctly,
   * e.g.:
   *
   * ```kt
   * val stringsAreEqual = machineLocale.run {
   *   string.equalsIgnoreCase(other)
   * }
   * ```
   */
  abstract class MachineLocale(override val localeContext: OppiaLocaleContext) : OppiaLocale() {
    /** Returns a formatted version of this string by interpolating the specified arguments. */
    abstract fun String.formatForMachines(vararg args: Any?): String

    /** Returns the lowercase version of this string. */
    abstract fun String.toMachineLowerCase(): String

    /** Returns the uppercase version of this string. */
    abstract fun String.toMachineUpperCase(): String

    /** Returns the capitalized version of this string. */
    abstract fun String.capitalizeForMachines(): String

    /** Returns the decapitalized version of this string. */
    abstract fun String.decapitalizeForMachines(): String

    /** Returns whether this string ends with the specified suffix, ignoring case differences. */
    abstract fun String.endsWithIgnoreCase(suffix: String): Boolean

    /**
     * Returns whether this string is the same as the specified string, ignoring case differences.
     */
    abstract fun String?.equalsIgnoreCase(other: String?): Boolean

    /**
     * Returns the current [TimeOfDay].
     *
     * Note that the returned date object is always corresponding to the local timezone of the
     * device which may not relate to the user's defined locale.
     */
    abstract fun getCurrentTimeOfDay(): TimeOfDay

    /**
     * Returns an [OppiaDate] object representing the specified date string, or null if the string
     * is incorrectly formatted. This has the same timezone caveat as [getCurrentTimeOfDay].
     *
     * Dates are expected in the format: YYYY-MM-DD.
     */
    abstract fun parseOppiaDate(dateString: String): OppiaDate?

    /**
     * Returns a time string corresponding to the current wall clock time. Note that, as with other
     * methods in this class, this should never be used for UI-displayed strings. It's intended for
     * developer-facing strings, instead, such as log statements.
     *
     * The returned string is guaranteed to include at least the hour, minute, and second pertaining
     * to the current wall time (though no guarantees are made to other time information being
     * included, or in how this information is presented).
     */
    abstract fun computeCurrentTimeString(): String

    /** Represents different times of day. */
    enum class TimeOfDay {
      /** Corresponds to the user's morning time. */
      MORNING,

      /** Corresponds to the user's afternoon time. */
      AFTERNOON,

      /** Corresponds to the user's evening time. */
      EVENING,

      /**
       * Corresponds to an unknown time of day (implying that something might have gone wrong during
       * the time-of-day computation).
       */
      UNKNOWN
    }

    /** An abstract representation of a date. */
    interface OppiaDate {
      /**
       * Returns whether this date occurred before today.
       *
       * Note that the internal implementation is not required to guarantee day alignment, so it
       * must be assigned that this check relates to the time at which the _object itself is
       * created_ rather than the day on which it was created.
       */
      fun isBeforeToday(): Boolean
    }
  }

  /**
   * An [OppiaLocale] which can be used to compute user-displayable strings. Generally, this only
   * applies to app strings (other content-based localization decisions are handled by
   * [ContentLocale]).
   *
   * Instances of this class must be retrieved through domain layer controllers (generally via data
   * providers) since the locale object to use may change if the user decides to change which
   * language that they want to use within the app. Because of this, instances of this class are not
   * available outside of the app layer.
   *
   * This class should only be used for strings that will eventually be shown to the user, or to
   * make decisions regarding the layout of views shown to the user. Other strings should be
   * processed using [MachineLocale] to ensure consistency across app instances.
   *
   * Note that some of the methods of this class use string receivers. See the documentation for
   * [MachineLocale] for a code sample on how to wrap this locale in a receiver context to call
   * those methods.
   *
   * Finally, note that resource-based methods do not guarantee that the strings returned are tied
   * to this locale. Only the active locale can affect the strings returned (see
   * [org.oppia.android.domain.locale.LocaleController.setAsDefault]).
   */
  abstract class DisplayLocale(override val localeContext: OppiaLocaleContext) : OppiaLocale() {
    /**
     * Returns a locally formatted representation of the long integer [value].
     *
     * No assumptions can be made regarding the formatting of the returned string except that:
     * 1. The exact value will be represented (no rounding or truncation will occur).
     * 2. The resulting value should be generally readable by screenreaders if they support the the
     *   current locale.
     */
    abstract fun formatLong(value: Long): String

    /**
     * Returns a locally formatted representation of the double [value].
     *
     * No assumptions can be made regarding the formatting of the returned string except that it
     * should generally be readable by screenreaders if they support the current locale. This
     * function may round and/or truncate the double for formatting simplicity.
     */
    abstract fun formatDouble(value: Double): String

    /** Returns a human-readable representation of [number]. */
    abstract fun toHumanReadableString(number: Int): String

    /**
     * Returns a locally formatted date string representing the specified Unix timestamp.
     *
     * No assumptions can be made regarding the formatting of the returned string. Further, the
     * implementation aims to return a string that includes a medium amount of information
     * corresponding to the time (i.e. 'January 1, 2019').
     */
    abstract fun computeDateString(timestampMillis: Long): String

    /**
     * Returns a locally formatted time string representing the specified Unix timestamp.
     *
     * Similar to [computeDateString], no assumptions can be made about the format returned.
     * Implementations should return enough information to convey hours and minutes (i.e. 4:45pm or
     * 16:45).
     */
    abstract fun computeTimeString(timestampMillis: Long): String

    /**
     * Returns a locally formatted date/time string representing the specified Unix timestamp.
     *
     * Similar to [computeDateString], no assumptions can be made about the format returned. The
     * information included corresponds to the documented behaviors of [computeDateString] and
     * [computeTimeString].
     */
    abstract fun computeDateTimeString(timestampMillis: Long): String

    /**
     * Returns the [androidx.core.view.ViewCompat] layout direction that should be used in layouts
     * that display app strings localized by this locale.
     */
    abstract fun getLayoutDirection(): Int

    /**
     * Returns a formatted version of this string by interpolating the specified arguments, taking
     * according to this locale.
     *
     * This method attempts to ensure bidirectional consistency by wrapping certain arguments with
     * bidirectional markers so that they appear in the correct position in the returned string. See
     * https://developer.android.com/training/basics/supporting-devices/languages#FormatTextExplanationSolution
     * for specific cases when this can occur. Note that the method only supports taking
     * CharSequences since all other types are likely machine-readable and need corresponding
     * resource strings to represent their contents (except potentially integers which can either be
     * directly converted to a string or formatted using [MachineLocale]). Further, null types are
     * prohibited since 'null' is not a user-readable concept.
     *
     * This method should generally only be used for strings that are about to be immediately shown
     * to the user. For strings that are intermediaries that will be part of other strings shown to
     * the user, use [formatInLocaleWithoutWrapping] & one of the other 'withWrapping' methods to
     * ensure the result is wrapped (strings shouldn't need to be wrapped multiple times for
     * bidirectional correctness).
     */
    abstract fun String.formatInLocaleWithWrapping(vararg args: CharSequence): String

    /**
     * Returns a formatted version of this string by interpolating the specified arguments, taking
     * according to this locale.
     *
     * Unlike [formatInLocaleWithWrapping], this method does not perform any bidirectional wrapping
     * for the supplied arguments. However, it has the same argument restrictions as
     * [formatInLocaleWithWrapping] since it's expected to produce strings that are meant to be
     * eventually shown to the user (it's very likely the returned value by this method will need to
     * be wrapped for bidirectional correctness by passing it to a subsequent 'withWrapping' method.
     */
    abstract fun String.formatInLocaleWithoutWrapping(vararg args: CharSequence): String

    /** Returns a locale-aware capitalized version of this string suitable for displaying in UIs. */
    abstract fun String.capitalizeForHumans(): String

    /**
     * Returns the exact resource string from this [Resources] object per the specified string
     * resource ID.
     */
    abstract fun Resources.getStringInLocale(@StringRes id: Int): String

    /**
     * Returns a formatted string as described in [formatInLocaleWithWrapping] except for a string
     * format retrieved from this [Resources] object (according to this locale). For this reason,
     * strings that include non-string parameters are not supported for wrapping. Instead, arguments
     * should be correctly converted to human-readable strings before being passed to this method.
     * This also means that the strings themselves should only ever take string types since
     * non-strings cannot be passed in.
     */
    abstract fun Resources.getStringInLocaleWithWrapping(
      @StringRes id: Int,
      vararg formatArgs: CharSequence
    ): String

    /**
     * Returns a formatted string as described in [formatInLocaleWithoutWrapping] for a resource
     * string. Unlike [getStringInLocaleWithWrapping], this method does not perform bidirectional
     * wrapping to passed arguments.
     */
    abstract fun Resources.getStringInLocaleWithoutWrapping(
      @StringRes id: Int,
      vararg formatArgs: CharSequence
    ): String

    /** Returns the string array corresponding to the specified ID from this [Resources] object. */
    abstract fun Resources.getStringArrayInLocale(@ArrayRes id: Int): List<String>

    /**
     * Returns the quantity string specified ID and for the specified [quantity] from this
     * [Resources] object.
     */
    abstract fun Resources.getQuantityStringInLocale(@PluralsRes id: Int, quantity: Int): String

    /**
     * Returns a quantity string with formatting per [getQuantityStringInLocale] and
     * [getStringInLocaleWithWrapping].
     */
    abstract fun Resources.getQuantityStringInLocaleWithWrapping(
      @PluralsRes id: Int,
      quantity: Int,
      vararg formatArgs: CharSequence
    ): String

    /**
     * Returns a quantity string with formatting per [getQuantityStringInLocale] and
     * [getStringInLocaleWithoutWrapping].
     */
    abstract fun Resources.getQuantityStringInLocaleWithoutWrapping(
      @PluralsRes id: Int,
      quantity: Int,
      vararg formatArgs: CharSequence
    ): String

    /**
     * Returns a similar result as [getQuantityStringInLocale] except as a [CharSequence] instead of
     * as a [String]. See [Resources.getQuantityText] for more details.
     */
    abstract fun Resources.getQuantityTextInLocale(@PluralsRes id: Int, quantity: Int): CharSequence
  }

  /**
   * An [OppiaLocale] representing content-based localization (such as for written translation
   * strings in lessons or audio voiceovers.
   *
   * This generally doesn't perform operations directly on strings and instead acts as a wrapper for
   * an [OppiaLocaleContext] which provides details on how to make language-based decisions
   * corresponding to content localization.
   */
  abstract class ContentLocale(override val localeContext: OppiaLocaleContext) : OppiaLocale()
}
