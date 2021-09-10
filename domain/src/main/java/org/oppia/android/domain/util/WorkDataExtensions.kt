package org.oppia.android.domain.util

import androidx.work.Data

fun Data.getStringFromData(key: String): String? = getString(key)
