package org.oppia.android.app.translation

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.oppia.android.domain.locale.LocaleController

@Module
class AppLanguageLocaleModule {

  @Provides
  @Singleton
  fun provideAppLanguageLocaleHandler(localeController: LocaleController): AppLanguageLocaleHandler {
    return AppLanguageLocaleHandler(localeController)
  }
}
