package com.example.weathercast

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.weathercast.databinding.ActivityMainBinding
import com.example.weathercast.viewmodel.WeatherViewModel
import androidx.appcompat.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

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

        setupSwipeRefresh()
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


    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Get the current city from the TextView
            val currentCity = binding.tvCityName.text.toString().split(",").firstOrNull()?.trim()

            if (!currentCity.isNullOrEmpty()) {
                // Refresh weather data for the current city
                viewModel.getWeatherForCity(currentCity)
            } else {
                // If no city is displayed, just stop the refresh animation
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // Set colors for the refresh indicator
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )
    }

    private fun observeViewModel() {
        // Observe weather data
        viewModel.weatherData.observe(this) { weatherData ->
            // Show weather card with animation
            binding.tvError.visibility = View.GONE

            // First set visibility
            binding.cardWeatherInfo.visibility = View.VISIBLE
            binding.tvError.visibility = View.GONE

            // Then load the animation and apply it
            val animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            binding.cardWeatherInfo.startAnimation(animation)

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

        // Update the isLoading observer to handle the SwipeRefreshLayout
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
                // Stop the refresh animation when loading is complete
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null) {
                binding.tvError.visibility = View.VISIBLE

                // Add a fade-in animation for the error message
                val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                binding.tvError.startAnimation(fadeInAnimation)

                binding.tvError.text = errorMessage
                binding.cardWeatherInfo.visibility = View.GONE
            } else {
                binding.tvError.visibility = View.GONE
            }
        }
    }

    private fun animateSearchBar() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.cardSearchBar.startAnimation(animation)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etCityName.windowToken, 0)
    }

    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {
                // Toggle between night and day mode
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

