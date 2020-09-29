package com.bdn.exosample.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Splash View Model used for managing life cycle states
 */
class SplashActivityViewModel(
    val application: Application
) : ViewModel() {

    companion object {
        var TAG: String = SplashActivityViewModel::class.java.canonicalName ?: ""
    }

    val splashStateData: LiveData<SplashState>
        get() = splashState

    private val splashState = MutableLiveData<SplashState>()

    init {



    }

    fun timer(){
        viewModelScope.launch(Dispatchers.Main) {
            delay(3000)
            splashState.value = SplashState.MainActivity
        }
    }
}

/**
 * Splash screen state
 */
sealed class SplashState {
    object MainActivity : SplashState()
}
