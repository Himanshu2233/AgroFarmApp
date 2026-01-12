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
import com.example.agrofarm.ui.theme.ThemeManager
import com.example.agrofarm.viewmodel.WeatherViewModel
import androidx.compose.runtime.collectAsState


class WeatherActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeManager.init(this)

        // Fetch weather for a default location (e.g., London)
//        weatherViewModel.fetchWeatherData(51.5072, -0.1276)
        // Fetch weather for Kathmandu, Nepal
        weatherViewModel.fetchWeatherData(27.7172, 85.3240)

        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            AgroFarmTheme(darkTheme = isDarkMode) {
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
                    containerColor = MaterialTheme.colorScheme.primary,
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    data.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    imageVector = getWeatherIcon(data.weather.firstOrNull()?.icon),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFFC107)
                )
                Text(
                    "${data.main.temp.toInt()}°C",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    data.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherDetail(icon = Icons.Default.WaterDrop, value = "${data.main.humidity}%", label = "Humidity")
                    WeatherDetail(icon = Icons.Default.Air, value = "${data.wind.speed} m/s", label = "Wind")
                    WeatherDetail(icon = Icons.Default.Thermostat, value = "${data.main.temp_max.toInt()}°/${data.main.temp_min.toInt()}°", label = "High/Low")
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
        "01d" -> Icons.Default.WbSunny // clear sky day
        "01n" -> Icons.Default.NightsStay // clear sky night
        "02d" -> Icons.Default.WbCloudy // few clouds day
        "02n" -> Icons.Default.Cloud // few clouds night
        "03d", "03n" -> Icons.Default.Cloud // scattered clouds
        "04d", "04n" -> Icons.Default.Cloud // broken clouds
        "09d", "09n" -> Icons.Default.Grain // shower rain
        "10d" -> Icons.Default.Grain // rain day
        "10n" -> Icons.Default.Grain // rain night
        "11d", "11n" -> Icons.Default.Thunderstorm // thunderstorm
        "13d", "13n" -> Icons.Default.AcUnit // snow
        "50d", "50n" -> Icons.Default.Cloud // mist
        else -> Icons.Default.WbSunny
    }
}

@Composable
fun WeatherDetail(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
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