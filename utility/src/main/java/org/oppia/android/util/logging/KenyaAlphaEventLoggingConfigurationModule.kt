package org.oppia.android.util.logging

import dagger.Binds
import dagger.Module

/**
 * Module to provide event logging infrastructure configurations specific to Kenya study alpha
 * builds of the app.
 */
@Module
interface KenyaAlphaEventLoggingConfigurationModule {
  @Binds
  fun bindKenyaAlphaSpecificEventTypeToHumanReadableNameConverter(
    impl: KenyaAlphaEventTypeToHumanReadableNameConverterImpl
  ): EventTypeToHumanReadableNameConverter
}
