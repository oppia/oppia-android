package org.oppia.android.app.translation

import dagger.Binds
import dagger.Module

@Module
interface ActivityRecreatorProdModule {
  @Binds
  fun provideActivityRecreator(impl: ActivityRecreatorImpl): ActivityRecreator
}
