package org.oppia.android.app.activity

import dagger.Module
import org.oppia.android.app.fragment.FragmentComponent

/** Root activity module. */
@Module(subcomponents = [FragmentComponent::class])
class ActivityModule
