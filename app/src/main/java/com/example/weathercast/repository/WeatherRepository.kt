package com.example.weathercast.repository

import com.example.weathercast.api.WeatherApiService
import com.example.weathercast.model.WeatherResponse
import retrofit2.Response

class WeatherRepository(private val apiService: WeatherApiService) {

    suspend fun getWeatherData(cityName: String, apiKey: String): Response<WeatherResponse> {
        return apiService.getWeatherData(cityName, "metric", apiKey)
    }
}