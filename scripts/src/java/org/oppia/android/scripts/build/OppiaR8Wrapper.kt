package org.oppia.android.scripts.build

import com.android.tools.r8.R8
import java.io.File

/**
 * The main entrypoint for invoking R8 in full mode from Bazel's `--proguard_top` hook.
 *
 * Bazel `6.5.0`'s native `android_binary` rule passes legacy Proguard CLI arguments and expects a
 * shrunk Java bytecode JAR before Bazel's separate desugar and D8 dexing step. This wrapper
 * translates those Proguard flags into R8 `--release --classfile` arguments and delegates to
 * [R8.main].
 *
 * Usage:
 *   bazel run //scripts:r8_compat_proguard -- <proguard_cli_args...>
 */
fun main(vararg args: String) {
  OppiaR8Wrapper().run(args)
}

/**
 * Translates legacy Proguard CLI arguments into R8 `--release --classfile` arguments and runs R8.
 *
 * @property r8Runner callback that executes R8 with the translated command-line arguments
 */
class OppiaR8Wrapper(private val r8Runner: (Array<String>) -> Unit = R8::main) {
  /** Translates the provided Proguard [args] and invokes [r8Runner]. */
  fun run(args: Array<out String>) {
    val r8Args = translateArgs(args)
    r8Runner(r8Args.toTypedArray())
  }

  private fun translateArgs(args: Array<out String>): List<String> {
    return sequence {
      yield("--release")
      yield("--classfile")

      val iter = args.iterator()
      while (iter.hasNext()) {
        val arg = iter.next()
        if (arg.startsWith("@")) {
          val specPath = arg.removePrefix("@")
          require(specPath.isNotEmpty()) { "Expected a Proguard configuration path after '@'." }
          yield("--pg-conf")
          yield(specPath)
          continue
        }
        when (arg) {
          "-forceprocessing" -> continue
          "-injars" -> yield(requireNextArg(iter, arg))
          "-outjars" -> {
            yield("--output")
            yield(requireNextArg(iter, arg))
          }
          "-libraryjars" -> {
            yield("--lib")
            yield(requireNextArg(iter, arg))
          }
          "-printmapping" -> {
            yield("--pg-map-output")
            yield(requireNextArg(iter, arg))
          }
          "-printconfiguration" -> {
            yield("--pg-conf-output")
            yield(requireNextArg(iter, arg))
          }
          "-printseeds", "-printusage" -> {
            // Native android_binary declares these outputs, so create empty placeholders for R8.
            val outputFile = File(requireNextArg(iter, arg))
            outputFile.parentFile?.mkdirs()
            outputFile.createNewFile()
          }
          else -> yield(arg)
        }
      }
    }.toList()
  }

  private fun requireNextArg(iter: Iterator<String>, flag: String): String {
    require(iter.hasNext()) { "Expected an argument value after $flag." }
    return iter.next()
  }
}
