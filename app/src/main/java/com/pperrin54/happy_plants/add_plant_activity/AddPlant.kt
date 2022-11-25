package com.pperrin54.happy_plants.add_plant_activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.pperrin54.happy_plants.DBHandler
import com.pperrin54.happy_plants.R
import com.pperrin54.happy_plants.manual_sensors_activity.ManualSensors
import java.util.*
import kotlin.math.roundToInt

class AddPlant : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager   // This is the sensor manager that will be used to get the sensor data

    // Sensors that will be used to detect temperature, luminosity and humidity
    private var temperature: Sensor? = null
    private var luminosity: Sensor? = null
    private var humidity: Sensor? = null

    private var saveButton: Button?= null

    // This is the edit text that will be used to get the plant name and category
    private var plantName: EditText? = null
    private var plantCategory: String? = null

    // This is the Spinner that will be used to get the plant category
    private var plantCategorySpinner: Spinner? = null

    // TextViews for sensors values
    private var temperatureTextView: TextView? = null
    private var luminosityTextView: TextView? = null
    private var humidityTextView: TextView? = null
    private var happinessView: TextView? = null

    private var finalLuminosity: Float = 0.0F
    private var finalTemperature: Float = 0.0F
    private var finalHumidity: Float = 0.0F

    private var manualLuminosity: Float = 0.0F
    private var manualTemperature: Float = 0.0F
    private var manualHumidity: Float = 0.0F

    private var happiness: Float = 0.0F

    private var alertDialog: AlertDialog? = null

    // This is the database handler that will be used to access the database
    private var dbHandler: DBHandler? = null

    // If the user doesn't have sensors on his phone, he will be able to manually enter the values
    @SuppressLint("SetTextI18n")
    private val getSensorsManually = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK) {
            manualLuminosity = it.data?.getFloatExtra("luminosity", 0.0F)!!
            manualTemperature = it.data?.getFloatExtra("temperature", 0.0F)!!
            manualHumidity = it.data?.getFloatExtra("humidity", 0.0F)!!
            luminosityTextView!!.text = "${manualLuminosity.roundToInt()} lx"
            temperatureTextView!!.text = "${(manualTemperature * 10.0).roundToInt() / 10.0} °C"
            humidityTextView!!.text = "${manualHumidity.roundToInt()} %"
            happiness = HappinessComputation().computeHappiness(manualLuminosity, manualTemperature, manualHumidity)
            if (happiness < 0.0F)
                happinessView!!.text = "0 %"
            else
                happinessView!!.text = "${happiness.roundToInt()} %"
            if (plantName!!.text.toString() != "") {
                saveButton!!.isEnabled = true
            }
        }
    }

    @SuppressLint("ServiceCast", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_plant)

        val pm = this.packageManager

        // This is Views that will be used to get the plant name and category
        plantName = findViewById(R.id.plant_name)
        plantCategorySpinner = findViewById(R.id.plant_category_spinner)

        // This is View that will be used to get the plant sensors data
        temperatureTextView = findViewById(R.id.temperature)
        luminosityTextView = findViewById(R.id.luminosity)
        humidityTextView = findViewById(R.id.humidity)
        happinessView = findViewById(R.id.happiness)

        // Initializing the SensorManager and sensors objects
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        luminosity = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)

        dbHandler = DBHandler(this)

        // This is the button that will take the user back to the home page
        val backButton = findViewById<Button>(R.id.go_back_button)
        backButton.setOnClickListener {
            finish()
        }

        // This is the listener that will be called when the user selects an item from the spinner
        plantCategorySpinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                plantCategory = adapterView?.getItemAtPosition(position).toString()
            }
        }

        // This is the button that will add the plant to the database
        saveButton = findViewById<Button>(R.id.save_plant_button)
        saveButton!!.setOnClickListener {
            // below line is to get data from all edit text fields.
            val plantName: String = plantName!!.text.toString()
            val plantCategory: String = plantCategory!!
            val plantImage: Int = chooseImage(plantCategory)
            val plantLastWatering = "10/11/2022"
            //val plantLastWatering = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val plantWateringFreq = chooseFrequency(plantCategory)
            val plantHappiness: Float = happiness

            if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT) || !pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE) || !pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY)) {
                dbHandler!!.addNewPlant(plantName, plantImage, plantCategory, manualLuminosity, manualTemperature, manualHumidity, plantLastWatering, plantWateringFreq, plantHappiness)
            } else {
                dbHandler!!.addNewPlant(plantName, plantImage, plantCategory, finalLuminosity, finalTemperature, finalHumidity, plantLastWatering, plantWateringFreq, plantHappiness)
            }
            finish()
        }

        // This is the trigger to change sensors values when the user call analyze button
        val analyzeButton = findViewById<Button>(R.id.analyze_plant_button)
        analyzeButton.setOnClickListener {
            if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT) || !pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE) || !pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY)) {
                val intent = Intent(this, ManualSensors::class.java)
                getSensorsManually.launch(intent)
            } else {
                luminosityTextView!!.text = "${finalLuminosity.roundToInt()} lx"
                temperatureTextView!!.text = "${(finalTemperature * 10.0).roundToInt() / 10.0} °C"
                humidityTextView!!.text = "${finalHumidity.roundToInt()} %"
                happiness = HappinessComputation().computeHappiness(finalLuminosity, finalTemperature, finalHumidity)

                happinessView!!.text = "${happiness.roundToInt()} %"

                if (happiness < 0.0F) {
                    happinessView!!.text = "0 %"
                    val alertMessage = when (happiness) {
                        -1.0F -> "The luminosity is inappropriate for a plant. Please put it in a different place."
                        -2.0F -> "The temperature is inappropriate for a plant. Please put it in a different place."
                        else -> "The humidity is inappropriate for this plant. Please put it in a different place."
                    }
                    showAlertDialog(alertMessage)
                }
            }

            saveButton!!.isEnabled = plantName!!.text.toString().isNotEmpty() && luminosityTextView!!.text != "0.0 lx" && temperatureTextView!!.text != "0.0 °C" && humidityTextView!!.text != "0.0 %"
        }

        // This is the text field that will be used to enter the plant name
        plantName!!.doOnTextChanged { text, _, _, _ ->
            // Enable the save button only if the plant name is not empty and the plant sensors values are not empty
            saveButton!!.isEnabled = text!!.isNotEmpty() && luminosityTextView!!.text != "0.0 lx" && temperatureTextView!!.text != "0.0 °C" && humidityTextView!!.text != "0.0 %"
        }
    }

    private fun chooseImage(plantCategory: String): Int {
        return when (plantCategory) {
            "Flowering" -> R.drawable.plant_flowering
            "Colorful Foliage" -> R.drawable.plant_colorful_foliage
            "Low-Light" -> R.drawable.plant_low_light
            "Trailing" -> R.drawable.plant_trailing
            "Small Houseplant" -> R.drawable.plant_small
            "Large Houseplant" -> R.drawable.plant_large
            "Succulents | Cacti" -> R.drawable.plant_cactus
            else -> R.drawable.plant_default
        }
    }

    private fun chooseFrequency(plantCategory: String): Int {
        return when (plantCategory) {
            "Flowering" -> 7
            "Colorful Foliage" -> 8
            "Low-Light" -> 12
            "Trailing" -> 14
            "Small Houseplant" -> 4
            "Large Houseplant" -> 6
            "Succulents | Cacti" -> 10
            else -> 7
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            finalLuminosity = event.values[0]
        }

        if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            finalTemperature = event.values[0]
        }

        if (event.sensor.type == Sensor.TYPE_RELATIVE_HUMIDITY) {
            finalHumidity = event.values[0]
        }
    }

    private fun showAlertDialog(alertMessage: String) {
        alertDialog = AlertDialog.Builder(this)
            .setTitle("Be Careful")
            .setMessage(alertMessage)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialog!!.show()
    }

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()
        sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, luminosity, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, humidity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onBackPressed() {
        alertDialog?.show()
    }
}