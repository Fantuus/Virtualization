package com.mygame.android

import android.content.Context
import com.mygame.SensorProvider

class AndroidSensorProvider(context: Context) : SensorProvider {
    private val sensorData = SensorDataProvider(context)

    override val rotationX: Float
        get() {
            val average = sensorData.gyroXBuffer
                .filterNotNull()
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toFloat() ?: 0f

            return sensorData.nearest_lower_rotatatin_velocity_by_gyroscope(average, ThresholdType.THRESHOLD_GYRO_XYZ)
        }
    override val rotationY: Float
        get() {
            val average = sensorData.gyroYBuffer
                .filterNotNull()
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toFloat() ?: 0f
            return sensorData.nearest_lower_rotatatin_velocity_by_gyroscope(average, ThresholdType.THRESHOLD_GYRO_XYZ)
        }
    override val isMoving: Boolean
        get() = sensorData.isMoving

    override val isXRotating: Boolean
        get() = sensorData.isXRotating

    override val isYRotating: Boolean
        get() = sensorData.isYRotating


    override val isZMoving: Boolean
        get() = sensorData.isZMoving

    override val MovingZ: Float
        get() {
            val average = sensorData.accelZBuffer
                .filterNotNull()
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toFloat() ?: 0f
            return sensorData.nearest_lower_rotatatin_velocity_by_gyroscope(average, ThresholdType.THRESHOLD_ACCELERATION_AXIS)
        }

    override var threshold_gyroXYZ: Float
        get() = sensorData.threshold_gyroXYZ.toFloat()
        set(value) { sensorData.threshold_gyroXYZ = value.toFloat() }


    override val accelZBuffer: Float
        get() {
            val average = sensorData.accelZBuffer
                .filterNotNull()
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toFloat() ?: 0f
            return sensorData.nearest_lower_rotatatin_velocity_by_gyroscope(average, ThresholdType.THRESHOLD_ACCELERATION_AXIS)
        }

    override fun startSensors() = sensorData.startListening()
    override fun stopSensors() = sensorData.stopListening()
}
