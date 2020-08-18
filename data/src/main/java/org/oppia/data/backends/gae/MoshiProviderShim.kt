package org.oppia.data.backends.gae

import retrofit2.Converter

// TODO(#1619): Remove file post-Gradle
interface MoshiProviderShim {

  fun getConverterFactory(): Converter.Factory
}
