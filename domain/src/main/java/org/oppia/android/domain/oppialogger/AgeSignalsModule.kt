package org.oppia.android.domain.oppialogger

import android.content.Context
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Singleton

/** Module that provides age signals startup listener. */
@Module
class AgeSignalsModule {
  @Provides
  @Singleton
  fun provideAgeSignalsManager(context: Context): AgeSignalsManager {
    return AgeSignalsManagerFactory.create(context)
  }

  @Provides
  @IntoSet
  fun bindAgeSignalsImporter(
    ageSignalsImporter: AgeSignalsImporter
  ): ApplicationStartupListener = ageSignalsImporter
}
