package com.example.najwa_belajarnavigationdrawer

import android.content.Context
import android.util.Log
import ai.onnxruntime.*
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer

class PitchPredictionHelper(private val context: Context) {

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val mean: FloatArray
    private val scale: FloatArray

    init {

        val modelBytes = context.assets.open("pitch_model.onnx").readBytes()
        session = env.createSession(modelBytes)
        Log.i("ONNX", "✅ Model ONNX dimuat")


        val json = context.assets.open("scaler_params.json")
            .bufferedReader().use { it.readText() }

        val scaler = Gson().fromJson(json, ScalerParams::class.java)
        mean = scaler.mean.map { it.toFloat() }.toFloatArray()
        scale = scaler.scale.map { it.toFloat() }.toFloatArray()

        Log.i("ONNX", "Scaler dimuat")
    }

    fun predictPitch(
        acX: Float,
        acY: Float,
        acZ: Float,
        gyX: Float,
        gyY: Float,
        gyZ: Float
    ): Float {


        val input = floatArrayOf(acX, acY, acZ, gyX, gyY, gyZ)

        Log.i("ONNX", "Input = ${input.toList()}")

        val normalized = FloatArray(6)
        for (i in 0..5) {
            normalized[i] = (input[i] - mean[i]) / scale[i]
        }

        Log.i("ONNX", "Normalized = ${normalized.toList()}")


        val inputName = session.inputNames.first()
        val buffer = FloatBuffer.wrap(normalized)
        val tensor = OnnxTensor.createTensor(env, buffer, longArrayOf(1, 6))


        val result = session.run(mapOf(inputName to tensor))

        val output = (result[0].value as Array<FloatArray>)[0][0]

        result.close()
        tensor.close()

        Log.i("ONNX", "✅ OUTPUT = $output")

        return output
    }

    fun close() {
        session.close()
        env.close()
    }

    data class ScalerParams(
        val mean: List<Double>,
        val scale: List<Double>
    )
}
