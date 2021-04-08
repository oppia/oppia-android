package org.oppia.android.app.mydownloads

import javax.inject.Qualifier

/**
 * Corresponds to an injectable boolean indicating whether downloads are enabled. When this
 * is false, the downloads feature itself will not be loaded.
 */
@Qualifier
annotation class EnableMyDownloads
