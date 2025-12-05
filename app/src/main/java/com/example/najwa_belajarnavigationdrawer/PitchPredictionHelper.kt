package com.example.najwa_belajarnavigationdrawer

import android.content.Context
import ai.onnxruntime.*
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer

class PitchPredictionHelper(private val context: Context) {

    private var ortSession: OrtSession? = null
    private var ortEnvironment: OrtEnvironment? = null
    private var scalerParams: ScalerParams? = null

    init {
        loadModel()
        loadScalerParams()
    }

    private fun loadModel() {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment()

            val modelBytes = context.assets.open("pitch_model.onnx").use {
                it.readBytes()
            }

            ortSession = ortEnvironment?.createSession(modelBytes)
            println("✓ Model ONNX berhasil dimuat")
        } catch (e: Exception) {
            e.printStackTrace()
            println("✗ Gagal memuat model: ${e.message}")
        }
    }

    private fun loadScalerParams() {
        try {
            val json = context.assets.open("scaler_params.json").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use {
                    it.readText()
                }
            }

            scalerParams = Gson().fromJson(json, ScalerParams::class.java)
            println("✓ Parameter scaler berhasil dimuat")
        } catch (e: Exception) {
            e.printStackTrace()
            println("✗ Gagal memuat scaler: ${e.message}")
        }
    }

    private fun normalize(input: FloatArray): FloatArray {
        val params = scalerParams ?: return input

        return FloatArray(input.size) { i ->
            ((input[i] - params.mean[i].toFloat()) / params.scale[i].toFloat())
        }
    }

    fun predictPitch(
        acX: Float,
        acY: Float,
        acZ: Float,
        gyX: Float,
        gyY: Float,
        gyZ: Float
    ): Float? {
        try {
            // 1. Siapkan input sensor
            val rawInput = floatArrayOf(acX, acY, acZ, gyX, gyY, gyZ)

            // 2. Normalisasi input
            val normalizedInput = normalize(rawInput)

            // 3. Buat tensor input untuk ONNX
            val inputName = ortSession?.inputNames?.iterator()?.next()
            val shape = longArrayOf(1, 6)

            val buffer = FloatBuffer.wrap(normalizedInput)
            val tensor = OnnxTensor.createTensor(ortEnvironment, buffer, shape)

            // 4. Jalankan inferensi
            val results = ortSession?.run(mapOf(inputName to tensor))

            // 5. Ambil hasil prediksi
            val output = results?.get(0)?.value as Array<*>
            val prediction = (output[0] as FloatArray)[0]

            // 6. Bersihkan resource
            results?.close()
            tensor.close()

            return prediction

        } catch (e: Exception) {
            e.printStackTrace()
            println("✗ Prediksi gagal: ${e.message}")
            return null
        }
    }

    fun close() {
        try {
            ortSession?.close()
            ortEnvironment?.close()
            println("✓ Resource ONNX berhasil dibersihkan")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

