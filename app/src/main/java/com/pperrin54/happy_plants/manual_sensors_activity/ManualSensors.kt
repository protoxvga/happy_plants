package com.pperrin54.happy_plants.manual_sensors_activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.pperrin54.happy_plants.R

class ManualSensors : AppCompatActivity() {
    private var luminosityPicker: NumberPicker? = null
    private var luminosity: Float = 1000.0f

    private var temperaturePicker: NumberPicker? = null
    private var temperature: Float = 10.0f

    private var humidityPicker: NumberPicker? = null
    private var humidity: Float = 25.0f

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual_sensors)

        luminosityPicker = findViewById(R.id.luminosity_picker)
        luminosityPicker?.minValue = 0
        luminosityPicker?.maxValue = 2
        luminosityPicker?.selectionDividerHeight = 0
        luminosityPicker?.displayedValues = arrayOf("In the shade", "Partly sunny", "Sunny")

        temperaturePicker = findViewById(R.id.temperature_picker)
        temperaturePicker?.minValue = 10
        temperaturePicker?.maxValue = 30
        temperaturePicker?.selectionDividerHeight = 0

        humidityPicker = findViewById(R.id.humidity_picker)
        humidityPicker?.minValue = 0
        humidityPicker?.maxValue = 2
        humidityPicker?.selectionDividerHeight = 0
        humidityPicker?.displayedValues = arrayOf("Dry", "Normal", "Humid")

        luminosityPicker?.setOnValueChangedListener { _, _, newVal ->
            luminosity = when (newVal) {
                0 -> 1000.0f
                1 -> 5000.0f
                2 -> 10000.0f
                else -> 1000.0f
            }
        }

        temperaturePicker?.setOnValueChangedListener { _, _, newVal ->
            temperature = newVal.toFloat()
        }

        humidityPicker?.setOnValueChangedListener { _, _, newVal ->
            humidity = when (newVal) {
                0 -> 25.0f
                1 -> 50.0f
                2 -> 75.0f
                else -> 25.0f
            }
        }

        val saveButton = findViewById<Button>(R.id.save_data_button)
        saveButton.setOnClickListener {
            val intent = Intent(this, ManualSensors::class.java)
            intent.putExtra("temperature", temperature)
            intent.putExtra("luminosity", luminosity)
            intent.putExtra("humidity", humidity)
            setResult(RESULT_OK, intent)
            finish()
        }

        val backButton = findViewById<Button>(R.id.go_back_button)
        backButton.setOnClickListener {
            val returnIntent = Intent()
            setResult(RESULT_CANCELED, returnIntent)
            finish()
        }
    }
}