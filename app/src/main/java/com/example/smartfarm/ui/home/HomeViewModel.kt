package com.example.smartfarm.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    // save spinner selection index
    private val _selectedCoop = MutableLiveData<Int>().apply {
        value = 0 // default Kandang 1
    }
    val selectedCoop: LiveData<Int> = _selectedCoop

    fun setSelectedCoop(position: Int) {
        _selectedCoop.value = position
    }
}
