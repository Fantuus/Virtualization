package com.mygame

interface SensorProvider {
    val rotationX: Float
    val rotationY: Float
    val isMoving: Boolean
    val isXRotating: Boolean
    val isYRotating: Boolean
    val isZMoving: Boolean
    val accelZBuffer: Float
    fun startSensors()
    fun stopSensors()
}
