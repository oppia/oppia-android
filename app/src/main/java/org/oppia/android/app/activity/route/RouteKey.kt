package org.oppia.android.app.activity.route

import dagger.MapKey
import org.oppia.android.app.model.DestinationScreen

@MapKey
annotation class RouteKey(val value: DestinationScreen.DestinationScreenCase)
