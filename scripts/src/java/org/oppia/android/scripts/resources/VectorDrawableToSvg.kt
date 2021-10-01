package org.oppia.android.scripts.resources

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.lang.IllegalStateException
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.absoluteValue
import kotlin.random.Random
import org.oppia.android.scripts.common.RepositoryFile
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList

fun main(vararg args: String) {
  val rootDirectory = File(args[0])

  VectorDrawableToSvg(rootDirectory).convertAllVectorDrawablesToSvgs()
}

private class VectorDrawableToSvg(private val repoRoot: File) {
  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }

  fun convertAllVectorDrawablesToSvgs() {
    val searchFiles = RepositoryFile.collectSearchFiles(
      repoRoot = repoRoot,
      expectedExtension = ".xml"
    )

    val containers = searchFiles.mapNotNull { file ->
      convertVectorDrawableToSvg(file)
    }.toList()

    val failed = containers.count { it.viewportWidth == -1 }
    println("$failed/${containers.size} failed")

    containers.forEach { container ->
      PrintStream(ByteArrayOutputStream()).use { container.generateSvg(it) }
    }
  }

  private fun convertVectorDrawableToSvg(drawableFile: File): VectorContainer? {
    val docBuilder = documentBuilderFactory.newDocumentBuilder()
    val document = docBuilder.parse(drawableFile)
    return document.parseVectorContainer(drawableFile)
  }

  private fun Document.parseVectorContainer(file: File): VectorContainer? {
    val documentChildren = getElementChildren()
    check(documentChildren.size == 1) { "Expected a single root element" }

    val rootNode = documentChildren.first()
    if (rootNode.nodeName != "vector") return null

    // TODO: consider adding print output to provide context on which file is being parsed, e.g.:
    // println("Parsing drawable: ${file.toRelativeString(repoRoot)}")

    // TODO: remove try-catch once all drawables are supported
    try {
      val attributes = rootNode.getAttributeMap()
      attributes.validateAttributeKeys(ACCEPTABLE_VECTOR_ATTRIBUTES)

      val paths = rootNode.getElementChildren().map { childNode ->
        when (childNode.nodeName) {
          "path" -> childNode.parsePath()
          else -> error("Encountered unsupported node: ${childNode.nodeName}")
        }
      }

      val viewportWidth = attributes.getExpectedInt("android:viewportWidth")
      val viewportHeight = attributes.getExpectedInt("android:viewportHeight")

      return VectorContainer(paths, viewportWidth, viewportHeight)
    } catch (e: IllegalStateException) {
      return VectorContainer(paths = listOf(), viewportWidth = -1, viewportHeight = -1)
    }
  }

  private fun Node.parsePath(): Path {
    val attributes = getAttributeMap()
    attributes.validateAttributeKeys(ACCEPTABLE_PATH_ATTRIBUTES)

    val pathData = attributes.getExpectedValue("android:pathData")
    val hasFillColorAttribute = "android:fillColor" in attributes

    val children = getElementChildren()
    val fillColor: FillColor = when {
      children.isNotEmpty() -> {
        check(!hasFillColorAttribute) { "Cannot define both android:fillColor and gradient child" }
        children.expectSingleChildOfType("aapt:attr").parseAaptAttrGradient()
      }
      hasFillColorAttribute ->
        FillColor.Solid(attributes.getExpectedColor("android:fillColor"))
      else -> error("Expected fill color to be specified for path")
    }

    return Path(pathData = pathData, fillColor = fillColor)
  }

  private fun Node.parseAaptAttrGradient(): FillColor.LinearGradient {
    val attributes = getAttributeMap()
    attributes.validateAttributeKeys(ACCEPTABLE_AAPT_ATTR_ATTRIBUTES)

    check(attributes["name"] == "android:fillColor") {
      "Expected aapt:attr tag to have 'android:fillColor' name"
    }

    return getElementChildren().expectSingleChildOfType("gradient").parseGradient()
  }

  private fun Node.parseGradient(): FillColor.LinearGradient {
    val attributes = getAttributeMap()
    attributes.validateAttributeKeys(ACCEPTABLE_GRADIENT_ATTRIBUTES)

    val children = getElementChildren()
    check(children.isNotEmpty()) { "Expected gradient items" }

    val items = children.map { childNode ->
      when (childNode.nodeName) {
        "item" -> childNode.parseGradientItem()
        else -> error("Unsupported node type: ${childNode.nodeName}")
      }
    }

    val startX = attributes.getExpectedDouble("android:startX")
    val startY = attributes.getExpectedDouble("android:startY")
    val endX = attributes.getExpectedDouble("android:endX")
    val endY = attributes.getExpectedDouble("android:endY")
    val type = attributes.getExpectedValue("android:type")
    check(type == "linear") { "Only linear gradients are supported, not: $type" }

    return FillColor.LinearGradient(startX, startY, endX, endY, items)
  }

  private fun Node.parseGradientItem(): FillColor.LinearGradient.GradientItem {
    val attributes = getAttributeMap()
    attributes.validateAttributeKeys(ACCEPTABLE_GRADIENT_ITEM_ATTRIBUTES)

    val offset = attributes.getExpectedValue("android:offset")
    val color = attributes.getExpectedColor("android:color")

    return FillColor.LinearGradient.GradientItem(offset, color)
  }

  private fun Node.getElementChildren(): List<Node> =
    childNodes.toListOfNodes().filter { it.nodeType == Node.ELEMENT_NODE }

  private fun Node.getAttributeMap(): Map<String, String> {
    return attributes.run {
      List(length) { index -> item(index) }.map { it.toAttributeValues() }.toMap()
    }
  }

  private fun Map<String, String>.getExpectedValue(key: String): String =
    this[key] ?: error("Expected attribute: 'key'")

  private fun Map<String, String>.getExpectedColor(key: String): Color =
    getExpectedValue(key).parseHexColor()

  private fun Map<String, String>.getExpectedInt(key: String): Int {
    return getExpectedValue(key).toIntOrNull()
      ?: error("Expected value to be an integer: ${getExpectedValue(key)}")
  }

  private fun Map<String, String>.getExpectedDouble(key: String): Double {
    return getExpectedValue(key).toDoubleOrNull()
      ?: error("Expected value to be a double: ${getExpectedValue(key)}")
  }

  private fun Map<String, String>.validateAttributeKeys(expectedKeys: Set<String>) {
    check(keys.all { it in expectedKeys }) {
      "Encountered unsupported attributes: ${keys - expectedKeys} (expected: $expectedKeys)"
    }
  }

  private fun List<Node>.expectSingleChildOfType(name: String): Node {
    check(size == 1) { "Expected a single child for node" }
    val child = first()
    check(child.nodeName == name) { "Expected child of type '$name', not: '${child.nodeName}'" }
    return child
  }

  private fun NodeList.toListOfNodes(): List<Node> = List(length) { index -> item(index) }

  private fun Node.toAttributeValues(): Pair<String, String> {
    check(nodeType == Node.ATTRIBUTE_NODE) {
      "Expected node '$nodeName' to be attribute, not: $nodeType"
    }
    return nodeName to checkNotNull(nodeValue) { "Expected value for node '$nodeName' attribute" }
  }

  private fun String.parseHexColor(): Color {
    check(isNotEmpty()) { "Expected non-empty hex color, encountered: $this" }
    check(first() == '#') { "Expected hex color to start with '#', encountered: $this" }

    val digits = substring(1)
    check(digits.all { it.isHexCharacter() }) {
      "Expected all characters in color to be hexadecimal, encountered: $this"
    }
    return when (digits.length) {
      3 -> {
        // 3-digit hex colors are shortened versions of 6-color ones, e.g.: ABC becomes AABBCC.
        val r = digits[0]
        val g = digits[1]
        val b = digits[2]
        Color.fromRgb("$r$r$g$g$b$b".parseHexInt())
      }
      6 -> Color.fromRgb(digits.parseHexInt())
      8 -> Color.fromArgb(digits.parseHexInt())
      else -> error("Expected 3, 6, or 8 digit hex color, encountered: $this")
    }
  }

  private fun String.parseHexInt(): Long = toLong(radix = 16)

  private fun Char.isHexCharacter(): Boolean = toLowerCase() in ACCEPTABLE_HEX_CHARACTERS

  private data class VectorContainer(
    val paths: List<Path>, val viewportWidth: Int, val viewportHeight: Int
  ) {
    fun generateSvg(stream: PrintStream) {
      stream.println(
        "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 $viewportWidth $viewportHeight\">"
      )
      stream.println(indents = 1, "<defs>")
      stream.println(indents = 2, "<style>")
      paths.forEach { it.generateSvgStyles(indents = 3, stream) }
      stream.println(indents = 2, "</style>")
      paths.forEach { it.generateSvgDefs(indents = 2, stream) }
      stream.println(indents = 1, "</defs>")
      paths.forEach { it.generateSvgPath(indents = 1, stream) }
      stream.println("</svg>")
    }
  }

  private data class Path(val pathData: String, val fillColor: FillColor) {

    fun generateSvgStyles(indents: Int, stream: PrintStream) {
      fillColor.generateSvgStyle(indents, stream)
    }

    fun generateSvgDefs(indents: Int, stream: PrintStream) {
      fillColor.generateSvgDefs(indents, stream)
    }

    fun generateSvgPath(indents: Int, stream: PrintStream) {
      stream.println(indents, "<path d=\"$pathData\" class=\"${fillColor.fillColorClassId}\"/>")
    }
  }

  private sealed class FillColor {
    abstract val fillColorClassId: String

    abstract fun generateSvgStyle(indents: Int, stream: PrintStream)

    abstract fun generateSvgDefs(indents: Int, stream: PrintStream)

    data class Solid(val color: Color): FillColor() {
      override val fillColorClassId by lazy { generateIdString() }

      override fun generateSvgStyle(indents: Int, stream: PrintStream) {
        stream.println(indents, ".$fillColorClassId {")
        stream.println(indents + 1, "fill: ${toSvgString()};")
        stream.println(indents, "}")
      }

      override fun generateSvgDefs(indents: Int, stream: PrintStream) {
        // Nothing to generate for solid colors.
      }

      private fun toSvgString(): String = "#${color.toRgbHexString().toUpperCase(Locale.US)}"
    }

    // TODO: figure out correct space to represent the gradient in so that it looks correct (Android
    //  seems to use a different basis than SVG).
    data class LinearGradient(
      val startX: Double,
      val startY: Double,
      val endX: Double,
      val endY: Double,
      val items: List<GradientItem>
    ): FillColor() {
      override val fillColorClassId by lazy { generateIdString() }
      private val gradientId by lazy { generateIdString() }

      override fun generateSvgStyle(indents: Int, stream: PrintStream) {
        stream.println(indents, ".$fillColorClassId {")
        stream.println(indents + 1, "fill: url(#$gradientId);")
        stream.println(indents, "}")
      }

      override fun generateSvgDefs(indents: Int, stream: PrintStream) {
        stream.println(
          indents,
          "<linearGradient id=\"$gradientId\" x1=\"$startX\" y1=\"$startY\" x2=\"$endX\"" +
            " y2=\"$endY\">"
        )
        items.forEach { it.generateStopSvg(indents + 1, stream) }
        stream.println(indents, "</linearGradient>")
      }

      data class GradientItem(val offset: String, val color: Color) {
        fun generateStopSvg(indents: Int, stream: PrintStream) {
          stream.print(
            indents, "<stop offset=\"$offset\" stop-color=\"#${color.toRgbHexString()}\""
          )
          if (!color.isFullyOpaque()) {
            // Ensure the color's transparency channel is included.
            stream.print(" stop-opacity=\"${color.a.toFloat() / 255.0f}\"")
          }
          stream.println("/>")
        }
      }
    }
  }

  private data class Color(val a: Int, val r: Int, val g: Int, val b: Int) {
    fun toRgbHexString(): String = "${r.toHexString()}${g.toHexString()}${b.toHexString()}"

    fun isFullyOpaque(): Boolean = a == 0xff

    companion object {
      fun fromRgb(rgb: Long): Color = fromArgb(rgb).copy(a = 255)

      fun fromArgb(argb: Long): Color = Color(
        a = argb.extractAlphaComponent(),
        r = argb.extractRedComponent(),
        g = argb.extractGreenComponent(),
        b = argb.extractBlueComponent()
      )

      private fun Long.extractAlphaComponent() = ((this ushr 24) and 0xff).toInt()
      private fun Long.extractRedComponent() = ((this ushr 16) and 0xff).toInt()
      private fun Long.extractGreenComponent() = ((this ushr 8) and 0xff).toInt()
      private fun Long.extractBlueComponent() = (this and 0xff).toInt()

      private fun Int.toHexString() = toString(radix = 16).padStart(length = 2, '0')
    }
  }

  private companion object {
    private val ACCEPTABLE_HEX_CHARACTERS =
      listOf('a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    private val ACCEPTABLE_VECTOR_ATTRIBUTES = setOf(
      "xmlns:android", "xmlns:aapt", "android:width", "android:height", "android:autoMirrored",
      "android:viewportWidth", "android:viewportHeight"
    )

    private val ACCEPTABLE_PATH_ATTRIBUTES = setOf("android:pathData", "android:fillColor")

    private val ACCEPTABLE_AAPT_ATTR_ATTRIBUTES = setOf("name")

    private val ACCEPTABLE_GRADIENT_ATTRIBUTES = setOf(
      "android:startX", "android:startY", "android:endX", "android:endY", "android:type"
    )

    private val ACCEPTABLE_GRADIENT_ITEM_ATTRIBUTES = setOf("android:offset", "android:color")
  }
}

private fun PrintStream.print(indents: Int, str: String) {
  print("${indents.toIndentString()}$str")
}

private fun PrintStream.println(indents: Int, str: String) {
  println("${indents.toIndentString()}$str")
}

private fun Int.toIndentString(): String = "  ".repeat(this)

private fun generateIdString() = "id-${Random.nextInt().toLong().absoluteValue}"
