package com.pperrin54.happy_plants.home_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pperrin54.happy_plants.DBHandler
import com.pperrin54.happy_plants.R
import com.pperrin54.happy_plants.add_plant_activity.AddPlant
import com.pperrin54.happy_plants.plant_details_activity.PlantDetails


class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
    private var gridView: GridView? = null              // This is the grid view that will display the plants
    private var arrayList: ArrayList<PlantModal>? = null // This is the array list that will hold the plants
    private var plantAdapter: PlantAdapter? = null      // This is the adapter that will be used to populate the grid view

    private var emptyGridView : TextView? = null            // This is the view that will be displayed when the grid view is empty
    private var dbHandler: DBHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHandler = DBHandler(this)

        // This is the button that will take the user to the add plant page
        val buttonClick = findViewById<Button>(R.id.add_plant_button)
        buttonClick.setOnClickListener {
            val intent = Intent(this, AddPlant::class.java)
            startActivity(intent)
        }

        gridView = findViewById(R.id.plant_grid)    // This is the grid view that will display the plants
        emptyGridView = findViewById(R.id.empty_grid_view)  // This is the view that will be displayed when the grid view is empty
        updateList()
    }

    override fun onRestart() {
        super.onRestart()
        updateList()
    }

    private fun updateList() {
        arrayList = ArrayList()                     // This is the array list that will hold the plant items
        arrayList = setDataList()                   // This is the method that will set the data for the array list
        plantAdapter = PlantAdapter(this, arrayList!!)  // This is the adapter that will be used to display the plants
        gridView?.adapter = plantAdapter            // This is the adapter that will be used to display the plants
        gridView?.onItemClickListener = this        // This is the listener that will be called when the user clicks on a plant

        if (arrayList!!.size == 0) {
            gridView?.visibility = View.GONE
            emptyGridView?.visibility = View.VISIBLE
        } else {
            gridView?.visibility = View.VISIBLE
            emptyGridView?.visibility = View.GONE
        }
    }

    // This is the method that will be called when the user clicks on a plant
    private fun setDataList(): ArrayList<PlantModal> {
        return dbHandler!!.getAllPlants()
    }

    // This is the method that will be called when the user clicks on a plant
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item: PlantModal = arrayList?.get(position)!!                     // This is the plant item that was clicked on
        val intent = Intent(this, PlantDetails::class.java)     // This is the intent that will be used to start the plant details activity

        // This is the data that will be passed to the plant details activity
        intent.putExtra("id", item.plantID)
        intent.putExtra("name", item.plantName)
        intent.putExtra("image", item.plantImage)
        intent.putExtra("category", item.plantCategory)
        intent.putExtra("luminosity", item.plantLuminosity)
        intent.putExtra("temperature", item.plantTemperature)
        intent.putExtra("humidity", item.plantHumidity)
        intent.putExtra("last_watering", item.plantLastWatering)
        intent.putExtra("watering_freq", item.plantWateringFreq)
        intent.putExtra("happiness", item.plantHappiness)

        // Start the activity
        startActivity(intent)
    }
}
