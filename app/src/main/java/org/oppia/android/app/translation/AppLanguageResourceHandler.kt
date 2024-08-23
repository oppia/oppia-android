package org.oppia.android.app.translation

import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.util.locale.OppiaLocale
import java.util.Locale
import javax.inject.Inject

/**
 * Convenience resource handler for formatting user-readable strings, and retrieving/formatting
 * multiple string resources (including quantity strings & string arrays).
 *
 * This class is backed by the current [OppiaLocale.DisplayLocale] initialized for the app layer to
 * ensure string formatting is consistent with the current locale (& properly wraps arguments in
 * string formatting cases for bidirectional rendering).
 *
 * This class must be used for all string & string resource uses in the app layer for strings that
 * will eventually be shown to the user (non-user facing strings can make use of the utilities
 * available in [OppiaLocale.MachineLocale].
 *
 * This class is activity-injectable & automatically pulls resources for the current activity
 * (rather than needing to manually connect the current activity's resources with a display locale
 * instance).
 */
class AppLanguageResourceHandler @Inject constructor(
  private val activity: AppCompatActivity,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler
) {
  private val resources by lazy { activity.resources }

  /** See [OppiaLocale.DisplayLocale.formatInLocaleWithWrapping]. */
  fun formatInLocaleWithWrapping(format: String, vararg formatArgs: String): String {
    return getDisplayLocale().run { format.formatInLocaleWithWrapping(*formatArgs) }
  }

  /** See [OppiaLocale.DisplayLocale.formatInLocaleWithoutWrapping]. */
  fun formatInLocaleWithoutWrapping(format: String, vararg formatArgs: String): String {
    return getDisplayLocale().run { format.formatInLocaleWithoutWrapping(*formatArgs) }
  }

  /** See [OppiaLocale.DisplayLocale.capitalizeForHumans]. */
  fun capitalizeForHumans(str: String): String {
    return getDisplayLocale().run { str.capitalizeForHumans() }
  }

  /**
   * See [OppiaLocale.DisplayLocale.getStringInLocale] for specifics. This returns a string
   * corresponding to the current activity's resources.
   */
  fun getStringInLocale(@StringRes id: Int): String {
    return getDisplayLocale().run { resources.getStringInLocale(id) }
  }

  /**
   * See [OppiaLocale.DisplayLocale.getStringInLocaleWithWrapping] for specifics. This returns a
   * string corresponding to the current activity's resources.
   */
  fun getStringInLocaleWithWrapping(@StringRes id: Int, vararg formatArgs: CharSequence): String {
    return getDisplayLocale().run { resources.getStringInLocaleWithWrapping(id, *formatArgs) }
  }

  /**
   * See [OppiaLocale.DisplayLocale.getStringInLocaleWithoutWrapping] for specifics. This returns a
   * string corresponding to the current activity's resources.
   */
  fun getStringInLocaleWithoutWrapping(
    @StringRes id: Int,
    vararg formatArgs: CharSequence
  ): String {
    return getDisplayLocale().run { resources.getStringInLocaleWithoutWrapping(id, *formatArgs) }
  }

  /**
   * See [OppiaLocale.DisplayLocale.getStringArrayInLocale] for specifics. This returns a string
   * array corresponding to the current activity's resources.
   */
  fun getStringArrayInLocale(@ArrayRes id: Int): List<String> {
    return getDisplayLocale().run { resources.getStringArrayInLocale(id) }
  }

  /**
   * See [OppiaLocale.DisplayLocale.getQuantityStringInLocale] for specifics. This returns a
   * quantity string corresponding to the current activity's resources.
   */
  fun getQuantityStringInLocale(@PluralsRes id: Int, quantity: Int): String {
    return getDisplayLocale().run { resources.getQuantityStringInLocale(id, quantity) }
  }

  /**
   * See [OppiaLocale.DisplayLocale.getQuantityStringInLocaleWithWrapping] for specifics. This
   * returns a quantity string corresponding to the current activity's resources.
   */
  fun getQuantityStringInLocaleWithWrapping(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: CharSequence
  ): String {
    return getDisplayLocale().run {
      resources.getQuantityStringInLocaleWithWrapping(id, quantity, *formatArgs)
    }
  }

  /**
   * See [OppiaLocale.DisplayLocale.getQuantityStringInLocaleWithoutWrapping] for specifics. This
   * returns a quantity string corresponding to the current activity's resources.
   */
  fun getQuantityStringInLocaleWithoutWrapping(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: CharSequence
  ): String {
    return getDisplayLocale().run {
      resources.getQuantityStringInLocaleWithoutWrapping(id, quantity, *formatArgs)
    }
  }

  /** See [OppiaLocale.DisplayLocale.formatLong] for specific behavior. */
  fun formatLong(value: Long): String = getDisplayLocale().formatLong(value)

  /** See [OppiaLocale.DisplayLocale.formatDouble] for specific behavior. */
  fun formatDouble(value: Double): String = getDisplayLocale().formatDouble(value)

  /** See [OppiaLocale.DisplayLocale.toHumanReadableString] for specific behavior. */
  fun toHumanReadableString(number: Int): String =
    getDisplayLocale().toHumanReadableString(number)

  /** See [OppiaLocale.DisplayLocale.computeDateString]. */
  fun computeDateString(timestampMillis: Long): String =
    getDisplayLocale().computeDateString(timestampMillis)

  /** See [OppiaLocale.DisplayLocale.computeDateTimeString]. */
  fun computeDateTimeString(timestampMillis: Long): String =
    getDisplayLocale().computeDateTimeString(timestampMillis)

  /** See [OppiaLocale.DisplayLocale.getLayoutDirection]. */
  fun getLayoutDirection(): Int = getDisplayLocale().getLayoutDirection()

  /** Returns the current [OppiaLocale.DisplayLocale] used for resource processing. */
  fun getDisplayLocale(): OppiaLocale.DisplayLocale = appLanguageLocaleHandler.getDisplayLocale()

  // TODO(#3793): Remove this once OppiaLanguage is used as the source of truth.
  /**
   * Returns a human-readable, localized representation of the specified [AudioLanguage].
   *
   * Note that the returned string is not expected to be localized to the user's current locale.
   * Instead, it will be localized for that specific language (i.e. each language will be
   * represented within that language to make it easier to identify when choosing a language).
   */
  fun computeLocalizedDisplayName(audioLanguage: AudioLanguage): String {
    return when (audioLanguage) {
      AudioLanguage.HINDI_AUDIO_LANGUAGE -> getLocalizedDisplayName("hi")
      AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE -> getLocalizedDisplayName("pt", "BR")
      AudioLanguage.ARABIC_LANGUAGE -> getLocalizedDisplayName("ar", "EG")
      AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE ->
        resources.getString(R.string.nigerian_pidgin_localized_language_name)
      AudioLanguage.NO_AUDIO, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED, AudioLanguage.UNRECOGNIZED,
      AudioLanguage.ENGLISH_AUDIO_LANGUAGE -> getLocalizedDisplayName("en")
    }
  }

  /**
   * Returns a human-readable, localized representation of the specified [OppiaLanguage].
   * Note that the returned string is not expected to be localized to the user's current locale.
   * Instead, it will be localized for that specific language (i.e. each language will be
   * represented within that language to make it easier to identify when choosing a language).
   */
  fun computeLocalizedDisplayName(oppiaLanguage: OppiaLanguage): String {
    return when (oppiaLanguage) {
      OppiaLanguage.HINDI -> resources.getString(R.string.hindi_localized_language_name)
      OppiaLanguage.PORTUGUESE ->
        resources.getString(R.string.portuguese_localized_language_name)
      OppiaLanguage.SWAHILI -> resources.getString(R.string.swahili_localized_language_name)
      OppiaLanguage.BRAZILIAN_PORTUGUESE ->
        resources.getString(R.string.brazilian_portuguese_localized_language_name)
      OppiaLanguage.UNRECOGNIZED, OppiaLanguage.LANGUAGE_UNSPECIFIED,
      OppiaLanguage.ENGLISH -> resources.getString(R.string.english_localized_language_name)
      OppiaLanguage.ARABIC -> resources.getString(R.string.arabic_localized_language_name)
      OppiaLanguage.HINGLISH -> resources.getString(R.string.hinglish_localized_language_name)
      OppiaLanguage.NIGERIAN_PIDGIN ->
        resources.getString(R.string.nigerian_pidgin_localized_language_name)
    }
  }

  /**
   * Returns an [OppiaLanguage] from its human-readable, localized representation.
   * It is expected that each input string is localized to the user's current locale, as per
   * [computeLocalizedDisplayName].
   */
  fun getOppiaLanguageFromDisplayName(displayName: String): OppiaLanguage {
    val localizedNameMap = OppiaLanguage.values()
      .filter { it !in IGNORED_OPPIA_LANGUAGES }
      .associateBy { computeLocalizedDisplayName(it) }
    return localizedNameMap[displayName] ?: OppiaLanguage.ENGLISH
  }

  private fun getLocalizedDisplayName(languageCode: String, regionCode: String = ""): String {
    // TODO(#3791): Remove this dependency.
    val locale = Locale(languageCode, regionCode)
    return locale.getDisplayLanguage(locale).replaceFirstChar {
      if (it.isLowerCase()) it.titlecase(locale) else it.toString()
    }
  }

  private companion object {
    private val IGNORED_AUDIO_LANGUAGES =
      listOf(
        AudioLanguage.NO_AUDIO, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED, AudioLanguage.UNRECOGNIZED
      )

    private val IGNORED_OPPIA_LANGUAGES =
      listOf(OppiaLanguage.LANGUAGE_UNSPECIFIED, OppiaLanguage.UNRECOGNIZED)
  }
}
