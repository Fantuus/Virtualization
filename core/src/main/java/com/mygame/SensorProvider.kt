package com.mygame

interface SensorProvider {
    val rotationX: Float
    val rotationY: Float
    val isMoving: Boolean
    val isXRotating: Boolean
    val isYRotating: Boolean
    fun startSensors()
    fun stopSensors()
}
