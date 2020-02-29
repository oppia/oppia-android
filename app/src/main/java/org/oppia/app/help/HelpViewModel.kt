package org.oppia.app.help

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HelpViewModel : ViewModel{
  var title = ""
  constructor() : super()
  constructor(category: String) : super() {
    this.title = category
  }

  var arrayListMutableLiveData = MutableLiveData<ArrayList<HelpViewModel>>()
  var arrayList = ArrayList<HelpViewModel>()

  fun getArrayList():MutableLiveData<ArrayList<HelpViewModel>>{
    val category1 = "Frequently Asked Question (FAQs)"
    val helpViewModel1: HelpViewModel = HelpViewModel(category1)
    arrayList!!.add(helpViewModel1)
    arrayListMutableLiveData.value = arrayList
    return arrayListMutableLiveData
  }

}
