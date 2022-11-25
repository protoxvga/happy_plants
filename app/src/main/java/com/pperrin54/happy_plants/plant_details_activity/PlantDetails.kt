package com.pperrin54.happy_plants.plant_details_activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pperrin54.happy_plants.DBHandler
import com.pperrin54.happy_plants.R
import com.pperrin54.happy_plants.add_plant_activity.HappinessComputation
import com.pperrin54.happy_plants.manual_sensors_activity.ManualSensors
import com.pperrin54.happy_plants.plant_graph_activity.PlantGraphActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class PlantDetails : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager   // This is the sensor manager that will be used to get the sensor data

    // Sensors that will be used to detect temperature, luminosity and humidity
    private var luminositySensor: Sensor? = null
    private var temperatureSensor: Sensor? = null
    private var humiditySensor: Sensor? = null

    private var dbHandler: DBHandler? = null

    private var plantName: TextView? = null
    private var plantImage: ImageView? = null
    private var plantType: TextView? = null
    private var plantLuminosity: TextView? = null
    private var plantTemperature: TextView? = null
    private var plantHumidity: TextView? = null
    private var wateringFreqView: TextView? = null
    private var lastWateringView: TextView? = null
    private var happinessView: TextView? = null

    private var plantId by Delegates.notNull<Int>()
    private lateinit var name: String
    private var image by Delegates.notNull<Int>()
    private lateinit var category: String
    private var luminosity by Delegates.notNull<Float>()
    private var temperature by Delegates.notNull<Float>()
    private var humidity by Delegates.notNull<Float>()
    private lateinit var lastWatering: String
    private var wateringFreq by Delegates.notNull<Int>()
    private var happiness by Delegates.notNull<Float>()

    private var backButton: Button? = null
    private var graphButton: Button? = null
    private var deleteButton: Button? = null
    private var waterButton: Button? = null

    private var alertDialog: android.app.AlertDialog? = null
    private var alertMessage: String? = null

    private var today: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    @SuppressLint("SetTextI18n")
    private val getSensorsManually = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK) {
            luminosity = it.data?.getFloatExtra("luminosity", 0.0F)!!
            temperature = it.data?.getFloatExtra("temperature", 0.0F)!!
            humidity = it.data?.getFloatExtra("humidity", 0.0F)!!
            plantLuminosity!!.text = "${luminosity.roundToInt()} lx"
            plantTemperature!!.text = "${(temperature * 10.0).roundToInt() / 10.0} °C"
            plantHumidity!!.text = "${humidity.roundToInt()} %"
            happiness = HappinessComputation().computeHappiness(luminosity, temperature, humidity)
            dbHandler!!.updatePlant(plantId, name, image, category, luminosity, temperature, humidity, lastWatering, wateringFreq, happiness, "dataUpdate")
            happinessView!!.text = "${happiness.roundToInt()} %"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // This is the first method that is called when the activity is created.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plant_details)

        plantId = intent.getIntExtra("id", 0)
        name = intent.getStringExtra("name").toString()                    // This is the name of the plant
        image = intent.getIntExtra("image", 0)                  // This is the image of the plant
        category = intent.getStringExtra("category").toString()            // This is the category of the plant
        luminosity = intent.getFloatExtra("luminosity", 0.0F)   // This is the luminosity of the plant
        temperature = intent.getFloatExtra("temperature", 0.0F) // This is the temperature of the plant
        humidity = intent.getFloatExtra("humidity", 0.0F)       // This is the humidity of the plant
        lastWatering = intent.getStringExtra("last_watering").toString()   // This is the last watering date of the plant
        wateringFreq = intent.getIntExtra("watering_freq", 0)   // This is the watering frequency of the plant
        happiness = intent.getFloatExtra("happiness", 0.0F)     // This is the happiness of the plant

        dbHandler = DBHandler(this)

        // Initializing the SensorManager and sensors objects
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        luminositySensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)

        setData()

        val updateButton = findViewById<Button>(R.id.update_button)
        updateButton.setOnClickListener {
            val pm = this.packageManager

            if (dbHandler!!.getLastUpdate(plantId) == today) {
                Toast.makeText(this, "You already updated this plant today", Toast.LENGTH_SHORT).show()
            } else if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT) || !pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE) || !pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY)) {
                val intent = Intent(this, ManualSensors::class.java)
                getSensorsManually.launch(intent)
            } else {
                updatePlantData()
            }
        }

        backButton = findViewById(R.id.go_back_button)
        backButton!!.setOnClickListener {
            finish()
        }

        graphButton = findViewById(R.id.graph_button)
        graphButton!!.setOnClickListener {
            val intent = Intent(this, PlantGraphActivity::class.java)
            intent.putExtra("id", plantId)
            startActivity(intent)
        }

        // This is the button that will delete the plant from the database
        deleteButton = findViewById(R.id.delete_plant_button)
        deleteButton!!.setOnClickListener {
            summonDialog()
        }

        // This is the button that will update the last watering date of the plant
        waterButton = findViewById(R.id.water_plant_button)
        waterButton!!.setOnClickListener {
            if (today != lastWatering) {
                lastWatering = today
                lastWateringView!!.text = lastWatering
                dbHandler!!.updatePlant(plantId, name, image, category, luminosity, temperature, humidity, lastWatering, wateringFreq, happiness, "watering")
                lastWateringView?.text = "Last watering was on $today"
                wateringFreqView?.text = "in $wateringFreq days"
            } else {
                Toast.makeText(this, "You already watered this plant today.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun setData() {
        val nextWatering = computeWateringDays(lastWatering, wateringFreq)

        plantName = findViewById(R.id.plant_name)               // This is the text view that will display the plant name
        plantImage = findViewById(R.id.plant_image)             // This is the image view that will display the plant image
        plantType = findViewById(R.id.plant_category)           // This is the text view that will display the plant category
        plantLuminosity = findViewById(R.id.luminosity)         // This is the text view that will display the plant luminosity
        plantTemperature = findViewById(R.id.temperature)       // This is the text view that will display the plant temperature
        plantHumidity = findViewById(R.id.humidity)             // This is the text view that will display the plant humidity
        lastWateringView = findViewById(R.id.last_watering)     // This is the text view that will display the plant last watering date
        wateringFreqView = findViewById(R.id.next_watering)     // This is the text view that will display the plant watering frequency
        happinessView = findViewById(R.id.happiness)            // This is the text view that will display the plant happiness

        plantName!!.text = name                 // Sets the text of the plant name text view to the name of the plant
        plantType!!.text = category             // Sets the text of the plant category text view to the category of the plant
        plantImage!!.setImageResource(image)    // Sets the image of the plant image view to the image of the plant
        plantLuminosity!!.text = "${luminosity.roundToInt()} lx"                    // Sets the text of the plant luminosity text view to the luminosity of the plant
        plantTemperature!!.text = "${(temperature * 10.0).roundToInt() / 10.0} °C"  // Sets the text of the plant temperature text view to the temperature of the plant
        plantHumidity!!.text = "${humidity.roundToInt()} %"                         // Sets the text of the plant humidity text view to the humidity of the plant
        lastWateringView!!.text = "Last watering was on $lastWatering"  // Sets the text of the plant last watering text view to the last watering date of the plant
        happinessView!!.text = "${happiness.roundToInt()} %"            // Sets the text of the plant happiness text view to the happiness of the plant

        if (nextWatering == 0) {
            wateringFreqView!!.text = "Watering needed today"  // Sets the text of the plant watering frequency text view to the watering frequency of the plant
        } else {
            wateringFreqView!!.text = "in $nextWatering days"  // Sets the text of the plant watering frequency text view to the watering frequency of the plant
        }
    }

    private fun summonDialog() {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Delete Plant")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete this plant?")

        //performing positive action
        builder.setPositiveButton("Yes"){ _, _ ->
            dbHandler!!.deletePlant(plantId)
            Toast.makeText(applicationContext,"Plant deleted",Toast.LENGTH_LONG).show()
            finish()
        }
        //performing cancel action
        builder.setNeutralButton("Cancel"){ _, _ ->}

        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun computeWateringDays(lastWatering: String, wateringFreq: Int): Int {
        val mDateFormat = SimpleDateFormat("dd/MM/yyyy")

        val lastWateringDate = mDateFormat.parse(lastWatering)
        val todayDate = mDateFormat.parse(today)

        val mDifference = kotlin.math.abs(lastWateringDate!!.time - todayDate!!.time)

        // Converting milli seconds to dates
        val dayDifference = (mDifference / (24 * 60 * 60 * 1000)).toInt()

        return if (wateringFreq - dayDifference < 0) {
            0
        } else {
            wateringFreq - dayDifference
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            luminosity = event.values[0]
        }

        if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            temperature = event.values[0]
        }

        if (event.sensor.type == Sensor.TYPE_RELATIVE_HUMIDITY) {
            humidity = event.values[0]
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePlantData() {
        happiness = HappinessComputation().computeHappiness(luminosity, temperature, humidity)
        if (happiness < 0.0F) {
            alertMessage = when (happiness) {
                -1.0F -> "The luminosity is inappropriate for a plant. Please put it in a different place."
                -2.0F -> "The temperature is inappropriate for a plant. Please put it in a different place."
                else -> "The humidity is inappropriate for this plant. Please put it in a different place."
            }

            // This is the alert dialog that will be shown when the user try to add a plant without analyzing it
            alertDialog = android.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Can't update plants data.\n $alertMessage")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog!!.show()
        } else {
            plantLuminosity!!.text = "${luminosity.roundToInt()} lx"
            plantTemperature!!.text = "${(temperature * 10.0).roundToInt() / 10.0} °C"
            plantHumidity!!.text = "${humidity.roundToInt()} %"

            happinessView!!.text = "${happiness.roundToInt()} %"

            dbHandler!!.updatePlant(plantId, name, image, category, luminosity, temperature, humidity, lastWatering, wateringFreq, happiness, "dataUpdate")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onResume() {
        // Register a listener for the sensor.
        super.onResume()
        sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, luminositySensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL)
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
