package io.objectbox.example.kotlin.ui.Page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var item_id: Long = -1
    val title_share: MutableLiveData<String> = MutableLiveData()
    val description_share: MutableLiveData<String> = MutableLiveData()
}