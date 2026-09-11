package org.oppia.android.app.application.agesignals

import android.app.Application
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** Provides the production Play Age Signals integration. */
@Module
class AgeSignalsModule {
  @Provides
  fun provideAgeSignalsManager(application: Application): AgeSignalsManager =
    AgeSignalsManagerFactory.create(application)

  @Provides
  @IntoSet
  fun provideAgeSignalsStartupListener(
    controller: AgeSignalsController
  ): ApplicationStartupListener = controller
}
