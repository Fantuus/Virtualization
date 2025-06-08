package com.mygame

import com.badlogic.gdx.Game
import com.mygame.in_3d_world.SensorProvider
import com.mygame.main_menu.MainMenuScreen

class MyGame(val sensorProvider: SensorProvider) : Game() {
    override fun create() {
        setScreen(MainMenuScreen(this))
    }
}
