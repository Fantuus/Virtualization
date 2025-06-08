package com.mygame

import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox

class CollisionManager(val modelInstance: com.badlogic.gdx.graphics.g3d.ModelInstance) {
    val objectBoundsMap = mutableMapOf<String, BoundingBox>()

    fun loadColliders() {
        traverseSceneGraph(Matrix4(), modelInstance.nodes)
    }

    private fun traverseSceneGraph(parentTransform: Matrix4, nodes: Iterable<Node>) {
        for (node in nodes) {
            if (node.id.startsWith("anim_zone_") || node.id.startsWith("audio_zone_")) continue
            val worldTransform = Matrix4()
            worldTransform.set(parentTransform).mul(node.localTransform)

            val bounds = BoundingBox()
            node.calculateBoundingBox(bounds)
            objectBoundsMap[node.id] = bounds

            if (node.children.iterator().hasNext()) {
                traverseSceneGraph(worldTransform, node.children)
            }
        }
    }

    fun checkCollision(cameraPosition: Vector3): Boolean {
        val cameraBox = createCameraBounds(cameraPosition)
        for ((key, objBounds) in objectBoundsMap) {
            if (cameraBox.intersects(objBounds)) {
                return true
            }
        }
        return false
    }

    private fun createCameraBounds(cameraPosition: Vector3): BoundingBox {
        val radiusAround = 0.1f
        val heightAboveCamera = 0.1f
        val heightUnderCamera = 1.4f
        val min = Vector3(cameraPosition.x - radiusAround, cameraPosition.y - heightUnderCamera, cameraPosition.z - radiusAround)
        val max = Vector3(cameraPosition.x + radiusAround, cameraPosition.y + heightAboveCamera, cameraPosition.z + radiusAround)
        return BoundingBox(min, max)
    }
}
