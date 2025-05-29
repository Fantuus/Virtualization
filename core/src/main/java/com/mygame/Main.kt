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
import com.badlogic.gdx.audio.Sound //
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.graphics.g3d.model.Node
/**
 * Реализация {@link com.badlogic.gdx.ApplicationListener},
 * общая для всех платформ.
 */



object AppContext {
    lateinit var stage: Stage
    lateinit var scene: Scene
    lateinit var camera: PerspectiveCamera
    lateinit var cameraController: CameraController
    lateinit var triggers: Triggers
    lateinit var collisionManager: CollisionManager
}


class Main(private val sensorProvider: SensorProvider) : ApplicationAdapter() {
    private var sceneManager: SceneManager? = null
    private var sceneAsset: SceneAsset? = null
    private var diffuseCubemap: Cubemap? = null
    private var environmentCubemap: Cubemap? = null
    private var specularCubemap: Cubemap? = null
    private var brdfLUT: Texture? = null
    private var time = 0f
    private var skybox: SceneSkybox? = null
    private var light: DirectionalLightEx? = null


    var threshold: Threshold? = null
    var sensitivity: Sensitivity? = null


    private val speed_rotation_camera_by_button = 25f
    private val speed_move_camera_by_button = 0.4f

    private val speed_rotation_camera_by_sensor = 0.6f
    private val speed_move_camera_by_sensor = 0.01f


    private var button_creator: ButtonCreator? = null

    private var real_move_direction = MoveDirections.STOP.value
    private var isMoving_old = false


    override fun create() {
        val worldName = "worktable"
        val path_to_model = "models/$worldName/gltf/$worldName.gltf"
        val path_to_sounds = "sounds/$worldName/"
        AppContext.stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = AppContext.stage
        // create scene
        val sceneAsset = GLTFLoader().load(Gdx.files.internal(path_to_model))
        AppContext.scene = Scene(sceneAsset.scene)


        AppContext.collisionManager = CollisionManager(AppContext.scene.modelInstance)
        AppContext.collisionManager.loadColliders()


        sceneManager = SceneManager()
        sceneManager!!.addScene(AppContext.scene)
        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        AppContext.camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        AppContext.camera.near = 0.1f // Минимальное расстояние, которое видит камера
        AppContext.camera.far = 50f  // Максимальное расстояние (достаточно для вашей модели)
        AppContext.camera.position.set(-0.2f, 1.6f, 0.5f)
        AppContext.camera.update()
        sceneManager!!.setCamera(AppContext.camera)
        AppContext.cameraController = CameraController(speed_move_camera_by_button, speed_rotation_camera_by_button)

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
//        skybox = SceneSkybox(environmentCubemap)
//        sceneManager!!.skyBox = skybox
        val cubemap = Cubemap(
            Gdx.files.internal("textures/environment/environment_posx.png"),
            Gdx.files.internal("textures/environment/environment_negx.png"),
            Gdx.files.internal("textures/environment/environment_posy.png"),
            Gdx.files.internal("textures/environment/environment_negy.png"),
            Gdx.files.internal("textures/environment/environment_posz.png"),
            Gdx.files.internal("textures/environment/environment_negz.png")
        )
        skybox = SceneSkybox(cubemap)
        sceneManager!!.skyBox = skybox


        threshold= Threshold(sensorProvider)
        sensitivity = Sensitivity(threshold, speed_rotation_camera_by_button, speed_move_camera_by_button, speed_rotation_camera_by_sensor, speed_move_camera_by_sensor)

        button_creator = ButtonCreator(sensitivity)
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


        AppContext.triggers = Triggers(path_to_sounds)
        AppContext.triggers.find_animations()
        AppContext.triggers.find_and_load_audios()
        AppContext.triggers.find_triggers_zone()
        AppContext.triggers.create_bounding_boxes()
        AppContext.triggers.createAnimBoundsMap()
        AppContext.triggers.createAudioBoundsMap()
    }


    override fun resize(width: Int, height: Int) {
        sceneManager!!.updateViewport(width.toFloat(), height.toFloat())
    }

