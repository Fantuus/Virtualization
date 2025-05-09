package com.mygame.android

import android.content.Context
import com.mygame.SensorProvider

class AndroidSensorProvider(context: Context) : SensorProvider {
    private val sensorData = SensorDataProvider(context)

    override val rotationX: Float
        get() = sensorData.gyroXBuffer.average().toFloat()

    override val rotationY: Float
        get() = sensorData.gyroYBuffer.average().toFloat()

    override val isMoving: Boolean
        get() = sensorData.isMoving

    override val isXRotating: Boolean
        get() = sensorData.isXRotating

    override val isYRotating: Boolean
        get() = sensorData.isYRotating

    override fun startSensors() = sensorData.startListening()
    override fun stopSensors() = sensorData.stopListening()
}
