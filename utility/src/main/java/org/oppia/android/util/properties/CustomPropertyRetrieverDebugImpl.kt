package org.oppia.android.util.properties

import android.content.Context
import android.provider.Settings
import javax.inject.Inject

class CustomPropertyRetrieverDebugImpl @Inject constructor(
  private val context: Context
) : CustomPropertyRetriever {
  private val packageName = "org.oppia.android"
  private val contentResolver by lazy { context.contentResolver }

  override fun getString(name: String): String? =
    Settings.Global.getString(contentResolver, computeQualifiedPropertyName(name))

  override fun getInt(name: String): Int? = getString(name)?.toIntOrNull()

  override fun getBoolean(name: String): Boolean? = getString(name)?.toBooleanStrictOrNull()

  private fun computeQualifiedPropertyName(name: String) = "$packageName.$name"
}
