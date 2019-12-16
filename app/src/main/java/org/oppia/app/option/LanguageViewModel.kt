package org.oppia.app.option

/** [ViewModel] for language selection. */
class LanguageViewModel(val language: CharSequence) {

  val listOfLanguages = getLanguageList()//Creating an empty arraylist

  fun getLanguageList() : ArrayList<String>{
    val dummyList = ArrayList<String>()//Creating an empty arraylist
    dummyList.add("English")//Adding object in arraylist
    dummyList.add("French")
    dummyList.add("Hindi")
    dummyList.add("Chinese")
    return  dummyList

  }
}
