package com.mygame.in_3d_world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox

class Triggers(val path_to_sounds: String) : AnimationController.AnimationListener {
    private val animationNames = mutableListOf<String>()
    private val audioNames = mutableListOf<String>()
    private val anim_trigger_zones = mutableListOf<String>()
    private val audio_trigger_zones = mutableListOf<String>()
    private val animBoundsList = mutableListOf<BoundingBox>()
    private val animBoundsMap = mutableMapOf<String, BoundingBox>()
    private val audioBoundsList = mutableListOf<BoundingBox>()
    private val audioBoundsMap =  mutableMapOf<String, BoundingBox>()
    private val animationsLaunched = mutableMapOf<String, Boolean>()
    private val audiosLaunched = mutableMapOf<String, Boolean>()

    // Словарь для хранения звуков
    private val soundMap = mutableMapOf<String, Music>()

    fun setup_all_triggers() {
        find_animations()
        find_and_load_audios()
        find_triggers_zone()
        create_bounding_boxes()
        createAnimBoundsMap()
        createAudioBoundsMap()
    }


    private fun find_animations() {
        val animations = AppContext.scene.modelInstance.animations
        for (animation in animations) {
            val animationName = animation.id
            animationNames.add(animationName)
            animationsLaunched[animationName] = false
        }
        animationNames.sort()
    }

    private fun find_and_load_audios() {
        val soundsFolder = Gdx.files.internal(path_to_sounds) // Путь к папке assets/sounds
        if (!soundsFolder.exists() || !soundsFolder.isDirectory) {
            Gdx.app.error("Sounds", "Папка '$path_to_sounds' не найдена или это не папка")
        }
        val files = soundsFolder.list()
        for (file in files) {
            if (file.extension().equals("mp3", ignoreCase = true)) {
                audioNames.add(file.name())
                audiosLaunched[file.name()] = false
                val music = Gdx.audio.newMusic(Gdx.files.internal("$path_to_sounds/${file.name()}"))
                music.isLooping = false
                soundMap[file.name()] = music
            }
        }
        audioNames.sort()
    }

    private fun find_triggers_zone() {
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

    private fun create_bounding_boxes() {
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

    private fun createAnimBoundsMap() {
        if (animationNames.size != animBoundsList.size) {
            Gdx.app.error("Triggers", "Размеры animationNames и animBoundsList не совпадают")
        }
        for (i in animationNames.indices) {
            animBoundsMap[animationNames[i]] = animBoundsList[i]
        }
    }

    private fun createAudioBoundsMap() {
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
        val nodes = AppContext.scene.modelInstance.nodes
        for (node in nodes) {
            if (node.isAnimated) {
                AppContext.collisionManager.objectBoundsMap.remove(node.id)
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
        val music = soundMap[key] ?: return
        stopAllAudio()
        music.play()

        music.setOnCompletionListener {
        }
    }

    fun stopAllAudio() {
        soundMap.values.forEach { it.stop() }
    }


    override fun onEnd(animation: AnimationController.AnimationDesc?) {
        // Анимация закончилась
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
