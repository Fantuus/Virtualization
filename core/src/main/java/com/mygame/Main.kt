package com.mygame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
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
    private var diffuseCubemap: Cubemap? = null
    private var environmentCubemap: Cubemap? = null
    private var specularCubemap: Cubemap? = null
    private var brdfLUT: Texture? = null
    private var time = 0f
    private var skybox: SceneSkybox? = null
    private var light: DirectionalLightEx? = null

    private var stage: Stage? = null
    private var outputLabel: Label? = null

    private var horizontal_angle_tagret = 0f
    private var vertical_angle_tagret = 0f


    override fun create() {

        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        val row_height = Gdx.graphics.width / 12
        val col_width = Gdx.graphics.width / 12
        val mySkin = Skin(Gdx.files.internal("skin/glassy-ui.json"))

        // Text Button 1
        val button1: Button = TextButton("Text Button", mySkin, "small")
        button1.setSize((col_width).toFloat(), row_height.toFloat())
        button1.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 3).toFloat()
        )
        button1.addListener(object : InputListener() {
            override fun touchUp(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ) {
                val localX = Vector3(camera!!.direction).crs(camera!!.up).nor()
                val quatX = Quaternion().setFromAxisRad(localX, MathUtils.degreesToRadians * 10)
                camera!!.rotate(quatX)
                camera!!.update() // Обязательно обновите матрицу
                outputLabel!!.setText("Press a Button 1")
            }

            override fun touchDown(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                outputLabel!!.setText("Pressed Text Button 1")
                return true
            }
        })
        stage!!.addActor(button1)


        // Text Button 2
        val button2: Button = TextButton("Text Button", mySkin, "small")
        button2.setSize((col_width).toFloat(), row_height.toFloat())
        button2.setPosition(
            (col_width * 3).toFloat(),
            (Gdx.graphics.height - row_height * 4).toFloat()
        )
        button2.addListener(object : InputListener() {
            override fun touchUp(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ) {
                val quatY = Quaternion().setFromAxisRad(Vector3.Y, MathUtils.degreesToRadians * -10)
                camera!!.rotate(quatY)
                camera!!.update()
                outputLabel!!.setText("Press a Button 2")
            }

            override fun touchDown(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                outputLabel!!.setText("Pressed Text Button 2")
                return true
            }
        })
        stage!!.addActor(button2)

// Text Button 3
        val button3: Button = TextButton("Text Button", mySkin, "small")
        button3.setSize((col_width).toFloat(), row_height.toFloat())
        button3.setPosition(
            (col_width * 2).toFloat(),
            (Gdx.graphics.height - row_height * 5).toFloat()
        )
        button3.addListener(object : InputListener() {
            override fun touchUp(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ) {
                val localX = Vector3(camera!!.direction).crs(camera!!.up).nor()
                val quatX = Quaternion().setFromAxisRad(localX, MathUtils.degreesToRadians * (-10))
                camera!!.rotate(quatX)
                camera!!.update()
                outputLabel!!.setText("Press a Button 3")
            }

            override fun touchDown(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                outputLabel!!.setText("Pressed Text Button 3")
                return true
            }
        })
        stage!!.addActor(button3)



        // Text Button 4
        val button4: Button = TextButton("Text Button", mySkin, "small")
        button4.setSize((col_width).toFloat(), row_height.toFloat())
        button4.setPosition(
            (col_width * 1).toFloat(),
            (Gdx.graphics.height - row_height * 4).toFloat()
        )
        button4.addListener(object : InputListener() {
            override fun touchUp(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ) {
                val quatY = Quaternion().setFromAxisRad(Vector3.Y, MathUtils.degreesToRadians * 10)
                camera!!.rotate(quatY)
                camera!!.update()
                outputLabel!!.setText("Press a Button 4")
            }

            override fun touchDown(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                outputLabel!!.setText("Pressed Text Button 4")
                return true
            }
        })
        stage!!.addActor(button4)




        // Text Button 5
        val button5: Button = TextButton("Text Button", mySkin, "small")
        button5.setSize((col_width).toFloat(), row_height.toFloat())
        button5.setPosition(
            (col_width * 9).toFloat(),
            (Gdx.graphics.height - row_height * 3).toFloat()
        )
        button5.addListener(object : InputListener() {
            override fun touchUp(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ) {
                val direction = camera!!.direction.cpy().nor()
                val distance = 0.04f
                camera!!.position.add(direction.scl(distance))
                camera!!.update()
                outputLabel!!.setText("Press a Button 5")
            }

            override fun touchDown(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                outputLabel!!.setText("Pressed Text Button 5")
                return true
            }
        })
        stage!!.addActor(button5)


        // Text Button 6
        val button6: Button = TextButton("Text Button", mySkin, "small")
        button6.setSize((col_width).toFloat(), row_height.toFloat())
        button6.setPosition(
            (col_width * 9).toFloat(),
            (Gdx.graphics.height - row_height * 5).toFloat()
        )
        button6.addListener(object : InputListener() {
            override fun touchUp(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ) {
                val direction = camera!!.direction.cpy().nor()
                val distance = -0.04f
                camera!!.position.add(direction.scl(distance))
                camera!!.update()
                outputLabel!!.setText("Press a Button 6")
            }

            override fun touchDown(
                event: InputEvent,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                outputLabel!!.setText("Pressed Text Button 6")
                return true
            }
        })
        stage!!.addActor(button6)



        outputLabel = Label("Press a Button", mySkin, "black")
        outputLabel!!.setSize(Gdx.graphics.width.toFloat(), row_height.toFloat())
        outputLabel!!.setPosition(0f, row_height.toFloat()*4)
        stage!!.addActor(outputLabel)
        // create scene

        val sceneAsset = GLTFLoader().load(Gdx.files.internal("models/BoomBox/glTF/BoomBox.gltf"))
        val scene = Scene(sceneAsset.scene)


        sceneManager = SceneManager()
        sceneManager!!.addScene(scene)


        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        val d = .02f
        camera!!.near = d / 1000f
        camera!!.far = d * 100
        sceneManager!!.setCamera(camera)


        // setup light
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
