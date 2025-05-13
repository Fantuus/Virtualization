package com.mygame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
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
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.viewport.ScreenViewport
import kotlin.math.abs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Реализация {@link com.badlogic.gdx.ApplicationListener},
 * общая для всех платформ.
 */
class Main(private val sensorProvider: SensorProvider) : ApplicationAdapter() {
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


    var threshold: Threshold? = null
    var sensitivity: Sensitivity? = null


    private val speed_rotation_camera_by_button = 25f
    private val speed_move_camera_by_button = 0.4f

    private val speed_rotation_camera_by_sensor = 0.6f
    private val speed_move_camera_by_sensor = 0.01f


    private var button_creator: ButtonCreator? = null

    private var real_move_direction = MoveDirections.STOP.value
    private var isMoving_old = false

    var triggers: Triggers? = null

    override fun create() {
        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage
        // create scene
        val sceneAsset = GLTFLoader().load(Gdx.files.internal("models/worktable/worktable.gltf"))
        scene = Scene(sceneAsset.scene)









        sceneManager = SceneManager()
        sceneManager!!.addScene(scene)
        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera!!.near = 0.1f // Минимальное расстояние, которое видит камера
        camera!!.far = 50f  // Максимальное расстояние (достаточно для вашей модели)
        camera!!.position.set(-0.2f, 1.6f, 0.5f)
        camera!!.update()
        sceneManager!!.setCamera(camera)
        сamera_сontroller = CameraController(camera, speed_move_camera_by_button, speed_rotation_camera_by_button)

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


        threshold= Threshold(sensorProvider)
        sensitivity = Sensitivity(threshold, speed_rotation_camera_by_button, speed_move_camera_by_button, speed_rotation_camera_by_sensor, speed_move_camera_by_sensor)

        button_creator = ButtonCreator(сamera_сontroller, stage, sensitivity, scene!!)
        button_creator!!.create_label()
        button_creator!!.create_button_rotation_up()
        button_creator!!.create_button_rotation_right()
        button_creator!!.create_button_rotation_down()
        button_creator!!.create_button_rotation_left()
        button_creator!!.create_button_move_forward()
        button_creator!!.create_button_move_backward()

        button_creator!!.create_button_threshholt_gyro_minus()
        button_creator!!.create_button_threshholt_gyro_plus()
        button_creator!!.create_button_speed_rotation_camera_by_sensor_minus()
        button_creator!!.create_button_speed_rotation_camera_by_sensor_plus()


        triggers = Triggers(scene)
        triggers!!.parse_gltf()
        triggers!!.find_animations()
        triggers!!.find_triggers_zone()
        triggers!!.create_bounding_boxes()
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


        triggers!!.check_and_start_animations(camera!!.position)

        if (sensorProvider.isXRotating) {
            if (sensorProvider.rotationX > 0f) {
                сamera_сontroller!!.rotate_camera(RotationDirections.LEFT.value, sensorProvider.rotationX)
            }
            else if (sensorProvider.rotationX < 0f) {
                сamera_сontroller!!.rotate_camera(RotationDirections.RIGHT.value, abs(sensorProvider.rotationX))
            }
            button_creator!!.print_to_label(sensorProvider.rotationX.toString())
        }

        if (sensorProvider.isYRotating) {
            if (sensorProvider.rotationY > 0f) {
                сamera_сontroller!!.rotate_camera(RotationDirections.DOWN.value, sensorProvider.rotationY)
            }
            else if (sensorProvider.rotationY < 0f) {
                сamera_сontroller!!.rotate_camera(RotationDirections.UP.value, abs(sensorProvider.rotationY))
            }
        }

        if (!sensorProvider.isMoving) {
            real_move_direction = MoveDirections.STOP.value
        }
        if (sensorProvider.isMoving && !isMoving_old && !sensorProvider.isXRotating && !sensorProvider.isYRotating) {
            if (sensorProvider.accelZBuffer > 0f ) {
                real_move_direction = MoveDirections.FORWARD.value
            }
            else if (sensorProvider.accelZBuffer < 0f ) {
                real_move_direction = MoveDirections.BACKWARD.value
            }
        }

        if (sensorProvider.isMoving) {
            if (real_move_direction == MoveDirections.FORWARD.value) {
                сamera_сontroller!!.move_camera(MoveDirections.FORWARD.value, sensorProvider.MovingZ)
            }
            else if (real_move_direction == MoveDirections.BACKWARD.value) {
                сamera_сontroller!!.move_camera(MoveDirections.BACKWARD.value, abs(sensorProvider.MovingZ)/6f)
            }
        }
        button_creator!!.print_to_label("f-s-b: ${real_move_direction}    speed: ${sensorProvider.MovingZ}")

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
    BACKWARD("BACKWARD"),
    STOP("STOP");
}




class ButtonCreator(val сamera_сontroller: CameraController?, val stage: Stage?, val sensitivity: Sensitivity?, var scene: Scene): ApplicationAdapter(),
    AnimationController.AnimationListener  {
    val row_height = Gdx.graphics.width / 12
    val col_width = Gdx.graphics.width / 12
    val mySkin = Skin(Gdx.files.internal("skin/glassy-ui.json"))
    private var outputLabel: Label? = null

    override fun onEnd(animation: AnimationController.AnimationDesc?) {
        // Вызывается, когда анимация закончилась
    }

    override fun onLoop(animation: AnimationController.AnimationDesc?) {
        // Вызывается, когда циклическая анимация повторяется
    }
    fun create_label() {
        outputLabel = Label("Press a Button", mySkin, "black")
        outputLabel!!.setSize(Gdx.graphics.width.toFloat(), row_height.toFloat())
        outputLabel!!.setPosition(0f, row_height.toFloat()*4)
        outputLabel!!.setFontScale(2f)
        stage!!.addActor(outputLabel)
    }

    fun print_to_label(text: String) {
        outputLabel!!.setText(text)
    }

    fun create_button_rotation_up() {
        val button_rotate_up: Button = TextButton("Text Button", mySkin, "small")
        button_rotate_up.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_up.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 3).toFloat()
        )
        button_rotate_up.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.rotate_camera(RotationDirections.UP.value)
                print_to_label("Press rotate_up")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed rotate_up")
                return true
            }
        })
        stage!!.addActor(button_rotate_up)
    }


    fun create_button_rotation_right()  {
        val button_rotate_right: Button = TextButton("Text Button", mySkin, "small")
        button_rotate_right.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_right.setPosition(
            (col_width * 3).toFloat(),
            (Gdx.graphics.height - row_height * 4).toFloat()
        )
        button_rotate_right.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.rotate_camera(RotationDirections.RIGHT.value)
                print_to_label("Press rotate_right")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed rotate_right")
                return true
            }
        })
        stage!!.addActor(button_rotate_right)
    }


    fun create_button_rotation_down()  {
        val button_rotate_down: Button = TextButton("Text Button", mySkin, "small")
        button_rotate_down.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_down.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 5).toFloat()
        )
        button_rotate_down.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.rotate_camera(RotationDirections.DOWN.value)
                print_to_label("Press rotate_down")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed rotate_down")
                return true
            }
        })
        stage!!.addActor(button_rotate_down)

    }


    fun create_button_rotation_left()  {
        val button_rotate_left: Button = TextButton("Text Button", mySkin, "small")
        button_rotate_left.setSize((col_width).toFloat(), row_height.toFloat())
        button_rotate_left.setPosition(
            (col_width * 1).toFloat(),
            (Gdx.graphics.height - row_height * 4).toFloat()
        )
        button_rotate_left.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.rotate_camera(RotationDirections.LEFT.value)
                print_to_label("Press rotate_left")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed rotate_left")
                return true
            }
        })
        stage!!.addActor(button_rotate_left)
    }


    fun create_button_move_forward() {
        val button_move_foward: Button = TextButton("Text Button", mySkin, "small")
        button_move_foward.setSize((col_width).toFloat(), row_height.toFloat())
        button_move_foward.setPosition(
            (col_width * 9).toFloat(),
            (Gdx.graphics.height - row_height * 3).toFloat()
        )
        button_move_foward.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.move_camera(MoveDirections.FORWARD.value)
                print_to_label("Press move_foward")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed move_foward")
                return true
            }
        })
        stage!!.addActor(button_move_foward)
    }

    fun create_button_move_backward() {
        val button_move_backward: Button = TextButton("Text Button", mySkin, "small")
        button_move_backward.setSize((col_width).toFloat(), row_height.toFloat())
        button_move_backward.setPosition(
            (col_width * 9).toFloat(),
            (Gdx.graphics.height - row_height * 5).toFloat()
        )
        button_move_backward.addListener(object : InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                сamera_сontroller!!.move_camera(MoveDirections.BACKWARD.value)
                print_to_label("Press move_backward")
            }
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                print_to_label("Pressed move_backward")
                return true
            }
        })
        stage!!.addActor(button_move_backward)
    }




    fun create_button_threshholt_gyro_minus() {
        val button_rotate_up: Button = TextButton("T -", mySkin, "small")
        button_rotate_up.setSize((col_width).toFloat()/2, row_height.toFloat()/2)
        button_rotate_up.setPosition(
            (col_width * 1).toFloat(),
            (Gdx.graphics.height - row_height * 1).toFloat()
        )
        button_rotate_up.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                val new_threshold_gyroXYZ = sensitivity!!.threshold!!.sensorProvider.threshold_gyroXYZ - 0.01f
                sensitivity!!.threshold!!.change_threshold(new_threshold_gyroXYZ)
                print_to_label("threshold_gyroXYZ: ${sensitivity.threshold!!.sensorProvider.threshold_gyroXYZ}")
                return true
            }
        })
        stage!!.addActor(button_rotate_up)
    }

    fun create_button_threshholt_gyro_plus() {
        val button_rotate_up: Button = TextButton("T +", mySkin, "small")
        button_rotate_up.setSize((col_width).toFloat()/2, row_height.toFloat()/2)
        button_rotate_up.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 1).toFloat()
        )
        button_rotate_up.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                val new_threshold_gyroXYZ = sensitivity!!.threshold!!.sensorProvider.threshold_gyroXYZ + 0.01f
                sensitivity.threshold!!.change_threshold(new_threshold_gyroXYZ)
                print_to_label("threshold_gyroXYZ: ${sensitivity.threshold!!.sensorProvider.threshold_gyroXYZ}")
                return true
            }
        })
        stage!!.addActor(button_rotate_up)
    }

    fun create_button_speed_rotation_camera_by_sensor_minus() {
        val button_rotate_up: Button = TextButton("R -", mySkin, "small")
        button_rotate_up.setSize((col_width).toFloat()/2, row_height.toFloat()/2)
        button_rotate_up.setPosition(
            (col_width * 1).toFloat(),
            (Gdx.graphics.height - row_height * 2).toFloat()
        )
        button_rotate_up.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                sensitivity!!.speed_rotation_camera_by_sensor -= 0.1f
                print_to_label("rotation_by_sensor: ${sensitivity!!.speed_rotation_camera_by_sensor}")
                return true
            }
        })
        stage!!.addActor(button_rotate_up)
    }

    fun create_button_speed_rotation_camera_by_sensor_plus() {
        val button_rotate_up: Button = TextButton("R +", mySkin, "small")
        button_rotate_up.setSize((col_width).toFloat()/2, row_height.toFloat()/2)
        button_rotate_up.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 2).toFloat()
        )
        button_rotate_up.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                sensitivity!!.speed_rotation_camera_by_sensor += 0.1f
                print_to_label("rotation_by_sensor: ${sensitivity!!.speed_rotation_camera_by_sensor}")
                return true
            }
        })
        stage!!.addActor(button_rotate_up)
    }
}