    override fun render() {
        val deltaTime = Gdx.graphics.deltaTime
        time += deltaTime

        AppContext.camera.up.set(Vector3.Y)
        AppContext.camera.update()

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        sceneManager!!.update(deltaTime)
        sceneManager!!.render()


        AppContext.triggers.check_and_start_animations(AppContext.camera.position)
        AppContext.triggers.check_and_start_audios(AppContext.camera.position)

        if (sensorProvider.isXRotating) {
            if (sensorProvider.rotationX > 0f) {
                AppContext.cameraController.rotate_camera(RotationDirections.LEFT.value, sensorProvider.rotationX)
            }
            else if (sensorProvider.rotationX < 0f) {
                AppContext.cameraController.rotate_camera(RotationDirections.RIGHT.value, abs(sensorProvider.rotationX))
            }
            button_creator!!.print_to_label(sensorProvider.rotationX.toString())
        }

        if (sensorProvider.isYRotating) {
            if (sensorProvider.rotationY > 0f) {
                AppContext.cameraController.rotate_camera(RotationDirections.DOWN.value, sensorProvider.rotationY)
            }
            else if (sensorProvider.rotationY < 0f) {
                AppContext.cameraController.rotate_camera(RotationDirections.UP.value, abs(sensorProvider.rotationY))
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
                AppContext.cameraController.move_camera(MoveDirections.FORWARD.value, sensorProvider.MovingZ)
            }
            else if (real_move_direction == MoveDirections.BACKWARD.value) {
                AppContext.cameraController.move_camera(MoveDirections.BACKWARD.value, abs(sensorProvider.MovingZ)/6f)
            }
        }
//        button_creator!!.print_to_label("f-s-b: ${real_move_direction}    speed: ${sensorProvider.MovingZ}")

        AppContext.stage.act()
        AppContext.stage.draw()
    }

    override fun dispose() {
        sceneManager!!.dispose()
        sceneAsset!!.dispose()
        environmentCubemap!!.dispose()
        diffuseCubemap!!.dispose()
        specularCubemap!!.dispose()
        brdfLUT!!.dispose()
        skybox!!.dispose()
        button_creator?.dispose()
        AppContext.triggers.dispose()
        AppContext.stage.dispose()
    }

}

class CameraController(var speed_move_camera: Float, var speed_rotation_camera: Float) {

