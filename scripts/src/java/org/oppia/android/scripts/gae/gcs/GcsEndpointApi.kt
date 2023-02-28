package org.oppia.android.scripts.gae.gcs

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Streaming

interface GcsEndpointApi {
  @GET("{gcs_bucket}/{entity_type}/{entity_id}/assets/{image_type}/{image_filename}")
  @Headers("Content-Type:application/octet-stream")
  @Streaming
  fun fetchImageData(
    @Path("gcs_bucket") gcsBucket: String,
    @Path("entity_type") entityType: String,
    @Path("entity_id") entityId: String,
    @Path("image_type") imageType: String,
    @Path("image_filename") imageFilename: String
  ): Call<ResponseBody>
}
