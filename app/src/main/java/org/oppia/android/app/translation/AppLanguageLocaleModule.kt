package org.oppia.android.app.translation

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.locale.LocaleController
import javax.inject.Singleton

@Module
class AppLanguageLocaleModule {

  @Provides
  @Singleton
  fun provideAppLanguageLocaleHandler(localeController: LocaleController): AppLanguageLocaleHandler {
    return AppLanguageLocaleHandler(localeController)
  }
}
