package org.oppia.android.tools.android

import com.android.tools.r8.R8
import java.io.File

fun main(args: Array<String>) {
  val r8Args = sequence {
    yield("--release")
    yield("--classfile")

    val iter = args.iterator()
    while (iter.hasNext()) {
      val arg = iter.next()
      if (arg.startsWith("@")) {
        yield("--pg-conf")
        yield(arg.removePrefix("@"))
        continue
      }
      when (arg) {
        "-forceprocessing", "-injars" -> continue // Skip these.
        "-outjars" -> yield("--output")
        "-libraryjars" -> yield("--lib")
        "-printmapping" -> yield("--pg-map-output")
        "-printconfiguration" -> yield("--pg-conf-output")
        "-printseeds", "-printusage" -> File(iter.next()).createNewFile()
        else -> yield(arg)
      }
    }
  }.toList()
  R8.main(r8Args.toTypedArray())
}
