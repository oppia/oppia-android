package org.oppia.android.util.properties

interface CustomPropertyRetriever {
  fun getString(name: String): String?

  fun getInt(name: String): Int?

  fun getBoolean(name: String): Boolean?
}
