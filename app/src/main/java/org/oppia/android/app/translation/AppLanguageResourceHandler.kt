package org.oppia.android.app.translation

import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.util.locale.OppiaLocale
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

  /** See [OppiaLocale.DisplayLocale.computeDateString]. */
  fun computeDateString(timestampMillis: Long): String =
    getDisplayLocale().computeDateString(timestampMillis)

  /** See [OppiaLocale.DisplayLocale.computeDateTimeString]. */
  fun computeDateTimeString(timestampMillis: Long): String =
    getDisplayLocale().computeDateTimeString(timestampMillis)

  /** See [OppiaLocale.DisplayLocale.getLayoutDirection]. */
  fun getLayoutDirection(): Int = getDisplayLocale().getLayoutDirection()

  fun getDisplayLocale(): OppiaLocale.DisplayLocale =
    appLanguageLocaleHandler.getDisplayLocale()
}
