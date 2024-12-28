package org.oppia.android.scripts.gae.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader

fun <T : Any> JsonReader.nextArray(readElement: () -> T): List<T> {
  beginArray()
  return generateSequence { maybeReadElement(readElement) }.toList().also { endArray() }
}

fun <V : Any> JsonReader.nextObject(readElement: (String) -> V): Map<String, V> {
  beginObject()
  return generateSequence { maybeReadObjectElement(readElement) }.toMap().also { endObject() }
}

inline fun <reified T : Any> JsonReader.nextCustomValue(adapter: JsonAdapter<T>): T {
  return checkNotNull(adapter.fromJson(this)) {
    "Reader does not have a next value corresponding to custom type ${T::class.simpleName} for" +
      " adapter: $adapter."
  }
}

private fun <T : Any> JsonReader.maybeReadElement(readElement: () -> T) =
  if (hasNext()) readElement() else null

private fun <V : Any> JsonReader.maybeReadObjectElement(readElement: (String) -> V): Pair<String, V>? {
  return maybeReadElement {
    nextName().let { name -> name to readElement(name) }
  }
}
