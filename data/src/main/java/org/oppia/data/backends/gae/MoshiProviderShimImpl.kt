package org.oppia.data.backends.gae

import retrofit2.Converter
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

// TODO(#1619): Remove file post-Gradle
class MoshiProviderShimImpl @Inject constructor() : MoshiProviderShim {

  override fun getConverterFactory(): Converter.Factory {
    return MoshiConverterFactory.create()
  }
}
