package org.oppia.android.scripts.emulator

import java.io.File
import java.io.PrintStream
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.xml.parsers.DocumentBuilderFactory
import org.oppia.android.scripts.proto.AvailableSystemImage
import org.oppia.android.scripts.proto.AvailableSystemImages
import org.oppia.android.scripts.proto.OsSplitSystemImage
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList

private const val REPOSITORY_BASE_URL = "https://dl.google.com/android/repository"

fun main(vararg args: String) {
  require(args.size == 1) {
    "Usage: bazel run //scripts:system_image_list_generator --" +
      " </absolute/path/to/system_images_list.bzl>"
  }
  val systemImageListPath = args[0]
  SystemImageListGenerator(File(systemImageListPath)).downloadSystemImageList()
}

// TODO: Refactor LicenseFetcher to be a general page scraper, then use it here so that this generator can be tested.
class SystemImageListGenerator(private val systemImageListFile: File) {
  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }
  private val sha1Digest by lazy { MessageDigest.getInstance("SHA-1") }
  private val sha256Digest by lazy { MessageDigest.getInstance("SHA-256") }
  private val buffer by lazy { ByteArray(DEFAULT_BUFFER_SIZE) }
  private val cachedSha1ToSha256Map = ConcurrentHashMap<String, String>()

  fun downloadSystemImageList() {
    println("Loading local and remote system images lists...")
    val availableSystemImages = File("scripts", "available_system_images.pb").inputStream().use {
      AvailableSystemImages.parseFrom(it)
    }
    val currentSystemImages = retrieveCurrentSystemImages(availableSystemImages)
    val requestedSystemImages = availableSystemImages.requestedSystemImageList
    val sysImageUrls = downloadSysImageUrls()

    // Note that packages sometimes redefine their system images, so associateBy() will arbitrarily
    // pick tiebreakers (more specifically, it's based on the order of the repositories in the main
    // manifest that dictates this). It's not clear why these are duplicative in the repositories,
    // or what the differences are between the different builds.
    val packages = sysImageUrls.flatMap {
      downloadSystemImagePackageList(it)
    }.groupBy(keySelector = { it.first }, valueTransform = { it.second }).filter { (key, _) ->
      // Filter out system images that will definitely not be used for emulation.
      key.apiLevel >= 19 && (key.tagName == "default" || key.tagName == "google_apis")
    }.mapValues { (_, systemImages) -> systemImages.associateBy { it.archive.hostOs } }
    val matchedPackages = packages.filter { (key, _) -> key.path in requestedSystemImages }
    val encounteredPaths = matchedPackages.keys.map { it.path }.toSet()
    val expectedPaths = requestedSystemImages.toSet()
    check(encounteredPaths == expectedPaths) {
      "Expected all of requested system images to be present. Missing:" +
        " ${expectedPaths - encounteredPaths}."
    }

    val packagesToUpdate = matchedPackages.filterNot { (key, osSplitImages) ->
      val currentImages = currentSystemImages[key]
      val localOsSplitImages = currentImages?.keys
      val remoteOsSplitImages = osSplitImages.keys
      return@filterNot localOsSplitImages == remoteOsSplitImages && remoteOsSplitImages.all {
        currentImages.getValue(it).sha1Checksum == osSplitImages[it]?.archive?.sha1Checksum
      }
    }
    println(
      "Updating ${packagesToUpdate.size}/${matchedPackages.size} image(s) locally (out of" +
        " ${packages.size} total available)..."
    )

    // Start resolving packages and recomputing the destination list (note that the list is
    // regenerated after each download so that the script can fail mid-download without needing to
    // restart everything).
    val changingImagesMap = currentSystemImages.toMutableMap().also {
      // Make sure any obsolete system images are removed from the original list.
      it.keys.retainAll(matchedPackages.keys)
    }
    packagesToUpdate.forEach { (key, osSplitImages) ->
      println("Rewriting image list for: '${key.path}'...")
      changingImagesMap[key] = osSplitImages.mapValues { (_, systemImage) ->
        systemImage.makeSerializable()
      }
      serializeSystemImages(
        requestedSystemImages, changingImagesMap.toSortedMap(SystemImageKey.COMPARATOR)
      )
    }

    // Perform one final regeneration so that re-running the script can also update formatting
    // changes.
    serializeSystemImages(
      requestedSystemImages, changingImagesMap.toSortedMap(SystemImageKey.COMPARATOR)
    )

    println("Finished!")
  }

  private fun retrieveCurrentSystemImages(
    availableSystemImages: AvailableSystemImages
  ): Map<SystemImageKey, Map<HostOs, SerializableSystemImage>> {
    return availableSystemImages.availableSystemImageMap.map { (path, osSplitImages) ->
      SystemImageKey.computeKey(path) to osSplitImages.osSplitImagesList.associate { osSplitImage ->
        when (osSplitImage.osTypeCase) {
          OsSplitSystemImage.OsTypeCase.LINUX -> HostOs.LINUX to osSplitImage.linux.asSerializable()
          OsSplitSystemImage.OsTypeCase.MAC_OSX ->
            HostOs.MAC_OSX to osSplitImage.macOsx.asSerializable()
          OsSplitSystemImage.OsTypeCase.WINDOWS ->
            HostOs.WINDOWS to osSplitImage.windows.asSerializable()
          OsSplitSystemImage.OsTypeCase.OSTYPE_NOT_SET, null ->
            error("Unsupported split OS image type case.")
        }
      }
    }.toMap()
  }

  private fun serializeSystemImages(
    requestedSystemImageList: List<String>,
    systemImages: Map<SystemImageKey, Map<HostOs, SerializableSystemImage>>
  ) {
    PrintStream(systemImageListFile.outputStream()).use { stream ->
      stream.println(
        """
          ""${'"'}
          Defines metadata corresponding to Android system images that should be made available for Espresso
          and end-to-end tests.

          NOTE TO DEVELOPER: This file is automatically generated. The only field that should be changed is
          REQUESTED_SYSTEM_IMAGE_LIST which specifies which images should be supported. If that list is
          changed, 'bazel run //scripts:system_image_list_generator' must be run to regenerate
          SYSTEM_IMAGES_LIST.
          ""${'"'}
        """.trimIndent()
      )
      stream.println()

      stream.println("REQUESTED_SYSTEM_IMAGE_LIST = [")
      requestedSystemImageList.map {
        SystemImageKey.computeKey(it)
      }.sortedWith(SystemImageKey.COMPARATOR).forEach { key ->
        stream.println("    \"${key.path}\",")
      }
      stream.println("]")
      stream.println()

      stream.println("SYSTEM_IMAGES_LIST = {")
      systemImages.forEach { (key, osSystemImages) ->
        stream.println("    \"${key.path}\": {")
        osSystemImages.forEach { (hostOs, systemImage) ->
          stream.println("        \"${hostOs.readableName}\": {")
          stream.println("            \"url\": \"${systemImage.qualifiedUrl}\",")
          stream.println("            \"sha1\": \"${systemImage.sha1Checksum}\",")
          stream.println("            \"sha256\": \"${systemImage.sha256Checksum}\",")
          stream.println("        },")
        }
        stream.println("    },")
      }
      stream.println("}")
    }
  }

  private fun downloadSysImageUrls(): Sequence<String> {
    // Reference:
    // https://github.com/eagletmt/android-repository-history/blob/master/repository/addons_list-3.xml.
    val systemImageRepoListUrl = "$REPOSITORY_BASE_URL/addons_list-3.xml"
    val document = documentBuilderFactory.newDocumentBuilder().parse(systemImageRepoListUrl)
    val listRoot = document.findOnlyChildWithDetails(tagName = "common:site-list")
    val sysImageSites =
      listRoot.findAllChildrenWithDetails(
        tagName = "site", attributes = mapOf("xsi:type" to "sdk:sysImgSiteType")
      )
    return sysImageSites.map {
      it.findOnlyChildWithDetails(tagName = "url").textContent
    }.map { "$REPOSITORY_BASE_URL/$it" }
  }

  private fun downloadSystemImagePackageList(
    indexUrl: String
  ): Sequence<Pair<SystemImageKey, SystemImageRemotePackage>> {
    // Reference:
    // https://github.com/eagletmt/android-repository-history/blob/master/repository/sys-img/android/sys-img2-1.xml.
    val document = documentBuilderFactory.newDocumentBuilder().parse(indexUrl)
    val listRoot = document.findOnlyChildWithDetails(tagName = "sys-img:sdk-sys-img")
    return listRoot.findAllChildrenWithDetails(tagName = "remotePackage").groupBy {
      it.attributes.asMap().getValue("path")
    }.asSequence().flatMap { (_, nodes) ->
      nodes.convertToRemotePackage(indexUrl)
    }
  }

  private fun List<Node>.convertToRemotePackage(
    indexUrl: String
  ): Sequence<Pair<SystemImageKey, SystemImageRemotePackage>> {
    val retrievedArchives = flatMap { remotePackageRoot ->
      // Pull the path to verify pulled attributes later on as a sanity check. Skip specific cases
      // that are inconsistent when computing the path (and won't be needed by the app).
      val path = remotePackageRoot.attributes.asMap().getValue("path")
      if ("android-wear-cn" in path || "android-Tiramisu" in path) return sequenceOf()

      // Skip system images with unsupported licenses.
      val licenseRef = remotePackageRoot.findOnlyChildWithDetails(tagName = "uses-license")
      if (licenseRef.attributes.asMap().getValue("ref") !in ACCEPTED_LICENSES) return sequenceOf()

      val typeDetails = remotePackageRoot.findOnlyChildWithDetails(tagName = "type-details")
      val apiLevel = typeDetails.findOnlyChildWithDetails(tagName = "api-level").textContent.toInt()
      val tagElement = typeDetails.findOnlyChildWithDetails(tagName = "tag")
      val tagName = tagElement.findOnlyChildWithDetails(tagName = "id").textContent
      val abi = Abi.createFromReadableName(typeDetails.findOnlyChildWithDetails("abi").textContent)

      val archivesRoot = remotePackageRoot.findOnlyChildWithDetails(tagName = "archives")
      val archives = archivesRoot.findAllChildrenWithDetails(tagName = "archive")

      val revisionIndex =
        remotePackageRoot.findOnlyChildWithDetails(tagName = "revision")
          .findOnlyChildWithDetails(tagName = "major")
          .textContent
          .toInt()

      val key = SystemImageKey(apiLevel, tagName, abi)
      val expectedPath = key.path
      check(path == expectedPath) {
        "Encountered path '$path' but expected: '$expectedPath' for repository: $indexUrl."
      }

      archives.map {
        val archive = it.convertToArchive(indexUrl)
        key to SystemImageRemotePackage(path, apiLevel, tagName, abi, revisionIndex, archive)
      }
    }

    return sequence {
      val instantiatedArchives = retrievedArchives.toList()
      val perOsSplits = setOf(HostOs.LINUX, HostOs.MAC_OSX, HostOs.WINDOWS)
      when (val oses = instantiatedArchives.map { it.second.archive.hostOs }.toSet()) {
        setOf(HostOs.ANY) -> {
          // There may be multiple packages; pick the most recent.
          val mostRecentPackage = instantiatedArchives.sortedBy { it.second.majorRevision }.first()
          perOsSplits.forEach { newHostOs ->
            yield(
              mostRecentPackage.first to mostRecentPackage.second.copy(
                archive = mostRecentPackage.second.archive.copy(hostOs = newHostOs)
              )
            )
          }
        }
        perOsSplits -> yieldAll(instantiatedArchives)
        else -> {
          val key = instantiatedArchives.map { it.first }.distinct().single()
          error("Unsupported combination of OSes for archive '${key.path}': $oses.")
        }
      }
    }
  }

  private fun Node.convertToArchive(indexUrl: String): Archive {
    val completion = findOnlyChildWithDetails(tagName = "complete")
    val size = completion.findOnlyChildWithDetails(tagName = "size").textContent.toLong()
    val checksum = completion.findOnlyChildWithDetails(tagName = "checksum").textContent
    val url = completion.findOnlyChildWithDetails(tagName = "url").textContent
    val hostOs =
      HostOs.createFromRepoReadableName(maybeFindOnlyChildWithDetails(tagName = "host-os")?.textContent)

    return Archive(
      fileName = url,
      qualifiedUrl = URL("${indexUrl.substringBeforeLast(delimiter = '/')}/$url").toExternalForm(),
      size = size,
      sha1Checksum = checksum,
      hostOs = hostOs
    )
  }

  private fun AvailableSystemImage.asSerializable() = SerializableSystemImage(url, sha1, sha256)

  private fun SystemImageRemotePackage.makeSerializable(): SerializableSystemImage {
    // Short-circuit the SHA-256 "computation" by retrieving it from an in-memory cache (for cases
    // when the same archive is used for multiple OS flavors).
    val sha256Hash = if (!cachedSha1ToSha256Map.containsKey(archive.sha1Checksum)) {
      val qualifiedUrl = URL(archive.qualifiedUrl)
      var readBytes = 0L
      sha1Digest.reset()
      sha256Digest.reset()
      qualifiedUrl.openStream().use {
        var read = it.read(buffer)
        while (read >= 0) {
          readBytes += read
          sha1Digest.update(buffer, /* ofs= */ 0, read)
          sha256Digest.update(buffer, /* ofs= */ 0, read)
          read = it.read(buffer)
        }
      }

      val sha1Hash = sha1Digest.digest().toHexString()
      val sha256Hash = sha256Digest.digest().toHexString()
      check(!cachedSha1ToSha256Map.containsKey(sha1Hash)) {
        "SHA-1 is unexpectedly already present in cache: $sha1Hash."
      }
      cachedSha1ToSha256Map[sha1Hash] = sha256Hash

      // Ensure the integrity of the archive so that there can be confidence in the SHA-256 checksum
      // passed to Bazel that it will use when downloading & caching the archive.
      check(archive.size == readBytes) {
        "Expected to read ${archive.size} bytes, but read: $readBytes from remote archive."
      }
      check(sha1Hash == archive.sha1Checksum) {
        "Expected remote file to have SHA-1 hash: ${archive.sha1Checksum}, but observed: $sha1Hash."
      }

      sha256Hash
    } else cachedSha1ToSha256Map.getValue(archive.sha1Checksum)

    return SerializableSystemImage(archive.qualifiedUrl, archive.sha1Checksum, sha256Hash)
  }

  private data class SystemImageRemotePackage(
    val path: String,
    val apiLevel: Int,
    val tagName: String,
    val abi: Abi,
    val majorRevision: Int,
    val archive: Archive
  )

  private data class Archive(
    val fileName: String,
    val qualifiedUrl: String,
    val size: Long,
    val sha1Checksum: String,
    val hostOs: HostOs
  )

  private data class SystemImageKey(val apiLevel: Int, val tagName: String, val abi: Abi) {
    val path: String
      get() = "system-images;android-$apiLevel;$tagName;${abi.readableName}"

    companion object {
      private val PATH_PATTERN = "system-images;android-(\\d+?);(.+?);(.+)".toRegex()

      val COMPARATOR by lazy {
        compareByDescending(SystemImageKey::apiLevel)
          .thenBy(SystemImageKey::tagName)
          .thenBy(SystemImageKey::abi)
      }

      fun computeKey(path: String): SystemImageKey {
        val (apiLevel, tagName, abi) = checkNotNull(PATH_PATTERN.matchEntire(path)?.destructured) {
          "Failed to match pattern against: $path."
        }
        return SystemImageKey(apiLevel.toInt(), tagName, Abi.createFromReadableName(abi))
      }
    }
  }

  private data class SerializableSystemImage(
    val qualifiedUrl: String, val sha1Checksum: String, val sha256Checksum: String
  )

  private enum class Abi(val readableName: String) {
    X86("x86"),
    X86_64("x86_64"),
    ARMEABI_V7A("armeabi-v7a"),
    ARM64_V8A("arm64-v8a");

    companion object {
      fun createFromReadableName(readableName: String): Abi {
        return when (readableName) {
          "armeabi-v7a" -> ARMEABI_V7A
          "arm64-v8a" -> ARM64_V8A
          "x86" -> X86
          "x86_64" -> X86_64
          else -> throw IllegalStateException("Unsupported ABI version: $readableName.")
        }
      }
    }
  }

  private enum class HostOs(val readableName: String) {
    ANY("any_os"),
    LINUX("linux"),
    MAC_OSX("mac_osx"),
    WINDOWS("windows");

    companion object {
      fun createFromRepoReadableName(readableName: String?): HostOs {
        return when (readableName) {
          null -> ANY
          "linux" -> LINUX
          "macosx" -> MAC_OSX
          "windows" -> WINDOWS
          else -> throw IllegalStateException("Unsupported OS version: $readableName.")
        }
      }
    }
  }

  private companion object {
    private val ACCEPTED_LICENSES = listOf("android-sdk-license", "android-sdk-preview-license")

    private fun Node.childSequence() = childNodes.asSequence()

    private fun NodeList.asSequence() = (0 until length).asSequence().map(this::item)

    private fun NamedNodeMap.asMap() =
      (0 until length).asSequence().map(this::item).map { it.nodeName to it.textContent }.toMap()

    private fun Node.findAllChildrenWithDetails(
      tagName: String, attributes: Map<String, String> = mapOf()
    ): Sequence<Node> {
      return childSequence().filter { it.nodeName == tagName && it.hasAttributes(attributes) }
    }

    private fun Node.findOnlyChildWithDetails(
      tagName: String, attributes: Map<String, String> = mapOf()
    ) = findAllChildrenWithDetails(tagName, attributes).single()

    private fun Node.maybeFindOnlyChildWithDetails(
      tagName: String, attributes: Map<String, String> = mapOf()
    ) = findAllChildrenWithDetails(tagName, attributes).singleOrNull()

    private fun Node.hasAttributes(attributes: Map<String, String>) =
      this.attributes.asMap().containsAllOf(attributes)

    private fun <K, V> Map<K, V>.containsAllOf(otherMap: Map<K, V>) =
      otherMap.all { (key, value) -> this@containsAllOf[key] == value }

    private fun ByteArray.toHexString() =
      fold(initial = "") { acc, byte -> acc + byte.toHexString() }

    private fun Byte.toHexString() =
      (toInt() and 0xff).toString(radix = 16).let { if (it.length == 1) "0$it" else it }
  }
}
