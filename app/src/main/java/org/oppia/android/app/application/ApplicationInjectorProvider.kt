package org.oppia.android.app.application

import org.oppia.android.app.translation.AppLanguageApplicationInjector
import org.oppia.android.app.translation.AppLanguageApplicationInjectorProvider
import org.oppia.android.domain.locale.LocaleApplicationInjector
import org.oppia.android.domain.locale.LocaleApplicationInjectorProvider
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.parser.image.ImageLoaderInjectorProvider
import org.oppia.android.util.system.OppiaClockInjector
import org.oppia.android.util.system.OppiaClockInjectorProvider

/** Provider for [ApplicationInjector]. The application context will implement this interface. */
interface ApplicationInjectorProvider :
  DataProvidersInjectorProvider,
  AppLanguageApplicationInjectorProvider,
  OppiaClockInjectorProvider,
  LocaleApplicationInjectorProvider,
  ImageLoaderInjectorProvider {
  fun getApplicationInjector(): ApplicationInjector

  override fun getDataProvidersInjector(): DataProvidersInjector = getApplicationInjector()

  override fun getAppLanguageApplicationInjector(): AppLanguageApplicationInjector =
    getApplicationInjector()

  override fun getOppiaClockInjector(): OppiaClockInjector = getApplicationInjector()

  override fun getLocaleApplicationInjector(): LocaleApplicationInjector = getApplicationInjector()
}
