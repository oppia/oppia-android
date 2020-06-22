package org.oppia.app.activity

import dagger.Module
import org.oppia.app.fragment.FragmentComponent

/** Root activity module. */
@Module(subcomponents = [FragmentComponent::class])
class ActivityModule
