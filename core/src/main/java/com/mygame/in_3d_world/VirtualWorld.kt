package com.mygame.in_3d_world

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cubemap
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
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
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import kotlin.math.abs

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


class VirtualWorld(private val sensorProvider: SensorProvider, val worldName: String) : ApplicationAdapter() {
    private var sceneManager: SceneManager? = null
    private var sceneAsset: SceneAsset? = null
    private var diffuseCubemap: Cubemap? = null
    private var environmentCubemap: Cubemap? = null
    private var specularCubemap: Cubemap? = null
    private var brdfLUT: Texture? = null
    private var time = 0f
    private var skybox: SceneSkybox? = null
    private var light: DirectionalLightEx? = null

    private val speed_rotation_camera_by_button = 25f
    private val speed_move_camera_by_button = 0.4f

    private var button_creator: ButtonCreator? = null

    private var real_move_direction = MoveDirections.STOP.value

    private var lastInputTime = 0f
    private var timeToHideButtons = 5f
    private var showUI = false

    override fun create() {
        val path_to_model = "models/$worldName/gltf/$worldName.gltf"
        val path_to_sounds = "sounds/$worldName/"
        AppContext.stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = AppContext.stage

        val sceneAsset = GLTFLoader().load(Gdx.files.internal(path_to_model))
        AppContext.scene = Scene(sceneAsset.scene)

        AppContext.collisionManager = CollisionManager(AppContext.scene.modelInstance)
        AppContext.collisionManager.loadColliders()

        sceneManager = SceneManager()
        sceneManager!!.addScene(AppContext.scene)

        setup_camera()

        setup_all_light()

        setup_scybox()

        button_creator = ButtonCreator()
        button_creator!!.create_all_ui()
        button_creator!!.hide_buttons()
        showUI = false

        AppContext.triggers = Triggers(path_to_sounds)
        AppContext.triggers.setup_all_triggers()
    }


    override fun resize(width: Int, height: Int) {
        sceneManager!!.updateViewport(width.toFloat(), height.toFloat())
    }

    override fun render() {
        val deltaTime = Gdx.graphics.deltaTime
        time += deltaTime

        if (Gdx.input.isTouched) run { show_ui() }
        if (showUI && time - lastInputTime > timeToHideButtons) run { hide_ui() }

        update_camera()
        render_screen(deltaTime)

        check_and_start_animations_and_audios()
        move_and_rotate_camera()

        AppContext.stage.act()
        AppContext.stage.draw()
    }

    private fun setup_all_light() {
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
    }

    private fun setup_scybox() {
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
    }

    private fun setup_camera() {
        AppContext.camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        AppContext.camera.near = 0.1f // Минимальное расстояние, которое видит камера
        AppContext.camera.far = 200f  // Максимальное расстояние (достаточно для вашей модели)
        AppContext.camera.position.set(-0.2f, 1.6f, 0.5f)
        AppContext.camera.update()
        sceneManager!!.setCamera(AppContext.camera)
        AppContext.cameraController = CameraController(speed_move_camera_by_button, speed_rotation_camera_by_button)
    }

    private fun show_ui() {
        lastInputTime = time
        if (!showUI) {
            button_creator!!.show_buttons()
            showUI = true
        }
    }

    private fun hide_ui() {
        button_creator!!.hide_buttons()
        showUI = false
    }

    private fun update_camera() {
        AppContext.camera.up.set(Vector3.Y)
        AppContext.camera.update()
    }

    private fun render_screen(deltaTime: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        sceneManager!!.update(deltaTime)
        sceneManager!!.render()
    }

    private fun check_and_start_animations_and_audios() {
        AppContext.triggers.check_and_start_animations(AppContext.camera.position)
        AppContext.triggers.check_and_start_audios(AppContext.camera.position)
    }

    private fun move_and_rotate_camera() {
        rotate_camera()
        move_camera()
    }
    private fun rotate_camera() {
        rotate_camera_X()
        rotate_camera_Y()
    }

    private fun move_camera() {
        move_camera_Z()
    }

    private fun rotate_camera_X() {
        if (sensorProvider.isXRotating) {
            if (sensorProvider.rotationX > 0f) {
                AppContext.cameraController.rotate_camera(RotationDirections.LEFT.value, sensorProvider.rotationX)
            }
            else if (sensorProvider.rotationX < 0f) {
                AppContext.cameraController.rotate_camera(RotationDirections.RIGHT.value, abs(sensorProvider.rotationX))
            }
            button_creator!!.print_to_label(sensorProvider.rotationX.toString())
        }
    }

    private fun rotate_camera_Y() {
        if (sensorProvider.isYRotating) {
            if (sensorProvider.rotationY > 0f) {
                AppContext.cameraController.rotate_camera(RotationDirections.DOWN.value, sensorProvider.rotationY)
            }
            else if (sensorProvider.rotationY < 0f) {
                AppContext.cameraController.rotate_camera(RotationDirections.UP.value, abs(sensorProvider.rotationY))
            }
        }
    }

    private fun move_camera_Z() {
        if (!sensorProvider.isMoving) {
            real_move_direction = MoveDirections.STOP.value
        }
        if (sensorProvider.isMoving && !sensorProvider.isXRotating && !sensorProvider.isYRotating) {
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
