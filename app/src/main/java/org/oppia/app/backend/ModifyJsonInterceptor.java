package org.oppia.app.backend;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ModifyJsonInterceptor implements Interceptor {

  //TODO: Transfer this XSSI_PREFIX to a constant file
  // which is responsible for networking too.
  private final static String XSSI_PREFIX = ")]}\'\n";
  private static final String TAG = ModifyJsonInterceptor.class.getSimpleName();

  @Override
  public Response intercept(Chain chain) throws IOException {

    Request request = chain.request();
    Response response = chain.proceed(request);

    if (response.code() == 200) {
      if (response.body() != null) {
        String rawJson = response.body().string();

        Log.d(TAG, "intercept: rawJson: " + rawJson);

        if (rawJson.startsWith(XSSI_PREFIX)) {
          rawJson = rawJson.substring(rawJson.indexOf('\n') + 1);
        }

        Log.d(TAG, "intercept: final: " + rawJson);

        MediaType contentType = response.body().contentType();
        ResponseBody body = ResponseBody.create(contentType, rawJson);
        return response.newBuilder().body(body).build();
      }
    } else if (response.code() == 403) {
      //TODO: Manage other network errors here
    }

    return response;

  }

}
