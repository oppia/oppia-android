package org.oppia.data.backends.gae

import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

//TODO: Transfer this XSSI_PREFIX to a constant file
// which is responsible for networking too.
private const val BASE_URL = "https://oppia.org"

object OppiaGaeClient {

  private var retrofit: Retrofit? = null

  val retrofitInstance: Retrofit?
    get() {
      if (retrofit == null) {
        val client = OkHttpClient.Builder()
        client.addInterceptor(ModifyJsonInterceptor())

        retrofit = retrofit2.Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(MoshiConverterFactory.create())
          .client(client.build())
          .build()
      }
      return retrofit
    }

}
