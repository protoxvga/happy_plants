package com.pperrin54.happy_plants.add_plant_activity

class HappinessComputation {
    private fun computeTemperature(temperature: Float): Float {
        return when {
            temperature < 10 -> 0.0F
            temperature in 10.0..15.0 -> 10 + (temperature - 10) * 8
            temperature in 15.0..20.0 -> 50 + (temperature - 15) * 8
            temperature in 20.0..30.0 -> 90 + (temperature - 20)
            else -> 0.0F
        }
    }

    private fun computeLuminosity(luminosity: Float): Float {
        return when {
            luminosity < 200 -> 0.0F
            luminosity in 200.0..500.0 -> 5 + luminosity / 100
            luminosity in 500.0..1000.0 -> 10 + luminosity / 100
            luminosity in 1000.0..2000.0 -> 15 + luminosity / 100
            luminosity in 2000.0..5000.0 -> 20 + luminosity / 100
            luminosity in 5000.0..10000.0 -> 25 + luminosity / 100
            luminosity in 10000.0..30000.0 -> 100 - luminosity / 1000
            luminosity > 30000.0 -> 0.0F
            else -> 0.0F
        }
    }

    private fun computeHumidity(humidity: Float): Float {
        return when {
            humidity < 20 -> 0.0F
            humidity in 20.0..40.0 -> 10 + humidity / 2
            humidity in 40.0..60.0 -> 60 + humidity / 2
            humidity in 60.0..80.0 -> 100 - humidity / 3
            humidity in 80.0..100.0 -> 100 - humidity
            else -> 0.0F
        }
    }

    fun computeHappiness(luminosity: Float, temperature: Float, humidity: Float): Float {
        val luminosityScore = computeLuminosity(luminosity)
        val temperatureScore = computeTemperature(temperature)
        val humidityScore = computeHumidity(humidity)

        return if (luminosityScore == 0.0F) {
            -1.0F
        } else if (temperatureScore == 0.0F) {
            -2.0F
        } else if (humidityScore == 0.0F) {
            -3.0F
        } else if (((luminosityScore * 2) + (temperatureScore * 2) + humidityScore) / 5 > 100.0F) {
            100.0F
        } else {
            ((luminosityScore * 2) + (temperatureScore * 2) + humidityScore) / 5
        }
    }
}