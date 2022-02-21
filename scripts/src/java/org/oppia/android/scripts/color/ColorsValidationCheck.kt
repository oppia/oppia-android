package org.oppia.android.scripts.color

import org.oppia.android.scripts.common.RepositoryFile

fun main(vararg args: String) {
  val componentColorsFile = RepositoryFile.collectSearchFiles(args[0])[0]
  val colorPaletteFile = RepositoryFile.collectSearchFiles(args[1])[0]
  val colorDefsFile = RepositoryFile.collectSearchFiles(args[2])[0]

  // Work in progress...

  val colorReferencedRegexPattern = Regex(pattern=">@color/(.+)<\\/color>")
  val colorNameRegexPattern = Regex(pattern="<color name=.(.+).>@")

  componentColorsFile.forEachLine{
    val _colorname = colorReferencedRegexPattern.find(it)?.groupValues?.get(1)
    if(_colorname != null){
      println(_colorname)
    }
  }
  println("This Worked! -- Builds OK.")
}
