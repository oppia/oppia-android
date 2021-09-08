package org.oppia.android.app.translation

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import org.oppia.android.domain.locale.OppiaLocale

class AppLanguageResourceHandler @Inject constructor(
  private val activity: AppCompatActivity,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler
) {
  private val resources by lazy { activity.resources }

  fun getStringInLocale(@StringRes id: Int): String {
//    ensureLocaleIsInitialized()
    return getDisplayLocale().run { resources.getStringInLocale(id) }
  }

  fun getStringInLocale(@StringRes id: Int, vararg formatArgs: Any?): String {
//    ensureLocaleIsInitialized()
    return getDisplayLocale().run { resources.getStringInLocale(id, *formatArgs) }
  }

  fun getStringArrayInLocale(@ArrayRes id: Int): List<String> {
//    ensureLocaleIsInitialized()
    return getDisplayLocale().run { resources.getStringArrayInLocale(id) }
  }

  private fun getDisplayLocale(): OppiaLocale.DisplayLocale =
    appLanguageLocaleHandler.getDisplayLocale()
}
