package com.example.najwa_belajarnavigationdrawer

import com.google.gson.annotations.SerializedName

data class SensorData(
    @SerializedName("AcX (LSB)", alternate = ["AcX", "acX"])
    val acX: Int? = 0,

    @SerializedName("AcY (LSB)", alternate = ["AcY", "acY"])
    val acY: Int? = 0,

    @SerializedName("AcZ (LSB)", alternate = ["AcZ", "acZ"])
    val acZ: Int? = 0,

    @SerializedName("GyX (LSB)", alternate = ["GyX", "gyX"])
    val gyX: Int? = 0,

    @SerializedName("GyY (LSB)", alternate = ["GyY", "gyY"])
    val gyY: Int? = 0,

    @SerializedName("GyZ (LSB)", alternate = ["GyZ", "gyZ"])
    val gyZ: Int? = 0,

    @SerializedName("Pitch (°)", alternate = ["Pitch", "pitch"])
    val pitch: Double? = 0.0
) {
    // Safe getters dengan default value
    fun getAcX(): Int = acX ?: 0
    fun getAcY(): Int = acY ?: 0
    fun getAcZ(): Int = acZ ?: 0
    fun getGyX(): Int = gyX ?: 0
    fun getGyY(): Int = gyY ?: 0
    fun getGyZ(): Int = gyZ ?: 0
    fun getPitch(): Double = pitch ?: 0.0
}