package org.oppia.android.app.application

import org.oppia.android.app.translation.AppLanguageApplicationInjector
import org.oppia.android.app.translation.AppLanguageApplicationInjectorProvider
import org.oppia.android.domain.locale.LocaleApplicationInjector
import org.oppia.android.domain.locale.LocaleApplicationInjectorProvider
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.ConsoleLoggerInjector
import org.oppia.android.util.logging.ConsoleLoggerInjectorProvider
import org.oppia.android.util.system.OppiaClockInjector
import org.oppia.android.util.system.OppiaClockInjectorProvider
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider

/** Provider for [ApplicationInjector]. The application context will implement this interface. */
interface ApplicationInjectorProvider :
  DataProvidersInjectorProvider,
  AppLanguageApplicationInjectorProvider,
  OppiaClockInjectorProvider,
  LocaleApplicationInjectorProvider,
  DispatcherInjectorProvider,
  ConsoleLoggerInjectorProvider {
  fun getApplicationInjector(): ApplicationInjector

  override fun getDataProvidersInjector(): DataProvidersInjector = getApplicationInjector()

  override fun getAppLanguageApplicationInjector(): AppLanguageApplicationInjector =
    getApplicationInjector()

  override fun getOppiaClockInjector(): OppiaClockInjector = getApplicationInjector()

  override fun getLocaleApplicationInjector(): LocaleApplicationInjector = getApplicationInjector()

  override fun getDispatcherInjector(): DispatcherInjector = getApplicationInjector()

  override fun getConsoleLoggerInjector(): ConsoleLoggerInjector = getApplicationInjector()
}
