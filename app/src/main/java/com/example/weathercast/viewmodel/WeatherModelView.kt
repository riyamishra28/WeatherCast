package com.example.weathercast.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathercast.api.WeatherApiService
import com.example.weathercast.model.WeatherResponse
import com.example.weathercast.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val weatherRepository = WeatherRepository(WeatherApiService.create())

    private val apiKey = "0a5357f17c6334382676f6f85fa1c76f"

    // LiveData for weather data
    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> get() = _errorMessage

    fun getWeatherForCity(cityName: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = weatherRepository.getWeatherData(cityName, apiKey)
                if (response.isSuccessful) {
                    _weatherData.value = response.body()
                } else {
                    when (response.code()) {
                        404 -> _errorMessage.value = "City not found. Please try again."
                        401 -> _errorMessage.value = "Invalid API key. Please check your API key."
                        else -> _errorMessage.value = "Error: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}