package com.mygame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mygame.MainMenuScreen.WorldData

class MainMenuScreen(private val game: MyGame) : Screen {
    private lateinit var stage: Stage
    private lateinit var startMenuUiCreator: StartMenuUiCreator

    init {
        stage = Stage(ScreenViewport())
        startMenuUiCreator = StartMenuUiCreator(game)
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        val rootTable = startMenuUiCreator.createRootTable()
        stage.addActor(rootTable)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1f)
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
    }

    data class WorldData(val name: String, val previewImagePath: String)
}

class StartMenuUiCreator(val game: MyGame) {
    val marginFromEdge = minOf(Gdx.graphics.height, Gdx.graphics.width) / 20f
    val marginBetweenElement = marginFromEdge / 2f
    val scrollTableWidth = Gdx.graphics.width / 2.2f
    val scrollTableHeight = Gdx.graphics.height / 1.7f
    val imageWidth = scrollTableWidth / 1.4f
    val imageHeight = scrollTableHeight / 1.4f
    val skin = Skin(Gdx.files.internal("skin/glassy-ui.json"))


    fun createRootTable() : Table {
        val worldList = this.getWorldData()

        val rootTable = Table()
        rootTable.setFillParent(true)

        val label_up = this.create_label_up()
        rootTable.add((label_up))
        rootTable.row()
        val scrollTable = this.createScrollTable(worldList)
        val scrollPane = this.createScrollPane(scrollTable)

        rootTable.add(scrollPane).pad(marginBetweenElement, marginFromEdge, 0f, marginFromEdge).growX().row()

        val exitButton = this.createExitButton()
        rootTable.row()
        rootTable.add(exitButton).padTop(marginBetweenElement)
        return rootTable
    }

    fun create_label_up(): Label {
        val label = Label("Select world", skin, "default")
        label.setFontScale(4f)
        return label

    }

    fun getWorldData(): List<WorldData> {
        val worldList = mutableListOf<WorldData>()
        val soundsFolder = Gdx.files.internal("models") // Путь к папке assets/sounds
        val folders = soundsFolder.list()
        for (folder in folders) {
            val worldName = folder.name()
            val worldImagePath = "models/$worldName/$worldName.jpg"
            worldList.add(WorldData(worldName, worldImagePath))
        }
        return worldList
    }

    fun createScrollTable(worldList: List<WorldData>): Table {
        val scrollTable = Table()
        for (world in worldList) {
            val worldCard = this.createWorldCard(world)
            scrollTable.add(worldCard).size(scrollTableWidth, scrollTableHeight).pad(marginBetweenElement)
        }
        return scrollTable
    }

    fun createScrollPane(scrollTable: Table) :ScrollPane {
        val scrollPane = ScrollPane(scrollTable, skin)
        scrollPane.setScrollingDisabled(false, true)
        scrollPane.fadeScrollBars = false
        return scrollPane
    }

    fun createExitButton() : TextButton {
        val exitButton = TextButton("Exit", skin)
        exitButton.addListener(object : InputListener() {
            override fun touchDown(
                event: InputEvent?,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                Gdx.app.exit()
                return true
            }
        })
        return exitButton
    }


    fun createWorldCard(world: WorldData): Table {
        val texture = Texture(Gdx.files.internal(world.previewImagePath))
        val region = TextureRegion(texture)
        val drawable = TextureRegionDrawable(region)

        val image = Image(drawable)
        image.setOrigin(Align.center)

        val label = Label(world.name, skin)
        label.setFontScale(3f)
        label.setAlignment(Align.center)

        val card = Table(skin)
        card.background = skin.newDrawable("white", Color(0.3f, 0.1f, 0.3f, 0.8f)) // Темный фон
        card.pad(marginBetweenElement*10)
        card.add(image).size(imageWidth, imageHeight).row()
        card.add(label).padTop(marginBetweenElement).center()

        card.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                game.setScreen(GameScreen(game.sensorProvider))
                return true
            }
        })

        return card
    }
}
