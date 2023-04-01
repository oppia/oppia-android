package org.oppia.android.app.activity.route

import dagger.MapKey
import org.oppia.android.app.model.DestinationScreen

/** Specifies [DestinationScreenCase] which can be used to pass in activity Route. */
@MapKey
annotation class RouteKey(val value: DestinationScreen.DestinationScreenCase)