class Sensitivity(var threshold: Threshold?, var speed_rotation_camera_by_button: Float, var speed_move_camera_by_button: Float, var speed_rotation_camera_by_sensor: Float, var speed_move_camera_by_sensor: Float)

class Threshold(var sensorProvider: SensorProvider) {

    fun change_threshold(new_val: Float) {
        sensorProvider.threshold_gyroXYZ = new_val
    }
}


class Triggers(var scene: Scene?): AnimationController.AnimationListener {
    var map: HashMap<String, Any>? = null
    val animationNames = mutableListOf<String>()
    val trigger_zones = mutableListOf<String>()
    private val zoneBoundsList = mutableListOf<BoundingBox>()

    fun parse_gltf() {
        val fileHandle = Gdx.files.internal("models/worktable/worktable.gltf")
        val jsonString = fileHandle.readString()
        val gson = Gson()
        map = gson.fromJson(jsonString, object : TypeToken<HashMap<String, Any>>() {}.type) as HashMap<String, Any>?
        Gdx.app.log("map", "$map")
    }

    fun find_animations() {
        val animationsJsonArray = map?.get("animations") as? List<*>
        animationsJsonArray?.forEach { item ->
            val animationMap = item as? Map<*, *> ?: return@forEach
            val name = animationMap["name"] as? String ?: return@forEach
            animationNames.add(name)
        }
        animationNames.sort()
        Gdx.app.log("animationNames", "$animationNames")
    }

