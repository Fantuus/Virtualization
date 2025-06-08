package com.mygame.in_3d_world

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3

class CameraController(var speed_move_camera: Float, var speed_rotation_camera: Float) {

    fun move_camera(move_direction: String, distance_delta: Float = speed_move_camera) {
        val direction = AppContext.camera.direction.cpy().nor()
        direction.y = 0f
        direction.nor()
        val old_pos = AppContext.camera.position.cpy()
        if (move_direction == MoveDirections.FORWARD.value) {
            val distance_step_forward = direction.scl(distance_delta)
            AppContext.camera.position.add(distance_step_forward)
        } else if (move_direction == MoveDirections.BACKWARD.value) {
            val distance_step_backward = direction.scl(-distance_delta)
            AppContext.camera.position.add(distance_step_backward)
        }
        if (AppContext.collisionManager.checkCollision(AppContext.camera.position)) {
            AppContext.camera.position.set(old_pos)
        }
        AppContext.camera.update()
    }

    fun rotate_camera(direction: String, angle_delta: Float = speed_rotation_camera) {
        val localX: Vector3
        val quatX: Quaternion
        val quatY: Quaternion
        when (direction) {
            RotationDirections.UP.value -> {
                localX = Vector3(AppContext.camera.direction).crs(AppContext.camera.up).nor()
                quatX = Quaternion().setFromAxisRad(localX, MathUtils.degreesToRadians * angle_delta)
                AppContext.camera.rotate(quatX)
            }
            RotationDirections.DOWN.value -> {
                localX = Vector3(AppContext.camera.direction).crs(AppContext.camera.up).nor()
                quatX = Quaternion().setFromAxisRad(localX, MathUtils.degreesToRadians * -angle_delta)
                AppContext.camera.rotate(quatX)
            }
            RotationDirections.LEFT.value -> {
                quatY = Quaternion().setFromAxisRad(Vector3.Y, MathUtils.degreesToRadians * angle_delta)
                AppContext.camera.rotate(quatY)
            }
            RotationDirections.RIGHT.value -> {
                quatY = Quaternion().setFromAxisRad(Vector3.Y, MathUtils.degreesToRadians * -angle_delta)
                AppContext.camera.rotate(quatY)
            }
        }
        AppContext.camera.update()
    }
}


enum class RotationDirections(val value: String) {
    UP("UP"),
    DOWN("DOWN"),
    LEFT("LEFT"),
    RIGHT("RIGHT");
}


enum class MoveDirections(val value: String) {
    FORWARD("FORWARD"),
    BACKWARD("BACKWARD"),
    STOP("STOP");
}