    fun move_camera(move_direction: String, distance_delta: Float = speed_move_camera) {
        val direction = AppContext.camera.direction.cpy().nor()
        direction.y = 0f
        direction.nor()
        val old_pos = AppContext.camera.position.cpy()
        Gdx.app.log("old_pos 1", "${old_pos}")
        if (move_direction == MoveDirections.FORWARD.value) {
            val distance_step_forward = direction.scl(distance_delta)
            AppContext.camera.position.add(distance_step_forward)
        } else if (move_direction == MoveDirections.BACKWARD.value) {
            val distance_step_backward = direction.scl(-distance_delta)
            AppContext.camera.position.add(distance_step_backward)
        }
        Gdx.app.log("camera pos", "${AppContext.camera.position}")
        Gdx.app.log("collision", "${AppContext.collisionManager.checkCollision(AppContext.camera.position)}")
        if (AppContext.collisionManager.checkCollision(AppContext.camera.position)) {
            AppContext.camera.position.set(old_pos)
        }
        Gdx.app.log("old_pos 2", "${old_pos}")
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



class ButtonCreator(val sensitivity: Sensitivity?): ApplicationAdapter(),
    AnimationController.AnimationListener  {
    val row_height = Gdx.graphics.width / 12
    val col_width = Gdx.graphics.width / 12
    val path_to_button_skin = "skin/glassy-ui.json"
    val mySkin = Skin(Gdx.files.internal(path_to_button_skin))
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
        AppContext.stage.addActor(outputLabel)
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


    fun create_button_rotation_right()  {
        val button_rotate_right: Button = TextButton("Text Button", mySkin, "small")
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


    fun create_button_rotation_down()  {
        val button_rotate_down: Button = TextButton("Text Button", mySkin, "small")
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


    fun create_button_rotation_left()  {
        val button_rotate_left: Button = TextButton("Text Button", mySkin, "small")
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


    fun create_button_move_forward() {
        val button_move_foward: Button = TextButton("Text Button", mySkin, "small")
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

    fun create_button_move_backward() {
        val button_move_backward: Button = TextButton("Text Button", mySkin, "small")
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
        AppContext.stage.addActor(button_rotate_up)
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
        AppContext.stage.addActor(button_rotate_up)
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
        AppContext.stage.addActor(button_rotate_up)
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
        AppContext.stage.addActor(button_rotate_up)
    }
    override fun dispose() {
        mySkin.dispose()
    }
}


class Sensitivity(var threshold: Threshold?, var speed_rotation_camera_by_button: Float, var speed_move_camera_by_button: Float, var speed_rotation_camera_by_sensor: Float, var speed_move_camera_by_sensor: Float)

class Threshold(var sensorProvider: SensorProvider) {

    fun change_threshold(new_val: Float) {
        sensorProvider.threshold_gyroXYZ = new_val
    }
}


class Triggers(val path_to_sounds: String) : AnimationController.AnimationListener {
    val animationNames = mutableListOf<String>()
    val audioNames = mutableListOf<String>()
    val anim_trigger_zones = mutableListOf<String>()
    val audio_trigger_zones = mutableListOf<String>()
    private val animBoundsList = mutableListOf<BoundingBox>()
    private val animBoundsMap = mutableMapOf<String, BoundingBox>()
    private val audioBoundsList = mutableListOf<BoundingBox>()
    private val audioBoundsMap =  mutableMapOf<String, BoundingBox>()
    private val animationsLaunched = mutableMapOf<String, Boolean>()
    private val audiosLaunched = mutableMapOf<String, Boolean>()

    // Словарь для хранения звуков
    private val soundMap = mutableMapOf<String, Sound>()

    fun find_animations() {
        val animations = AppContext.scene.modelInstance.animations
        for (animation in animations) {
            val animationName = animation.id
            animationNames.add(animationName)
            animationsLaunched[animationName] = false
        }
        animationNames.sort()
    }

    fun find_and_load_audios() {
        val soundsFolder = Gdx.files.internal(path_to_sounds) // Путь к папке assets/sounds
        if (!soundsFolder.exists() || !soundsFolder.isDirectory) {
            Gdx.app.error("Sounds", "Папка '$path_to_sounds' не найдена или это не папка")
        }
        val files = soundsFolder.list()
        for (file in files) {
            if (file.extension().equals("mp3", ignoreCase = true)) {
                audioNames.add(file.name())
                audiosLaunched[file.name()] = false
                soundMap[file.name()] = Gdx.audio.newSound(Gdx.files.internal("$path_to_sounds/${file.name()}"))
            }
        }
        audioNames.sort()
    }

    fun find_triggers_zone() {
        val nodes = AppContext.scene.modelInstance.nodes
        for (node in nodes) {
            if (node.id.startsWith("anim_zone_", ignoreCase = false)) {
                anim_trigger_zones.add(node.id)
            } else if (node.id.startsWith("audio_zone_", ignoreCase = false)) {
                audio_trigger_zones.add(node.id)
            }
        }
        anim_trigger_zones.sort()
        audio_trigger_zones.sort()
    }

    fun create_bounding_boxes() {
        for (zoneName in anim_trigger_zones) {
            val zoneNode = AppContext.scene.modelInstance.getNode(zoneName)
            if (zoneNode != null) {
                val bounds = BoundingBox()
                zoneNode.calculateBoundingBox(bounds)
                animBoundsList.add(bounds)
            }
        }

        for (audioZoneName in audio_trigger_zones) {
            val zoneNode = AppContext.scene.modelInstance.getNode(audioZoneName)
            if (zoneNode != null) {
                val bounds = BoundingBox()
                zoneNode.calculateBoundingBox(bounds)
                audioBoundsList.add(bounds)
            }
        }
    }

    fun createAnimBoundsMap() {
        if (animationNames.size != animBoundsList.size) {
            Gdx.app.error("Triggers", "Размеры animationNames и animBoundsList не совпадают")
        }
        for (i in animationNames.indices) {
            animBoundsMap[animationNames[i]] = animBoundsList[i]
        }
    }

    fun createAudioBoundsMap() {
        if (audioNames.size != audioBoundsList.size) {
            Gdx.app.error("Triggers", "Размеры audioNames и audioBoundsList не совпадают")
        }
        for (i in audioNames.indices) {
            audioBoundsMap[audioNames[i]] = audioBoundsList[i]
        }
    }

    fun check_and_start_animations(cameraPos: Vector3) {
        for ((animationName, animBound) in animBoundsMap) {
            if (animBound.contains(cameraPos) && animationsLaunched[animationName] == false) {
                AppContext.scene.animationController.action(animationName, 1, 1f, this, 0f)
                animationsLaunched[animationName] = true
            }
        }
    }

    fun check_and_start_audios(cameraPos: Vector3) {
        for ((audioName, audioBound) in audioBoundsMap) {
            if (audioBound.contains(cameraPos) && audiosLaunched[audioName] == false) {
                playAudio(audioName)
                audiosLaunched[audioName] = true
            }
        }
    }

    private fun playAudio(key: String) {
        val sound = soundMap[key] ?: return
        sound.play(1f) // Громкость 100%
    }


    override fun onEnd(animation: AnimationController.AnimationDesc?) {
        // Анимация закончилась
        AppContext.collisionManager.loadColliders()
    }

    override fun onLoop(animation: AnimationController.AnimationDesc?) {
        // Циклическая анимация повторяется
    }

    fun dispose() {
        for (sound in soundMap.values) {
            sound.dispose()
        }
    }
}



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
//            bounds.mul(worldTransform)
            objectBoundsMap[node.id] = bounds
            Gdx.app.log("node_name", "${node.id}")
            Gdx.app.log("node_box", "$bounds")

            if (node.children.iterator().hasNext()) {
                traverseSceneGraph(worldTransform, node.children)
            }
        }
    }

    fun checkCollision(cameraPosition: Vector3): Boolean {
        val cameraBox = createCameraBounds(cameraPosition)
        for ((key, objBounds) in objectBoundsMap) {
            if (cameraBox.intersects(objBounds)) {
//                objectBoundsMap.remove(key)
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
