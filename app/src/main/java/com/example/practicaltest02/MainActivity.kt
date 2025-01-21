package com.example.practicaltest02

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val apiUrl = "https://api.coindesk.com/v1/bpi/currentprice/EUR.json"
    private val sharedPreferences by lazy { getSharedPreferences("rates_cache", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currencySpinner: Spinner = findViewById(R.id.currencySpinner)
        val sendRequestButton: Button = findViewById(R.id.sendRequestButton)
        val resultTextView: TextView = findViewById(R.id.resultTextView)
        val goToSecondActivityButton: Button = findViewById(R.id.goToSecondActivityButton)

        // Allow network on the main thread (quick and simple example; better with Coroutines)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        sendRequestButton.setOnClickListener {
            val response = fetchRates()
            if (response != null) {
                parseAndCacheRates(response)
                val selectedCurrency = currencySpinner.selectedItem.toString()
                val cachedRate = getCachedRate(selectedCurrency)
                if (cachedRate != null) {
                    resultTextView.text = "1 BTC = $cachedRate $selectedCurrency"
                } else {
                    resultTextView.text = "Rate not available for $selectedCurrency."
                }
            } else {
                Toast.makeText(this, "Failed to fetch rates.", Toast.LENGTH_LONG).show()
            }
        }

        goToSecondActivityButton.setOnClickListener {
            // Intent to navigate to SecondActivity
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
    }

    // Fetch rates from the CoinDesk API using HttpURLConnection
    private fun fetchRates(): String? {
        return try {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                response
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Parse the response and cache the rates for USD and EUR
    private fun parseAndCacheRates(response: String) {
        try {
            val json = JSONObject(response)
            val bpi = json.getJSONObject("bpi")
            val usdRate = bpi.getJSONObject("USD").getString("rate")
            val eurRate = bpi.getJSONObject("EUR").getString("rate")

            // Cache the USD and EUR rates
            cacheRate("USD", usdRate)
            cacheRate("EUR", eurRate)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error parsing response.", Toast.LENGTH_SHORT).show()
        }
    }

    // Cache a rate in shared preferences
    private fun cacheRate(currency: String, rate: String) {
        sharedPreferences.edit().putString(currency, rate).apply()
    }

    // Get a cached rate from shared preferences
    private fun getCachedRate(currency: String): String? {
        val cachedRate = sharedPreferences.getString(currency, null)
        if (cachedRate != null) {
            android.util.Log.d("Cache", "Cached rate for $currency: $cachedRate")
        } else {
            android.util.Log.d("Cache", "No cached rate found for $currency.")
        }
        return cachedRate
    }
}