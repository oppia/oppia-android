package org.oppia.util.parser

import org.json.JSONArray

/** This class parse json to mutable list of String. */
class JsonParser {
  fun parseJsonArrayToMutableListOfString(jsonArray: JSONArray): MutableList<String> {
    val list: MutableList<String> = ArrayList()
    for (i in 0..jsonArray!!.length() - 1) {
      list.add(jsonArray.get(i).toString())
    }
    return list
  }
}
