package org.oppia.android.domain.util

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.getStringFromArray(index: Int): String = getString(index)

fun JSONObject.getStringFromObject(name: String): String = getString(name)
