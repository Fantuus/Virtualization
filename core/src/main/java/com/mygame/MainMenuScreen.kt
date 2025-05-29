package com.mygame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.viewport.ScreenViewport

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener

class MainMenuScreen(private val game: MyGame) : Screen {
    private lateinit var stage: Stage
    private lateinit var skin: Skin

    init {
        skin = Skin(Gdx.files.internal("skin/glassy-ui.json"))
        stage = Stage()
        Gdx.input.inputProcessor = stage
    }

    override fun show() {
        val table = Table()
        table.setFillParent(true)
        val startButton = TextButton("Start", skin)
        val exitButton = TextButton("Exit", skin)

        startButton.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                game.setScreen(GameScreen(game.sensorProvider))
                return true
            }
        })

        exitButton.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                Gdx.app.exit()
                return true
            }
        })

        table.add(startButton).pad(20f).row()
        table.add(exitButton).pad(20f)
        stage.addActor(table)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        stage.dispose()
        skin.dispose()
    }
}
