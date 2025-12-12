package com.example.najwa_belajarnavigationdrawer

import com.google.gson.annotations.SerializedName

data class SensorData(
    @SerializedName("AcX (LSB)") val acX: Int,
    @SerializedName("AcY (LSB)") val acY: Int,
    @SerializedName("AcZ (LSB)") val acZ: Int,
    @SerializedName("GyX (LSB)") val gyX: Int,
    @SerializedName("GyY (LSB)") val gyY: Int,
    @SerializedName("GyZ (LSB)") val gyZ: Int,
    @SerializedName("Pitch (°)") val pitch: Double
)
