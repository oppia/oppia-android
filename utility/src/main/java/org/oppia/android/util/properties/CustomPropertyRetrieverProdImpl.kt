package org.oppia.android.util.properties

import javax.inject.Inject

/**
 * A production implementation of [CustomPropertyRetriever] that acts as a no-op since configurable
 * settings should only be used in developer builds of the app.
 */
class CustomPropertyRetrieverProdImpl @Inject constructor() : CustomPropertyRetriever {
  override fun getPropertyString(name: String): String? = null
  override fun getPropertyInt(name: String): Int? = null
  override fun getPropertyBoolean(name: String): Boolean? = null
}
