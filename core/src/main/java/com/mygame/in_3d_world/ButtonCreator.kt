package com.mygame.in_3d_world

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.mygame.MyGame
import com.mygame.main_menu.MainMenuScreen

class ButtonCreator(): ApplicationAdapter(),
    AnimationController.AnimationListener  {
    val row_height = Gdx.graphics.width / 12
    val col_width = Gdx.graphics.width / 12
    val path_to_button_skin = "skin/glassy-ui.json"
    val mySkin = Skin(Gdx.files.internal(path_to_button_skin))
    private var outputLabel: Label? = null

    override fun onEnd(animation: AnimationController.AnimationDesc?) { }

    override fun onLoop(animation: AnimationController.AnimationDesc?) { }

    fun create_all_ui() {
        create_label()
        create_button_rotation_up()
        create_button_rotation_right()
        create_button_rotation_down()
        create_button_rotation_left()
        create_button_move_forward()
        create_button_move_backward()
        create_button_go_home()
        create_button_teleport_to_spawn()
    }

    private fun create_label() {
        outputLabel = Label("Press a Button", mySkin, "black")
        outputLabel!!.setSize(Gdx.graphics.width.toFloat(), row_height.toFloat())
        outputLabel!!.setPosition(0f, row_height.toFloat()*4)
        outputLabel!!.setFontScale(2f)
        AppContext.stage.addActor(outputLabel)
    }

    fun print_to_label(text: String) {
        outputLabel!!.setText(text)
    }

    private fun create_button_rotation_up() {
        val button_rotate_up: Button = TextButton("", mySkin, "small")
        val texture = Texture(Gdx.files.internal("ui/menu/rotate_up.png"))
        val image = Image(TextureRegionDrawable(TextureRegion(texture)))
        button_rotate_up.add(image)
        button_rotate_up.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_up.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 3).toFloat()
        )
        button_rotate_up.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                AppContext.cameraController.rotate_camera(RotationDirections.UP.value)
                print_to_label("Press rotate_up")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed rotate_up")
                return true
            }
        })
        AppContext.stage.addActor(button_rotate_up)
    }

    private fun create_button_rotation_right()  {
        val button_rotate_right: Button = TextButton("", mySkin, "small")
        val texture = Texture(Gdx.files.internal("ui/menu/rotate_right.png"))
        val image = Image(TextureRegionDrawable(TextureRegion(texture)))
        button_rotate_right.add(image)
        button_rotate_right.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_right.setPosition(
            (col_width * 3).toFloat(),
            (Gdx.graphics.height - row_height * 4).toFloat()
        )
        button_rotate_right.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                AppContext.cameraController.rotate_camera(RotationDirections.RIGHT.value)
                print_to_label("Press rotate_right")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed rotate_right")
                return true
            }
        })
        AppContext.stage.addActor(button_rotate_right)
    }


    private fun create_button_rotation_down()  {
        val button_rotate_down: Button = TextButton("", mySkin, "small")
        val texture = Texture(Gdx.files.internal("ui/menu/rotate_down.png"))
        val image = Image(TextureRegionDrawable(TextureRegion(texture)))
        button_rotate_down.add(image)
        button_rotate_down.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_down.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 5).toFloat()
        )
        button_rotate_down.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                AppContext.cameraController.rotate_camera(RotationDirections.DOWN.value)
                print_to_label("Press rotate_down")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed rotate_down")
                return true
            }
        })
        AppContext.stage.addActor(button_rotate_down)

    }

    private fun create_button_rotation_left()  {
        val button_rotate_left: Button = TextButton("", mySkin, "small")
        val texture = Texture(Gdx.files.internal("ui/menu/rotate_left.png"))
        val image = Image(TextureRegionDrawable(TextureRegion(texture)))
        button_rotate_left.add(image)
        button_rotate_left.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_left.setPosition(
            (col_width * 1).toFloat(),
            (Gdx.graphics.height - row_height * 4).toFloat()
        )
        button_rotate_left.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                AppContext.cameraController.rotate_camera(RotationDirections.LEFT.value)
                print_to_label("Press rotate_left")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed rotate_left")
                return true
            }
        })
        AppContext.stage.addActor(button_rotate_left)
    }

    private fun create_button_move_forward() {
        val button_move_foward: Button = TextButton("", mySkin, "small")
        val texture = Texture(Gdx.files.internal("ui/menu/move_forward.png"))
        val image = Image(TextureRegionDrawable(TextureRegion(texture)))
        button_move_foward.add(image)
        button_move_foward.setSize((col_width).toFloat(), row_height.toFloat())
        button_move_foward.setPosition(
            (col_width * 9).toFloat(),
            (Gdx.graphics.height - row_height * 3).toFloat()
        )
        button_move_foward.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                AppContext.cameraController.move_camera(MoveDirections.FORWARD.value)
                print_to_label("Press move_foward")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed move_foward")
                return true
            }
        })
        AppContext.stage.addActor(button_move_foward)
    }

    private fun create_button_move_backward() {
        val button_move_backward: Button = TextButton("", mySkin, "small")
        val texture = Texture(Gdx.files.internal("ui/menu/move_backward.png"))
        val image = Image(TextureRegionDrawable(TextureRegion(texture)))
        button_move_backward.add(image)
        button_move_backward.setSize((col_width).toFloat(), row_height.toFloat())
        button_move_backward.setPosition(
            (col_width * 9).toFloat(),
            (Gdx.graphics.height - row_height * 5).toFloat()
        )
        button_move_backward.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                AppContext.cameraController.move_camera(MoveDirections.BACKWARD.value)
                print_to_label("Press move_backward")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed move_backward")
                return true
            }
        })
        AppContext.stage.addActor(button_move_backward)
    }


    private fun create_button_go_home() {
        val texture = Texture(Gdx.files.internal("ui/menu/home.png"))
        val image = Image(TextureRegionDrawable(TextureRegion(texture)))

        val buttonWithImage = TextButton("", mySkin, "small")
        buttonWithImage.add(image)
        buttonWithImage.setPosition(
            (col_width * 11).toFloat(),
            (Gdx.graphics.height - row_height * 1).toFloat())
        buttonWithImage.setSize((col_width).toFloat()/2, row_height.toFloat()/2)

        buttonWithImage.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                val game = Gdx.app.getApplicationListener() as? MyGame
                game?.setScreen(MainMenuScreen(game))
                AppContext.triggers.stopAllAudio()
                return true
            }
        })
        AppContext.stage.addActor(buttonWithImage)
    }


    private fun create_button_teleport_to_spawn() {
        val texture = Texture(Gdx.files.internal("ui/menu/to_spawn.png"))
        val image = Image(TextureRegionDrawable(TextureRegion(texture)))

        val buttonWithImage = TextButton("", mySkin, "small")
        buttonWithImage.add(image)
        buttonWithImage.setPosition(
            (col_width * 10).toFloat(),
            (Gdx.graphics.height - row_height * 1).toFloat())
        buttonWithImage.setSize((col_width).toFloat()/2, row_height.toFloat()/2)

        buttonWithImage.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                AppContext.camera.position.set(-0.2f, 1.6f, 0.5f)
                return true
            }
        })
        AppContext.stage.addActor(buttonWithImage)
    }


    fun show_buttons() {
        AppContext.stage.root.children.forEach { actor ->
            actor.isVisible = true
        }
    }

    fun hide_buttons() {
        AppContext.stage.root.children.forEach { actor ->
            actor.isVisible = false
        }
    }

    override fun dispose() {
        mySkin.dispose()
    }
}
