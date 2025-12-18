package com.example.agrofarm.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agrofarm.model.WeatherResponse
import com.example.agrofarm.ui.theme.AgroFarmTheme
import com.example.agrofarm.viewmodel.WeatherViewModel


class WeatherActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fetch weather for a default location (e.g., London)
//        weatherViewModel.fetchWeatherData(51.5072, -0.1276)
        // Fetch weather for Kathmandu, Nepal
        weatherViewModel.fetchWeatherData(27.7172, 85.3240)

        setContent {
            AgroFarmTheme {
                WeatherApp(weatherViewModel = weatherViewModel, onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp(weatherViewModel: WeatherViewModel, onNavigateBack: () -> Unit) {
    val weatherData by weatherViewModel.weatherData.observeAsState()
    val error by weatherViewModel.error.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Forecast", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                weatherData != null -> {
                    WeatherScreen(weatherData!!)
                }
                error != null -> {
                    Text(text = "Error: $error", color = Color.Red, fontWeight = FontWeight.Bold)
                }
                else -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(data: WeatherResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current Weather Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(data.name, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    imageVector = getWeatherIcon(data.weather.firstOrNull()?.icon),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFFC107)
                )
                Text("${data.main.temp.toInt()}°C", fontSize = 56.sp, fontWeight = FontWeight.Bold)
                Text(data.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "", fontSize = 18.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherDetail(icon = Icons.Default.ThumbUp, value = "${data.main.humidity}%", label = "Humidity")
                    WeatherDetail(icon = Icons.Default.CheckCircle, value = "${data.wind.speed} m/s", label = "Wind")
                    WeatherDetail(icon = Icons.Default.Check, value = "${data.main.temp_max.toInt()}°/${data.main.temp_min.toInt()}°", label = "High/Low")
                }
            }
        }

        // Additional features can be added here in the future
    }
}

// Helper function to map API icon codes to Material Icons
@Composable
fun getWeatherIcon(iconCode: String?): ImageVector {
    return when (iconCode) {
        "01d" -> Icons.Default.Star // clear sky day
        "01n" -> Icons.Default.Star // clear sky night
        "02d" -> Icons.Default.Star // few clouds day
        "02n" -> Icons.Default.Star // few clouds night
        "03d", "03n" -> Icons.Default.Star // scattered clouds
        "04d", "04n" -> Icons.Default.Star // broken clouds
        "09d", "09n" -> Icons.Default.Star // shower rain
        "10d" -> Icons.Default.Star // rain day
        "10n" -> Icons.Default.Star // rain night
        "11d", "11n" -> Icons.Default.Star // thunderstorm
        "13d", "13n" -> Icons.Default.Star // snow
        "50d", "50n" -> Icons.Default.Star // mist
        else -> Icons.Default.ThumbUp
    }
}

@Composable
fun WeatherDetail(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = Color(0xFF4CAF50))
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}


@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    AgroFarmTheme {
        val previewData = WeatherResponse(
            weather = listOf(com.example.agrofarm.model.Weather("Clear sky", "01d")),
            main = com.example.agrofarm.model.Main(25.0, 15.0, 28.0, 15),
            wind = com.example.agrofarm.model.Wind(10.0),
            name = "Preview Location"
        )
        WeatherScreen(data = previewData)
    }
}