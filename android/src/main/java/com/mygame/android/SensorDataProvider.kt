package com.mygame.android


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
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

    val threshold_gyroXYZ_list =               listOf(0f, 0.1f, 0.15f, 0.2f,  0.3f, 0.45f, 0.7f, 0.9f)
    val speed_rotation_camera_by_sensor_list = listOf(0f, 0.2f, 0.22f, 0.25f, 0.35f, 0.6f, 0.8f, 1.1f)


//    val threshold_accelerationZ_list = listOf(0f, 0.1f, 0.15f, 0.2f,  0.3f, 0.45f, 0.7f, 0.9f, 1.5f, 1.9f, 2.5f, )


    val threshold_acceleration_axis_list =      listOf(0f, 0.05f, 0.1f,   0.15f,  0.2f,  0.3f)
    val speed_move_camera_by_sensor_axis_list = listOf(0f, 0.01f, 0.025f, 0.035f, 0.04f, 0.06f)




    // Параметры для гироскопа
    var threshold_gyroXYZ =threshold_gyroXYZ_list[1]
    private val len_gyroXYZBuffer = 3
    val gyroXBuffer = ArrayDeque<Float>(len_gyroXYZBuffer)
    val gyroYBuffer = ArrayDeque<Float>(len_gyroXYZBuffer)
    private val gyroZBuffer = ArrayDeque<Float>(len_gyroXYZBuffer)

    // Параметры для линейного ускорения
    private val threshold_acceleration = 0.1f
    private val threshold_acceleration_axis = threshold_acceleration_axis_list[1]
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
        }
        if (gyroYBuffer.size > len_gyroXYZBuffer) {
            gyroYBuffer.removeFirst()
        }
        if (gyroZBuffer.size > len_gyroXYZBuffer) {
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


    fun nearest_lower_rotatatin_velocity_by_gyroscope(input: Float, thresholdType: ThresholdType): Float{
        var my_input = input
        var pairs = listOf(0f, 1f).zip(listOf(0f, 1f)).sortedBy { it.first }
        if (thresholdType == ThresholdType.THRESHOLD_GYRO_XYZ) {
            pairs = threshold_gyroXYZ_list.zip(speed_rotation_camera_by_sensor_list).sortedBy { it.first }
        }
        else if (thresholdType == ThresholdType.THRESHOLD_ACCELERATION_AXIS) {
            pairs = threshold_acceleration_axis_list.zip(speed_move_camera_by_sensor_axis_list).sortedBy { it.first }
        }
        else {
            return 0f
        }
        var is_positive = true
        if (my_input < 0f) {
            is_positive = false
            my_input *= -1f
        }
        var speed_rotation_camera_by_sensor = pairs
            .filter { it.first <= my_input }
            .maxByOrNull { it.first } !!
            .second
        if (!is_positive) {
            speed_rotation_camera_by_sensor *= -1f
        }
        return speed_rotation_camera_by_sensor
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}



enum class ThresholdType(val value: String) {
    THRESHOLD_GYRO_XYZ("THRESHOLD_GYRO_XYZ"),
//    THRESHOLD_ACCELERATION("THRESHOLD_ACCELERATION"),
    THRESHOLD_ACCELERATION_AXIS("THRESHOLD_ACCELERATION_AXIS");
}
