package org.oppia.app.drawer.model

class DrawerModel {

  var name: String? = null
  var image: Int = 0

  fun getImages(): Int {
    return image
  }

  fun setImages(image: Int) {
    this.image = image
  }

  fun getNames(): String? {
    return name
  }

  fun setNames(name: String) {
    this.name = name
  }

}