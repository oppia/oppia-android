package org.oppia.android.domain.util

import org.json.JSONArray
import org.json.JSONObject

/** Returns the string from this array corresponding to the specified index. */
fun JSONArray.getStringFromArray(index: Int): String = getString(index)

/** Returns the string from this object corresponding to the specified property name. */
fun JSONObject.getStringFromObject(name: String): String = getString(name)
