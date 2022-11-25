package com.pperrin54.happy_plants.home_activity

class PlantModal(
    val plantID: Int,
    var plantName: String,
    var plantImage: Int,
    var plantCategory: String,
    var plantLuminosity: Float,
    var plantTemperature: Float,
    var plantHumidity: Float,
    var plantLastWatering: String,
    var plantWateringFreq: Int,
    var plantHappiness: Float
) {
    var id = 0
}

class PlantGraphData(
    val plantID: Int,
    var date: String,
    var newPlantLuminosity: Float,
    var newPlantTemperature: Float,
    var newPlantHumidity: Float,
    var newPlantHappiness: Float
) {
    var id = 0
}