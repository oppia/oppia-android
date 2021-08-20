package org.oppia.android.data.backends.gae

import javax.inject.Qualifier

/**
 * Qualifier for an application-injectable string representing the prefix in Json response used for
 * security in API calls.
 */
@Qualifier
annotation class XssiPrefix
