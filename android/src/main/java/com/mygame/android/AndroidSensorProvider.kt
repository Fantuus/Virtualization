package com.mygame.android

import android.content.Context
import com.mygame.in_3d_world.SensorProvider

class AndroidSensorProvider(context: Context) : SensorProvider {
    private val sensorData = SensorDataProvider(context)

    override val rotationX: Float
        get() {

            val bufferCopy = synchronized(sensorData.gyroXBuffer) {
                sensorData.gyroXBuffer.toList()
            }
            val filtered = bufferCopy.filterNotNull()
            val average = if (filtered.isNotEmpty()) {
                filtered.average().toFloat()
            } else {
                0f
            }

            return sensorData.nearest_lower_rotatatin_velocity_by_gyroscope(average, ThresholdType.THRESHOLD_GYRO_XYZ)
        }
    override val rotationY: Float
        get() {
            val bufferCopy = synchronized(sensorData.gyroYBuffer) {
                sensorData.gyroYBuffer.toList()
            }
            val filtered = bufferCopy.filterNotNull()
            val average = if (filtered.isNotEmpty()) {
                filtered.average().toFloat()
            } else {
                0f
            }
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
            val bufferCopy = synchronized(sensorData.accelZBuffer) {
                sensorData.accelZBuffer.toList()
            }
            val filtered = bufferCopy.filterNotNull()
            val average = if (filtered.isNotEmpty()) {
                filtered.average().toFloat()
            } else {
                0f
            }

            return sensorData.nearest_lower_rotatatin_velocity_by_gyroscope(average, ThresholdType.THRESHOLD_ACCELERATION_AXIS)
        }

    override var threshold_gyroXYZ: Float
        get() = sensorData.threshold_gyroXYZ.toFloat()
        set(value) { sensorData.threshold_gyroXYZ = value.toFloat() }


    override val accelZBuffer: Float
        get() {
            val bufferCopy = synchronized(sensorData.accelZBuffer) {
                sensorData.accelZBuffer.toList()
            }
            val filtered = bufferCopy.filterNotNull()
            val average = if (filtered.isNotEmpty()) {
                filtered.average().toFloat()
            } else {
                0f
            }

            return sensorData.nearest_lower_rotatatin_velocity_by_gyroscope(average, ThresholdType.THRESHOLD_ACCELERATION_AXIS)
        }

    override fun startSensors() = sensorData.startListening()
    override fun stopSensors() = sensorData.stopListening()
}
