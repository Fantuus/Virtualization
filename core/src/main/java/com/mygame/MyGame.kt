package com.mygame

import com.badlogic.gdx.Game

class MyGame(val sensorProvider: SensorProvider) : Game() {
    override fun create() {
        setScreen(MainMenuScreen(this))
    }
}
