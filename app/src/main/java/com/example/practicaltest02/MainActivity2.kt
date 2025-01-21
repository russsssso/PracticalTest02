package com.example.practicaltest02

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val t1EditText: EditText = findViewById(R.id.t1EditText)
        val t2EditText: EditText = findViewById(R.id.t2EditText)
        val operationSpinner: Spinner = findViewById(R.id.operationSpinner)
        val sendRequestButton: Button = findViewById(R.id.sendRequestButton)
        val resultTextView: TextView = findViewById(R.id.resultTextView)

        // Spinner items (for example, predefined in your resources)
        val operations = arrayOf("plus", "minus", "times", "divide")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, operations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        operationSpinner.adapter = adapter

        sendRequestButton.setOnClickListener {
            // Get the inputs
            val t1 = t1EditText.text.toString().toIntOrNull()
            val t2 = t2EditText.text.toString().toIntOrNull()
            val operation = operationSpinner.selectedItem.toString()

            if (t1 != null && t2 != null) {
                // Send the request to the server
                val result = sendRequestToServer(t1, t2, operation)
                resultTextView.text = "Result: $result"
            } else {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to send the request to the Python server
    private fun sendRequestToServer(t1: Int, t2: Int, operation: String): String {
        return try {
            val url = URL("http://10.0.2.2:8080?operation=$operation&t1=$t1&t2=$t2")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 10000
            urlConnection.readTimeout = 10000

            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = InputStreamReader(urlConnection.inputStream)
                val response = reader.readText()
                reader.close()
                response
            } else {
                "Error: Unable to connect to server"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}