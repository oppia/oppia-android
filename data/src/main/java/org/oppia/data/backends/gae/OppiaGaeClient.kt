package org.oppia.data.backends.gae

import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object OppiaGaeClient {

  private var retrofit: Retrofit? = null

  val retrofitInstance: Retrofit?
    get() {
      if (retrofit == null) {
        val client = OkHttpClient.Builder()
        client.addInterceptor(ModifyJsonInterceptor())

        retrofit = retrofit2.Retrofit.Builder()
          .baseUrl(NetworkSettings.BASE_URL)
          .addConverterFactory(MoshiConverterFactory.create())
          .client(client.build())
          .build()
      }
      return retrofit
    }

}
