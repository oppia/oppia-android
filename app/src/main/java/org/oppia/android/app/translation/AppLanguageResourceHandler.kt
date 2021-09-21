package org.oppia.android.app.translation

import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import org.oppia.android.util.locale.OppiaLocale

class AppLanguageResourceHandler @Inject constructor(
  private val activity: AppCompatActivity,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler
) {
  private val resources by lazy { activity.resources }

  fun formatInLocaleWithWrapping(format: String, vararg formatArgs: String): String {
    return getDisplayLocale().run { format.formatInLocaleWithWrapping(*formatArgs) }
  }

  fun formatInLocaleWithoutWrapping(format: String, vararg formatArgs: String): String {
    return getDisplayLocale().run { format.formatInLocaleWithoutWrapping(*formatArgs) }
  }

  fun capitalizeForHumans(str: String): String {
    return getDisplayLocale().run { str.capitalizeForHumans() }
  }

  fun getStringInLocale(@StringRes id: Int): String {
    return getDisplayLocale().run { resources.getStringInLocale(id) }
  }

  fun getStringInLocaleWithWrapping(@StringRes id: Int, vararg formatArgs: CharSequence): String {
    return getDisplayLocale().run { resources.getStringInLocaleWithWrapping(id, *formatArgs) }
  }

  fun getStringInLocaleWithoutWrapping(
    @StringRes id: Int, vararg formatArgs: CharSequence
  ): String {
    return getDisplayLocale().run { resources.getStringInLocaleWithoutWrapping(id, *formatArgs) }
  }

  fun getStringArrayInLocale(@ArrayRes id: Int): List<String> {
    return getDisplayLocale().run { resources.getStringArrayInLocale(id) }
  }

  fun getQuantityStringInLocale(@PluralsRes id: Int, quantity: Int): String {
    return getDisplayLocale().run { resources.getQuantityStringInLocale(id, quantity) }
  }

  fun getQuantityStringInLocaleWithWrapping(
    @PluralsRes id: Int, quantity: Int, vararg formatArgs: CharSequence
  ): String {
    return getDisplayLocale().run {
      resources.getQuantityStringInLocaleWithWrapping(id, quantity, *formatArgs)
    }
  }

  fun getQuantityStringInLocaleWithoutWrapping(
    @PluralsRes id: Int, quantity: Int, vararg formatArgs: CharSequence
  ): String {
    return getDisplayLocale().run {
      resources.getQuantityStringInLocaleWithoutWrapping(id, quantity, *formatArgs)
    }
  }

  fun computeDateString(timestampMillis: Long): String =
    getDisplayLocale().computeDateString(timestampMillis)

  fun computeDateTimeString(timestampMillis: Long): String =
    getDisplayLocale().computeDateTimeString(timestampMillis)

  private fun getDisplayLocale(): OppiaLocale.DisplayLocale =
    appLanguageLocaleHandler.getDisplayLocale()
}
