package org.oppia.android.scripts.common

import com.google.protobuf.MessageLite
import java.io.File
import java.nio.file.Files
import java.util.Properties
import java.util.Random

// TODO: Move to common? Maybe can be consolidated with RepositoryFile?
//  An idea might be to introduce a general-purpose file loading system since that could be used for
//  a bunch of different types: XML, Kotlin, .class, Jar, .gz archives, proto, text files, ini, etc.

object FileUtils {
  fun openFile(path: String, verifyDoesExist: Boolean = true, lazyMessage: (File) -> Any) =
    openFile(parent = null, path, verifyDoesExist, lazyMessage)

  fun openFile(
    parent: File?, path: String, verifyDoesExist: Boolean = true, lazyMessage: (File) -> Any
  ): File {
    return File(parent, path).also {
      check(it.exists() == verifyDoesExist) { lazyMessage(it) }
    }
  }

  inline fun <reified T : MessageLite> loadProtoFile(path: String, default: T): T {
    return openFile(path) {
      "Failed to load proto file at path: ${it.absolutePath}."
    }.inputStream().use { default.newBuilderForType().mergeFrom(it).build() as T }
  }

  fun File.loadAsProperties(): Map<String, String> {
    return Properties().also { properties ->
      inputStream().use { inputStream -> properties.load(inputStream) }
    }.mapKeys { (key, _) -> key as String }.mapValues { (_, value) -> value as String }
  }

  fun Map<String, String>.storePropertiesTo(file: File) {
    val properties = Properties().also { it.putAll(this) }
    file.outputStream().use { properties.store(it, /* comment= */ null) }
  }

  // TODO: Remove this?
  fun List<File>.computeCommonBase(): File {
    require(isNotEmpty()) { "At least one file is needed to compute a common base." }
    require(all { it.exists() }) { "All provided files must exist." }

    // Use absolute files to ensure the root is considered up to the filesystem root.
    val immediateParents = map { it.absoluteFile.expandToAllParents().toList().asReversed() }
    val maxDepth = immediateParents.minOf { it.size }
    val parentTree = Array(maxDepth) { parentIndex ->
      Array(immediateParents.size) { outerFileIndex ->
        immediateParents[outerFileIndex][parentIndex]
      }
    }

    // Note that the find() will fail to find anything if all files are the same (such as in the
    // case that exactly 1 file is passed).
    val firstIndexWithDifferingParents = parentTree.withIndex().find { (_, dirsAtSameLevel) ->
      // If any of the file paths are different, the directories at this "level" in the tree
      // cannot be considered a single base.
      dirsAtSameLevel.distinct().size != 1
    }?.index ?: return checkNotNull(first().parentFile) {
      "Files impossibly have no parents: ${first()}."
    }

    check(firstIndexWithDifferingParents > 0) {
      "All files have completely different roots (expected a single common root): $this."
    }

    // While the extra distinct/single isn't necessary, it introduces a nice peace-of-mind that
    // exactly one root has been found by this point.
    val absoluteRoot = File(parentTree[firstIndexWithDifferingParents - 1].distinct().single())
    return checkNotNull(absoluteRoot.relativeToOrNull(File("."))) {
      "Expected file root $absoluteRoot to be relative to the current working directory."
    }
  }

  fun File.toAbsoluteNormalizedFile(): File = absoluteFile.normalize()

  fun File.toAbsoluteNormalizedPath(): String = toAbsoluteNormalizedFile().path

  fun createTempDirectory(prefix: String): File = Files.createTempDirectory(prefix).toFile()

  private fun File.expandToAllParents() =
    generateSequence(parentFile) { it.parentFile }.map { it.absolutePath }
}
