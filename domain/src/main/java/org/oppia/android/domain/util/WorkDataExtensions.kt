package org.oppia.android.domain.util

import androidx.work.Data

/**
 * Returns the string from this [Data] object corresponding to the specified key, or null if there
 * is not one.
 */
fun Data.getStringFromData(key: String): String? = getString(key)
