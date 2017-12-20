package com.acme.weather.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.acme.weather.model.repository.geolocation.WeatherLocationService
import com.acme.weather.model.repository.WeatherRepository
import org.jetbrains.anko.doAsync
import timber.log.Timber
import javax.inject.Inject

sealed class State
class DEFAULT : State()
class LOCATION_ADD_PENDING : State()
class LOCATION_ADD_FAILED(val error: String) : State()

class WeatherListViewModel @Inject constructor(
        val weatherRepository: WeatherRepository,
        val weatherLocationService: WeatherLocationService) : ViewModel() {

    val weather = weatherRepository.weatherList
    val state = MutableLiveData<State>().apply{ value = DEFAULT() }

    fun onLocationEntered(zip: String) {
        Timber.i("onLocationEntered: ${zip}")
        state.value = LOCATION_ADD_PENDING()
        doAsync {
            val location = weatherLocationService.locationForZip(zip)
            if(location != null) {
                weatherRepository.addWeatherLocation(location = location)
                state.postValue(DEFAULT()) // post, automatically emits to UI thread.
            } else {
                state.postValue(LOCATION_ADD_FAILED("Location not found"))
                state.postValue(DEFAULT())
            }
        }
    }

    fun onLocationDeleted(id: Long) {
        weatherRepository.removeWeatherLocation(id)
    }

}

