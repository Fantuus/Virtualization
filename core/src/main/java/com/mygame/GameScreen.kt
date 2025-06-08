package com.mygame

import com.badlogic.gdx.Screen
import com.mygame.in_3d_world.VirtualWorld
import com.mygame.in_3d_world.SensorProvider

class GameScreen(private val sensorProvider: SensorProvider, worldName: String) : Screen {
    private val virtualWorld = VirtualWorld(sensorProvider, worldName)

    init {
        virtualWorld.create()
    }

    override fun render(delta: Float) {
        virtualWorld.render()
    }

    override fun resize(width: Int, height: Int) {
        virtualWorld.resize(width, height)
    }

    override fun show() {}

    override fun hide() {}

    override fun pause() {
        virtualWorld.pause()
    }

    override fun resume() {
        virtualWorld.resume()
    }

    override fun dispose() {
        virtualWorld.dispose()
    }
}
