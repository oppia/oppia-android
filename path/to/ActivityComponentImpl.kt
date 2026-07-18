# complete code
package org.oppia.android.app.activity

import org.oppia.android.app.injector.ActivityComponent
import org.oppia.android.app.injector.ActivityComponentImpl
import org.oppia.android.app.injector.ActivityIntentFactories
import org.oppia.android.app.injector.ActivityIntentFactoriesModule
import org.oppia.android.app.injector.ActivityModule
import org.oppia.android.app.injector.ActivityScope
import org.oppia.android.app.injector.Injector

class ActivityComponentImpl(
    private val activityIntentFactories: ActivityIntentFactories,
    private val activityModule: ActivityModule,
    private val activityScope: ActivityScope,
    private val injector: Injector
) : ActivityComponentImpl() {
    override fun get(activity: String): ActivityComponentImpl {
        return ActivityComponentImpl(
            activityIntentFactories,
            activityModule,
            activityScope,
            injector
        )
    }
}