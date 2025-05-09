package com.mygame.android


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import kotlin.math.*


class SensorDataProvider(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Состояния для данных с датчиков
    private val _gyroscopeData = mutableStateOf("Gyro: X=0.00, Y=0.00, Z=0.00")
    val gyroscopeData: State<String> get() = _gyroscopeData

    private val _significantMotionData = mutableStateOf("Significant Motion: Stopped")
    val significantMotionData: State<String> get() = _significantMotionData

    // Состояния для отображения вращений
    private val _rotationStatus = mutableStateOf("X: Stopped, Y: Stopped, Z: Stopped")
    val rotationStatus: State<String> get() = _rotationStatus

    // Состояния для движения по осям
    private val _movementStatus = mutableStateOf("X: Stopped, Y: Stopped, Z: Stopped")
    val movementStatus: State<String> get() = _movementStatus

    // Флаг для значительного движения
    var isMoving = false

    var isXRotating = false
    var isYRotating = false

    var isZMoving = false




    // Параметры для гироскопа
    private val threshold_gyroXYZ = 0.3f
    private val len_gyroXYZBuffer = 3
    val gyroXBuffer = ArrayDeque<Float>(len_gyroXYZBuffer)
    val gyroYBuffer = ArrayDeque<Float>(len_gyroXYZBuffer)
    private val gyroZBuffer = ArrayDeque<Float>(len_gyroXYZBuffer)

    // Параметры для линейного ускорения
    private val threshold_acceleration = 0.8f
    private val threshold_acceleration_axis = 0.7f
    private val len_accelerationBuffer = 10
    private val accelerationBuffer = ArrayDeque<Float>(len_accelerationBuffer)
    private val accelXBuffer = ArrayDeque<Float>(len_accelerationBuffer)
    private val accelYBuffer = ArrayDeque<Float>(len_accelerationBuffer)
    val accelZBuffer = ArrayDeque<Float>(len_accelerationBuffer)




    fun startListening() {
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val significantMotionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)

        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gravity?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        linearAcceleration?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        significantMotionSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }

    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> handleGyroscopeData(it)
                Sensor.TYPE_LINEAR_ACCELERATION -> handleLinearAccelerationData(it)
            }
        }
    }


    private fun handleGyroscopeData(event: SensorEvent) {
        gyroXBuffer.addLast(event.values[0])
        gyroYBuffer.addLast(event.values[1])
        gyroZBuffer.addLast(event.values[2])
        if (gyroXBuffer.size > len_gyroXYZBuffer) {
            gyroXBuffer.removeFirst()
            gyroYBuffer.removeFirst()
            gyroZBuffer.removeFirst()
        }
        isXRotating = abs(gyroXBuffer.average()) > threshold_gyroXYZ
        isYRotating = abs(gyroYBuffer.average()) > threshold_gyroXYZ
        val isZRotating = abs(gyroZBuffer.average()) > threshold_gyroXYZ
        _rotationStatus.value = "Верх Низ:       ${if (isXRotating) "%.2f".format(gyroXBuffer.average()) else "Stopped"}\n" +
            "Лево Право:  ${if (isYRotating) "%.2f".format(gyroYBuffer.average()) else "Stopped"}"
        _gyroscopeData.value = "X=${"%.2f".format(event.values[0])}, Y=${"%.2f".format(event.values[1])}, Z=${"%.2f".format(event.values[2])}"
    }

    private fun handleLinearAccelerationData(event: SensorEvent) {
        val accelerationMagnitude = sqrt(
            event.values[0].pow(2) + event.values[1].pow(2) + event.values[2].pow(2)
        ).toFloat()
        accelerationBuffer.addLast(accelerationMagnitude)
        if (accelerationBuffer.size > len_accelerationBuffer) accelerationBuffer.removeFirst()
        isMoving = accelerationBuffer.average().toFloat() > threshold_acceleration
        _significantMotionData.value = "\nSteps:  ${if (isMoving) "Moving" else "Stopped"}"

        accelXBuffer.addLast(event.values[0])
        accelYBuffer.addLast(event.values[1])
        accelZBuffer.addLast(event.values[2])
        if (accelXBuffer.size > len_accelerationBuffer) {
            accelXBuffer.removeFirst()
            accelYBuffer.removeFirst()
            accelZBuffer.removeFirst()
        }
        val isXMoving = abs(accelXBuffer.average()) > threshold_acceleration_axis
        val isYMoving = abs(accelYBuffer.average()) > threshold_acceleration_axis
        isZMoving = abs(accelZBuffer.average()) > threshold_acceleration_axis
        _movementStatus.value = "Лево Право:       ${if (isXMoving) "%.2f".format(accelXBuffer.average()) else "Stopped"}\n Верх Низ:           ${if (isYMoving) "%.2f".format(accelYBuffer.average()) else "Stopped"}\n Вперёд Назад:  ${if (isZMoving) "%.2f".format(accelZBuffer.average()) else "Stopped"}"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
