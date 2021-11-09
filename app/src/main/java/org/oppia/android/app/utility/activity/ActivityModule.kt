package org.oppia.android.app.utility.activity

import dagger.Module
import org.oppia.android.app.fragment.FragmentComponentImpl

/** Root activity module. */
@Module(subcomponents = [FragmentComponentImpl::class])
class ActivityModule
