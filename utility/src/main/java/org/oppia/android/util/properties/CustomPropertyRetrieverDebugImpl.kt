package org.oppia.android.util.properties

import android.content.Context
import android.provider.Settings
import javax.inject.Inject

/**
 * A debug implementation of [CustomPropertyRetriever] that fetches properties from the Android
 * device's global settings.
 */
class CustomPropertyRetrieverDebugImpl @Inject constructor(
  private val context: Context
) : CustomPropertyRetriever {
  private val packageName = "org.oppia.android"
  private val contentResolver by lazy { context.contentResolver }

  override fun getPropertyString(name: String): String? =
    Settings.Global.getString(contentResolver, computeQualifiedPropertyName(name))

  override fun getPropertyInt(name: String): Int? = getPropertyString(name)?.toIntOrNull()

  override fun getPropertyBoolean(name: String): Boolean? =
    getPropertyString(name)?.toBooleanStrictOrNull()

  private fun computeQualifiedPropertyName(name: String) = "$packageName.$name"
}
