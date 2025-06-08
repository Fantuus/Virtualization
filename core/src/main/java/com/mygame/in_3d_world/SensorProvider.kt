package com.mygame.in_3d_world

interface SensorProvider {
    val rotationX: Float
    val rotationY: Float
    val isMoving: Boolean
    val isXRotating: Boolean
    val isYRotating: Boolean
    val isZMoving: Boolean
    val MovingZ: Float
    val accelZBuffer: Float
    var threshold_gyroXYZ: Float
    fun startSensors()
    fun stopSensors()
}
