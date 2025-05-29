package com.mygame

import com.badlogic.gdx.Screen
import com.badlogic.gdx.Gdx

class GameScreen(private val sensorProvider: SensorProvider) : Screen {
    private val main = Main(sensorProvider)

    init {
        main.create()
    }

    override fun render(delta: Float) {
        main.render()
    }

    override fun resize(width: Int, height: Int) {
        main.resize(width, height)
    }

    override fun show() {}

    override fun hide() {}

    override fun pause() {
        main.pause()
    }

    override fun resume() {
        main.resume()
    }

    override fun dispose() {
        main.dispose()
    }
}
