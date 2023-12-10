package io.objectbox.example.kotlin.ui.Page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var item_id: MutableLiveData<Long> = MutableLiveData()
    var type_share: MutableLiveData<String> = MutableLiveData()
    var legal_share: MutableLiveData<String> = MutableLiveData()
    val title_share: MutableLiveData<String> = MutableLiveData()
    val description_share: MutableLiveData<String> = MutableLiveData()
}