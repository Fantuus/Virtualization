package com.mygame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import net.mgsx.gltf.loaders.gltf.GLTFLoader
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.scene.SceneManager
import net.mgsx.gltf.scene3d.scene.SceneSkybox
import net.mgsx.gltf.scene3d.utils.IBLBuilder
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.viewport.ScreenViewport
/**
 * Реализация {@link com.badlogic.gdx.ApplicationListener},
 * общая для всех платформ.
 */
class Main : ApplicationAdapter() {
    private var sceneManager: SceneManager? = null
    private var sceneAsset: SceneAsset? = null
    private var scene: Scene? = null
    private var camera: PerspectiveCamera? = null
    private var сamera_сontroller: CameraController? = null
    private var diffuseCubemap: Cubemap? = null
    private var environmentCubemap: Cubemap? = null
    private var specularCubemap: Cubemap? = null
    private var brdfLUT: Texture? = null
    private var time = 0f
    private var skybox: SceneSkybox? = null
    private var light: DirectionalLightEx? = null

    private var stage: Stage? = null

    private val speed_rotation_camera = 10f
    private val speed_move_camera = 0.04f


    override fun create() {

        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        // create scene





        val sceneAsset = GLTFLoader().load(Gdx.files.internal("models/worktable/worktable.gltf"))
        val scene = Scene(sceneAsset.scene)


        sceneManager = SceneManager()
        sceneManager!!.addScene(scene)


        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera!!.near = 0.1f // Минимальное расстояние, которое видит камера
        camera!!.far = 50f  // Максимальное расстояние (достаточно для вашей модели)
        camera!!.position.set(0.02f, 2.5f, 2.2f)
        camera!!.update()
        sceneManager!!.setCamera(camera)

        сamera_сontroller = CameraController(camera, speed_move_camera, speed_rotation_camera)


//         setup light
        light = DirectionalLightEx()
        light!!.direction.set(1f, -3f, 1f).nor()
        light!!.color.set(Color.WHITE)
        sceneManager!!.environment.add(light)


        // setup quick IBL (image based lighting)
        val iblBuilder = IBLBuilder.createOutdoor(light)
        environmentCubemap = iblBuilder.buildEnvMap(1024)
        diffuseCubemap = iblBuilder.buildIrradianceMap(256)
        specularCubemap = iblBuilder.buildRadianceMap(10)
        iblBuilder.dispose()


        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"))

        sceneManager!!.setAmbientLight(1f)
        sceneManager!!.environment.set(
            PBRTextureAttribute(
                PBRTextureAttribute.BRDFLUTTexture,
                brdfLUT
            )
        )
        sceneManager!!.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap))
        sceneManager!!.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap))


        // setup skybox
        skybox = SceneSkybox(environmentCubemap)
        sceneManager!!.skyBox = skybox



        var button_creator = ButtonCreator(сamera_сontroller, stage)
        button_creator.create_label()




        val row_height = Gdx.graphics.width / 12
        val col_width = Gdx.graphics.width / 12
        val mySkin = Skin(Gdx.files.internal("skin/glassy-ui.json"))


        val button_rotate_up: Button = TextButton("Text Button", mySkin, "small")
        button_rotate_up.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_up.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 3).toFloat()
        )
        button_rotate_up.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.rotate_camera(RotationDirections.UP.value)
                button_creator.print_to_label("Press rotate_up")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                button_creator.print_to_label("Pressed rotate_up")
                return true
            }
        })
        stage!!.addActor(button_rotate_up)


        val button_rotate_right: Button = TextButton("Text Button", mySkin, "small")
        button_rotate_right.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_right.setPosition(
            (col_width * 3).toFloat(),
            (Gdx.graphics.height - row_height * 4).toFloat()
        )
        button_rotate_right.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.rotate_camera(RotationDirections.RIGHT.value)
                button_creator.print_to_label("Press rotate_right")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                button_creator.print_to_label("Pressed rotate_right")
                return true
            }
        })
        stage!!.addActor(button_rotate_right)


        val button_rotate_down: Button = TextButton("Text Button", mySkin, "small")
        button_rotate_down.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_down.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 5).toFloat()
        )
        button_rotate_down.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.rotate_camera(RotationDirections.DOWN.value)
                button_creator.print_to_label("Press rotate_down")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                button_creator.print_to_label("Pressed rotate_down")
                return true
            }
        })
        stage!!.addActor(button_rotate_down)


        val button_rotate_left: Button = TextButton("Text Button", mySkin, "small")
        button_rotate_left.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_left.setPosition(
            (col_width * 1).toFloat(),
            (Gdx.graphics.height - row_height * 4).toFloat()
        )
        button_rotate_left.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.rotate_camera(RotationDirections.LEFT.value)
                button_creator.print_to_label("Press rotate_left")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                button_creator.print_to_label("Pressed rotate_left")
                return true
            }
        })
        stage!!.addActor(button_rotate_left)





        val button_move_foward: Button = TextButton("Text Button", mySkin, "small")
        button_move_foward.setSize((col_width).toFloat(), row_height.toFloat())
        button_move_foward.setPosition(
            (col_width * 9).toFloat(),
            (Gdx.graphics.height - row_height * 3).toFloat()
        )
        button_move_foward.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.move_camera(MoveDirections.FORWARD.value)
                button_creator.print_to_label("Press move_foward")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                button_creator.print_to_label("Pressed move_foward")
                return true
            }
        })
        stage!!.addActor(button_move_foward)


        val button_move_backward: Button = TextButton("Text Button", mySkin, "small")
        button_move_backward.setSize((col_width).toFloat(), row_height.toFloat())
        button_move_backward.setPosition(
            (col_width * 9).toFloat(),
            (Gdx.graphics.height - row_height * 5).toFloat()
        )
        button_move_backward.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.move_camera(MoveDirections.BACKWARD.value)
                button_creator.print_to_label("Press move_backward")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                button_creator.print_to_label("Pressed move_backward")
                return true
            }
        })
        stage!!.addActor(button_move_backward)

    }


    override fun resize(width: Int, height: Int) {
        sceneManager!!.updateViewport(width.toFloat(), height.toFloat())
    }

    override fun render() {
        val deltaTime = Gdx.graphics.deltaTime
        time += deltaTime

        camera!!.up.set(Vector3.Y)
        camera!!.update()

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        sceneManager!!.update(deltaTime)
        sceneManager!!.render()

        stage!!.act()
        stage!!.draw()
    }

    override fun dispose() {
        sceneManager!!.dispose()
        sceneAsset!!.dispose()
        environmentCubemap!!.dispose()
        diffuseCubemap!!.dispose()
        specularCubemap!!.dispose()
        brdfLUT!!.dispose()
        skybox!!.dispose()
    }
}

