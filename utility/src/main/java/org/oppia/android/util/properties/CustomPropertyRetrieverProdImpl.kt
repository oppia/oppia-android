package org.oppia.android.util.properties

import javax.inject.Inject

class CustomPropertyRetrieverProdImpl @Inject constructor() : CustomPropertyRetriever {
  override fun getPropertyString(name: String): String? = null
  override fun getPropertyInt(name: String): Int? = null
  override fun getPropertyBoolean(name: String): Boolean? = null
}
