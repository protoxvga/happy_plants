package com.pperrin54.happy_plants.plant_graph_activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.pperrin54.happy_plants.DBHandler
import com.pperrin54.happy_plants.R
import com.pperrin54.happy_plants.home_activity.PlantGraphData


class PlantGraphActivity : AppCompatActivity() {
    private var data: ArrayList<PlantGraphData>? = null
    private var plantId: Int = 0

    private var dbHandler: DBHandler? = null

    private var sendHappiness: ArrayList<Float>? = null
    private var sendDates: ArrayList<String>? = null
    private var sendLuminosity: ArrayList<Float>? = null
    private var sendTemperature: ArrayList<Float>? = null
    private var sendHumidity: ArrayList<Float>? = null

    private var mLineGraph: CustomGraph? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plant_graph)

        plantId = intent.getIntExtra("id", 0)

        dbHandler = DBHandler(this)

        val backButton = findViewById<Button>(R.id.go_back_button)
        backButton.setOnClickListener {
            finish()
        }

        data = dbHandler!!.getGraphData(plantId)

        // get all happiness values from data and put them in an array
        prepareSendedData()

        mLineGraph = findViewById(R.id.custom_graph)
        mLineGraph!!.setData(sendHappiness!!, sendDates!!)
        mLineGraph!!.postInvalidate()

        val luminosityButton = findViewById<Button>(R.id.luminosity_graph_button)
        luminosityButton.setOnClickListener {
            mLineGraph!!.changeData(sendLuminosity!!, sendDates!!, "luminosity (.10^3 lx)", "luminosity", 4)
        }

        val happinessButton = findViewById<Button>(R.id.happiness_graph_button)
        happinessButton.setOnClickListener {
            mLineGraph!!.changeData(sendHappiness!!, sendDates!!, "happiness (%)", "happiness", 10)
        }

        val humidityButton = findViewById<Button>(R.id.humidity_graph_button)
        humidityButton.setOnClickListener {
            mLineGraph!!.changeData(sendHumidity!!, sendDates!!, "humidity (%)", "humidity", 10)
        }

        val temperatureButton = findViewById<Button>(R.id.temperature_graph_button)
        temperatureButton.setOnClickListener {
            mLineGraph!!.changeData(sendTemperature!!, sendDates!!, "temperature (°C)", "temperature", 3)
        }
    }

    private fun prepareSendedData() {
        val happinessValues = arrayListOf<Float>()
        for (i in 0 until data!!.size) {
            val happiness = data!![i].newPlantHappiness
            happinessValues.add(happiness / 10F)
        }
        sendHappiness = ArrayList(happinessValues.takeLast(10))

        val dates = arrayListOf<String>()
        for (i in 0 until data!!.size) {
            val date = data!![i].date
            val formattedDate: String = date.substring(0, date.lastIndexOf("/"))
            dates.add(formattedDate)
        }
        sendDates = ArrayList(dates.takeLast(10))

        val luminosityValues = arrayListOf<Float>()
        for (i in 0 until data!!.size) {
            val luminosity = data!![i].newPlantLuminosity
            luminosityValues.add(luminosity / 4000f)
        }
        sendLuminosity = ArrayList(luminosityValues.takeLast(10))

        val humidityValues = arrayListOf<Float>()
        for (i in 0 until data!!.size) {
            val humidity = data!![i].newPlantHumidity
            humidityValues.add(humidity / 10f)
        }
        sendHumidity = ArrayList(humidityValues.takeLast(10))

        val temperatureValues = arrayListOf<Float>()
        for (i in 0 until data!!.size) {
            val temperature = data!![i].newPlantTemperature
            temperatureValues.add(temperature / 3f)
        }
        sendTemperature = ArrayList(temperatureValues.takeLast(10))
    }
}
