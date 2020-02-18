package org.oppia.app.parser

import android.content.Context

import java.io.IOException
import java.io.InputStream

// It helps to parse json file.
class JSONParser {

  fun loadJSONFromAsset(context: Context): String? {
    val jsonString: String
    try {
      jsonString = context.assets.open("currencyunits.json").bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
      ioException.printStackTrace()
      return null
    }
    return jsonString
  }
}