class CameraController(var camera: PerspectiveCamera?, var speed_move_camera: Float, var speed_rotation_camera: Float ) {

    fun move_camera(move_direction: String, distance_delta: Float = speed_move_camera) {
        val direction = camera!!.direction.cpy().nor()
        direction.y = 0f
        direction.nor()
        if (move_direction == MoveDirections.FORWARD.value) {
            camera!!.position.add(direction.scl(distance_delta))
        } else if (move_direction == MoveDirections.BACKWARD.value) {
            camera!!.position.add(direction.scl(-distance_delta))
        }
        camera!!.update()
    }

    fun rotate_camera(direction: String, angle_delta:Float = speed_rotation_camera) {
        val localX: Vector3
        val quatX: Quaternion
        val quatY: Quaternion
        if (direction == RotationDirections.UP.value) {
            localX = Vector3(camera!!.direction).crs(camera!!.up).nor()
            quatX = Quaternion().setFromAxisRad(localX, MathUtils.degreesToRadians * angle_delta)
            camera!!.rotate(quatX)
        } else if (direction == RotationDirections.DOWN.value) {
            localX = Vector3(camera!!.direction).crs(camera!!.up).nor()
            quatX = Quaternion().setFromAxisRad(localX, MathUtils.degreesToRadians * -angle_delta)
            camera!!.rotate(quatX)
        } else if (direction == RotationDirections.LEFT.value) {
            quatY = Quaternion().setFromAxisRad(Vector3.Y, MathUtils.degreesToRadians * angle_delta)
            camera!!.rotate(quatY)
        } else if (direction == RotationDirections.RIGHT.value) {
            quatY = Quaternion().setFromAxisRad(Vector3.Y, MathUtils.degreesToRadians * -angle_delta)
            camera!!.rotate(quatY)
        }
        camera!!.update()
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
    BACKWARD("BACKWARD");
}




class ButtonCreator(val сamera_сontroller: CameraController?, val stage: Stage?) {
    val row_height = Gdx.graphics.width / 12
    val col_width = Gdx.graphics.width / 12
    val mySkin = Skin(Gdx.files.internal("skin/glassy-ui.json"))
    private var outputLabel: Label? = null

    fun create_label() {
        outputLabel = Label("Press a Button", mySkin, "black")
        outputLabel!!.setSize(Gdx.graphics.width.toFloat(), row_height.toFloat())
        outputLabel!!.setPosition(0f, row_height.toFloat()*8)
        stage!!.addActor(outputLabel)
    }

    fun print_to_label(text: String) {
        outputLabel!!.setText("Pressed $text")
    }
}
