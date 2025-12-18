package com.example.agrofarm.model

import com.google.gson.annotations.SerializedName

// Main container for the entire API response
data class WeatherResponse(
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("main") val main: Main,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("name") val name: String
)

// Contains the main weather description and icon
data class Weather(
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

// Contains the core weather data like temperature and humidity
data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("temp_min") val temp_min: Double,
    @SerializedName("temp_max") val temp_max: Double,
    @SerializedName("humidity") val humidity: Int
)

// Contains wind information
data class Wind(
    @SerializedName("speed") val speed: Double
)