    fun find_triggers_zone() {
        val nodesJsonArray = map?.get("nodes") as? List<*>
        nodesJsonArray?.forEach { item ->
            val nodesMap = item as? Map<*, *> ?: return@forEach
            val name = nodesMap["name"] as? String ?: return@forEach
            if (name.startsWith("zone_", ignoreCase = false)) {
                trigger_zones.add(name)
            }
        }
        trigger_zones.sort()
        Gdx.app.log("trigger_zones", "$trigger_zones")
    }

    fun create_bounding_boxes() {
        for (zoneName in trigger_zones) {
            val zoneNode = scene!!.modelInstance.getNode(zoneName)
            if (zoneNode != null) {
                val bounds = BoundingBox()
                zoneNode.calculateBoundingBox(bounds)
                zoneBoundsList.add(bounds)
            }
        }
        Gdx.app.log("zoneBoundsList", "$zoneBoundsList")
    }

    fun check_and_start_animations(cameraPos: Vector3) {
        for (i in 0 until zoneBoundsList.size) {
            if (zoneBoundsList[i].contains(cameraPos)) {
                scene!!.animationController.action(animationNames[i], 1, 1f, this, 0f)

            }
        }
    }
    override fun onEnd(animation: AnimationController.AnimationDesc?) {
        // Вызывается, когда анимация закончилась
    }

    override fun onLoop(animation: AnimationController.AnimationDesc?) {
        // Вызывается, когда циклическая анимация повторяется
    }
}
