package org.oppia.android.util.properties

interface CustomPropertyRetriever {
  fun getPropertyString(name: String): String?

  fun getPropertyInt(name: String): Int?

  fun getPropertyBoolean(name: String): Boolean?
}
