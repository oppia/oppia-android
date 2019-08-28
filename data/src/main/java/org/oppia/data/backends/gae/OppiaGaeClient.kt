package org.oppia.data.backends.gae

import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/** An object that contains the Retrofit configuration with the Oppia backend. */
object OppiaGaeClient {

  private var retrofit: Retrofit? = null

  val retrofitInstance: Retrofit? by lazy {
    if (retrofit == null) {
      val client = OkHttpClient.Builder()
      client.addInterceptor(NetworkInterceptor())

      retrofit = retrofit2.Retrofit.Builder()
        .baseUrl(NetworkSettings.getBaseUrl())
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client.build())
        .build()
    }
    return@lazy retrofit
  }
}
