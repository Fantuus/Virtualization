package com.mygame.android

import android.content.Context
import com.mygame.SensorProvider

class AndroidSensorProvider(context: Context) : SensorProvider {
    private val sensorData = SensorDataProvider(context)

    override val rotationX: Float
        get() = sensorData.nearest_lower_rotatatin_velocity_by_gyroscope(sensorData.gyroXBuffer.average().toFloat())
//        get() = sensorData.gyroXBuffer.average().toFloat()

    override val rotationY: Float
        get() = sensorData.gyroYBuffer.average().toFloat()

    override val isMoving: Boolean
        get() = sensorData.isMoving

    override val isXRotating: Boolean
        get() = sensorData.isXRotating

    override val isYRotating: Boolean
        get() = sensorData.isYRotating


    override val isZMoving: Boolean
        get() = sensorData.isZMoving

    override var threshold_gyroXYZ: Float
        get() = sensorData.threshold_gyroXYZ.toFloat()
        set(value) { sensorData.threshold_gyroXYZ = value.toFloat() }




    override val accelZBuffer: Float
        get() = sensorData.accelZBuffer.average().toFloat()

    override fun startSensors() = sensorData.startListening()
    override fun stopSensors() = sensorData.stopListening()
}
