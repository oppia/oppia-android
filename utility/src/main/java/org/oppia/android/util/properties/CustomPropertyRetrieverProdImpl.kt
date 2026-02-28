package org.oppia.android.util.properties

import javax.inject.Inject

class CustomPropertyRetrieverProdImpl @Inject constructor() : CustomPropertyRetriever {
  override fun getString(name: String): String? = null
  override fun getInt(name: String): Int? = null
  override fun getBoolean(name: String): Boolean? = null
}
