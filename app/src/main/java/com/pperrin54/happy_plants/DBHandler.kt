package com.pperrin54.happy_plants

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pperrin54.happy_plants.home_activity.PlantGraphData
import com.pperrin54.happy_plants.home_activity.PlantModal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DBHandler
    (context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + IMAGE_COL + " INT,"
                + CATEGORY_COL + " TEXT,"
                + LUMINOSITY_COL + " FLOAT,"
                + TEMPERATURE_COL + " FLOAT,"
                + HUMIDITY_COL + " FLOAT,"
                + LAST_WATERING_COL + " TEXT,"
                + WATERING_FREQ_COL + " INT,"
                + PLANT_HAPPINESS_COL + " FLOAT)")

        val graphQuery = ("CREATE TABLE " + TABLE_NAME2 + " ("
                + ID_COL_GRAPH + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PLANT_ID + " INT,"
                + DATE_COL + " TEXT,"
                + GRAPH_LUMINOSITY_COL + " FLOAT,"
                + GRAPH_TEMPERATURE_COL + " FLOAT,"
                + GRAPH_HUMIDITY_COL + " FLOAT,"
                + GRAPH_PLANT_HAPPINESS_COL + " FLOAT)")

        db.execSQL(query)
        db.execSQL(graphQuery)
    }
    @SuppressLint("Range", "Recycle")
    fun addNewPlant(
        plantName: String?,
        plantImage: Int?,
        plantCategory: String?,
        plantLuminosity: Float?,
        plantTemperature: Float?,
        plantHumidity: Float?,
        plantLastWatering: String?,
        plantWateringFreq: Int?,
        plantHappiness: Float?
    ) {
        val db = this.writableDatabase

        val values = ContentValues()

        values.put(NAME_COL, plantName)
        values.put(IMAGE_COL, plantImage)
        values.put(CATEGORY_COL, plantCategory)
        values.put(LUMINOSITY_COL, plantLuminosity)
        values.put(TEMPERATURE_COL, plantTemperature)
        values.put(HUMIDITY_COL, plantHumidity)
        values.put(LAST_WATERING_COL, plantLastWatering)
        values.put(WATERING_FREQ_COL, plantWateringFreq)
        values.put(PLANT_HAPPINESS_COL, plantHappiness)

        db.insert(TABLE_NAME, null, values)

        val cursor: Cursor = db.rawQuery("SELECT last_insert_rowid()", null)
        cursor.moveToFirst()

        val graphValues = ContentValues()
        graphValues.put(PLANT_ID, cursor.getInt(0))
        graphValues.put(DATE_COL, "12/11/2022")
        graphValues.put(GRAPH_LUMINOSITY_COL, plantLuminosity)
        graphValues.put(GRAPH_TEMPERATURE_COL, plantTemperature)
        graphValues.put(GRAPH_HUMIDITY_COL, plantHumidity)
        graphValues.put(GRAPH_PLANT_HAPPINESS_COL, plantHappiness)

        db.insert(TABLE_NAME2, null, graphValues)

        db.close()
    }

    fun updatePlant(
        plantID: Int,
        plantName: String,
        plantImage: Int,
        plantCategory: String,
        plantLuminosity: Float,
        plantTemperature: Float,
        plantHumidity: Float,
        plantLastWatering: String,
        plantWateringFreq: Int,
        plantHappiness: Float,
        reason: String
    ) {
        val today: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(NAME_COL, plantName)
        values.put(IMAGE_COL, plantImage)
        values.put(CATEGORY_COL, plantCategory)
        values.put(LUMINOSITY_COL, plantLuminosity)
        values.put(TEMPERATURE_COL, plantTemperature)
        values.put(HUMIDITY_COL, plantHumidity)
        values.put(LAST_WATERING_COL, plantLastWatering)
        values.put(WATERING_FREQ_COL, plantWateringFreq)
        values.put(PLANT_HAPPINESS_COL, plantHappiness)

        db.update(TABLE_NAME, values, "id=?", arrayOf(plantID.toString()))

        if (reason == "dataUpdate") {
            val graphValues = ContentValues()
            graphValues.put(PLANT_ID, plantID)
            graphValues.put(DATE_COL, today)
            graphValues.put(GRAPH_LUMINOSITY_COL, plantLuminosity)
            graphValues.put(GRAPH_TEMPERATURE_COL, plantTemperature)
            graphValues.put(GRAPH_HUMIDITY_COL, plantHumidity)
            graphValues.put(GRAPH_PLANT_HAPPINESS_COL, plantHappiness)

            db.insert(TABLE_NAME2, null, graphValues)
        }

        db.close()
    }

    @SuppressLint("Range")
    fun getLastUpdate(plantID: Int): String {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME2 WHERE $PLANT_ID = $plantID ORDER BY $ID_COL_GRAPH DESC LIMIT 1"
        val cursor = db.rawQuery(query, null)
        var lastUpdate = ""
        if (cursor.moveToFirst()) {
            lastUpdate = cursor.getString(cursor.getColumnIndex(DATE_COL))
        }
        cursor.close()
        db.close()
        return lastUpdate
    }

    fun getGraphData(plantID: Int): ArrayList<PlantGraphData> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME2 WHERE $PLANT_ID = $plantID"
        val cursor = db.rawQuery(query, null)
        val graphData = ArrayList<PlantGraphData>()
        if (cursor.moveToFirst()) {
            do {
                val plantGraphData = PlantGraphData(
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getFloat(3),
                    cursor.getFloat(4),
                    cursor.getFloat(5),
                    cursor.getFloat(6)
                )
                graphData.add(plantGraphData)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return graphData
    }

    @SuppressLint("Range")
    fun getAllPlants(): ArrayList<PlantModal> {
        // on below line we are creating a
        // database for reading our database.
        val db = this.readableDatabase

        // on below line we are creating a cursor with query to read data from database.
        val cursorPlant: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        // on below line we are creating a new array list.
        val plantModalArrayList: ArrayList<PlantModal> = ArrayList()

        // moving our cursor to first position.
        if (cursorPlant.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                plantModalArrayList.add(
                    PlantModal(
                        cursorPlant.getInt(cursorPlant.getColumnIndex(ID_COL)),
                        cursorPlant.getString(1),
                        cursorPlant.getInt(2),
                        cursorPlant.getString(3),
                        cursorPlant.getFloat(4),
                        cursorPlant.getFloat(5),
                        cursorPlant.getFloat(6),
                        cursorPlant.getString(7),
                        cursorPlant.getInt(8),
                        cursorPlant.getFloat(9)
                    )
                )
            } while (cursorPlant.moveToNext())
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        cursorPlant.close()
        return plantModalArrayList
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // below is the method for deleting our plants.
    fun deletePlant(plantID: Int) {
        val db = this.writableDatabase

        db.delete(TABLE_NAME, "id=?", arrayOf(plantID.toString()))
        db.close()
    }

    companion object {
        private const val DB_NAME = "happy_plants_db"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "plants"
        private const val ID_COL = "id"
        private const val NAME_COL = "name"
        private const val IMAGE_COL = "image"
        private const val CATEGORY_COL = "category"
        private const val LUMINOSITY_COL = "luminosity"
        private const val TEMPERATURE_COL = "temperature"
        private const val HUMIDITY_COL = "humidity"
        private const val LAST_WATERING_COL = "last_watered"
        private const val WATERING_FREQ_COL = "watering_freq"
        private const val PLANT_HAPPINESS_COL = "happiness"

        private const val TABLE_NAME2 = "graphData"
        private const val ID_COL_GRAPH = "id"
        private const val PLANT_ID = "plant_id"
        private const val DATE_COL = "date"
        private const val GRAPH_LUMINOSITY_COL = "luminosity"
        private const val GRAPH_TEMPERATURE_COL = "temperature"
        private const val GRAPH_HUMIDITY_COL = "humidity"
        private const val GRAPH_PLANT_HAPPINESS_COL = "happiness"
    }
}
