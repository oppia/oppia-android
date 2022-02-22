package org.oppia.android.scripts.color

import org.oppia.android.scripts.common.RepositoryFile

/**
 * Script for ensuring that color declarations in component_colors.xml and
 * color_palette.xml are referencing to correct files.
 *
 * Usage:
 *   bazel run //scripts:colors_validation_check -- <path_to_component_colors> <path_to_color_palette> <path_to_color_defs>
 */
fun main(vararg args: String) {
  val componentColorsFile = RepositoryFile.collectSearchFiles(args[0])[0]
  val colorPaletteFile = RepositoryFile.collectSearchFiles(args[1])[0]
  val colorDefsFile = RepositoryFile.collectSearchFiles(args[2])[0]

  val colorReferencedRegexPattern = Regex(pattern = ">@color/(.+)<\\/color>")
  val colorNameRegexPattern = Regex(pattern = "<color name=.(.+).>[@#]")

  val colorsUsedInComponentColors = mutableSetOf<String>()
  val colorsInColorPalette = mutableSetOf<String>()
  val colorsUsedInColorPalette = mutableSetOf<String>()
  val colorsInColorDefs = mutableSetOf<String>()

  componentColorsFile.forEachLine {
    val _colorname = colorReferencedRegexPattern.find(it)?.groupValues?.get(1)
    if (_colorname != null) {
      colorsUsedInComponentColors.add(_colorname)
    }
  }

  colorPaletteFile.forEachLine {
    val _declarationName = colorNameRegexPattern.find(it)?.groupValues?.get(1)
    val _colorUsed = colorReferencedRegexPattern.find(it)?.groupValues?.get(1)
    if (_declarationName != null) {
      colorsInColorPalette.add(_declarationName)
    }
    if (_colorUsed != null) {
      colorsUsedInColorPalette.add(_colorUsed)
    }
  }

  colorDefsFile.forEachLine {
    val _colorname = colorNameRegexPattern.find(it)?.groupValues?.get(1)
    if (_colorname != null) {
      colorsInColorDefs.add(_colorname)
    }
  }

  var componentColorsStatus = true;
  var colorPaletteStatus = true;

  colorsUsedInComponentColors.forEach {
    if (colorsInColorPalette.contains(it) == false) {
      componentColorsStatus = false;
    }
  }

  colorsUsedInColorPalette.forEach {
    if (colorsInColorDefs.contains(it) == false) {
      colorPaletteStatus = false;
    }
  }

  if (componentColorsStatus == false || colorPaletteStatus == false) {
    throw Exception("COLOR FILES VALIDATION FAILED")
  } else {
    println("COLOR FILES VALIDATION PASSED")
  }

}
