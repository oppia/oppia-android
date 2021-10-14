package org.oppia.android.app.fragment

import dagger.Module
import org.oppia.android.app.view.ViewComponentImpl

/** Root fragment module. */
@Module(subcomponents = [ViewComponentImpl::class])
class FragmentModule
