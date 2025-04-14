package com.example.weathercast
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.weathercast.databinding.ActivityMainBinding
import com.example.weathercast.viewmodel.WeatherViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        // Set up search functionality
        setupSearchFunctionality()

        // Observe LiveData
        observeViewModel()
    }

    private fun setupSearchFunctionality() {
        // Search button click listener
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // Handle keyboard search action
        binding.etCityName.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                performSearch()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun performSearch() {
        val cityName = binding.etCityName.text.toString().trim()
        if (cityName.isNotEmpty()) {
            viewModel.getWeatherForCity(cityName)
            hideKeyboard()
        }
    }

    private fun observeViewModel() {
        // Observe weather data
        viewModel.weatherData.observe(this) { weatherData ->
            binding.cardWeatherInfo.visibility = View.VISIBLE
            binding.tvError.visibility = View.GONE

            // Update UI with weather data
            binding.tvCityName.text = "${weatherData.name}, ${weatherData.sys.country}"
            binding.tvTemperature.text = "${weatherData.main.temp.toInt()}°C"
            binding.tvWeatherDescription.text = weatherData.weather[0].description.capitalize()
            binding.tvHumidity.text = "${weatherData.main.humidity}%"
            binding.tvWindSpeed.text = "${weatherData.wind.speed} m/s"

            // Load weather icon
            val iconCode = weatherData.weather[0].icon
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
            Glide.with(this)
                .load(iconUrl)
                .into(binding.ivWeatherIcon)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null) {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = errorMessage
                binding.cardWeatherInfo.visibility = View.GONE
            } else {
                binding.tvError.visibility = View.GONE
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etCityName.windowToken, 0)
    }

    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
