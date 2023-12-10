package io.objectbox.example.kotlin.ui.datamanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataManagerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is data Fragment"
    }
    val text: LiveData<String> = _text
}