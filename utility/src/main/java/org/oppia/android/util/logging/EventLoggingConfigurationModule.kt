package org.oppia.android.util.logging

import dagger.Binds
import dagger.Module

/** Module to provide standard configurations for the event logging infrastructure. */
@Module
interface EventLoggingConfigurationModule {
  @Binds
  fun bindStandardEventTypeToHumanReadableNameConverter(
    impl: StandardEventTypeToHumanReadableNameConverterImpl
  ): EventTypeToHumanReadableNameConverter
}
