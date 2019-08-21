package org.oppia.data.backends.gae

import com.google.gson.GsonBuilder
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object OppiaGaeClient {

  private var retrofit: Retrofit? = null
  private const val BASE_URL = "https://oppia.org"

  val retrofitInstance: Retrofit?
    get() {

      if (retrofit == null) {

        val gson = GsonBuilder()
          .setLenient()
          .create()

        val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()

        retrofit = retrofit2.Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(GsonConverterFactory.create(gson))
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()
      }
      return retrofit

    }

}
