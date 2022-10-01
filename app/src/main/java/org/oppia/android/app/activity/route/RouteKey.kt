package org.oppia.android.app.activity.route

import dagger.MapKey
import org.oppia.android.app.model.DestinationScreen

/** Corresponds to an injectable [ActivityRouter]. */
@MapKey
annotation class RouteKey(val value: DestinationScreen.DestinationScreenCase)
