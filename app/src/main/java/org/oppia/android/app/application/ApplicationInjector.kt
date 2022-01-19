package org.oppia.android.app.application

import org.oppia.android.app.translation.AppLanguageApplicationInjector
import org.oppia.android.domain.locale.LocaleApplicationInjector
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.parser.image.ImageLoaderInjector
import org.oppia.android.util.system.OppiaClockInjector

/** Injector for application-level dependencies that can't be directly injected where needed. */
interface ApplicationInjector :
  DataProvidersInjector,
  AppLanguageApplicationInjector,
  OppiaClockInjector,
  LocaleApplicationInjector,
  ImageLoaderInjector
