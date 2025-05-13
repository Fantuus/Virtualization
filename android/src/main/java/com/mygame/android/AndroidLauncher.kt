package com.mygame.android

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.mygame.Main

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    private lateinit var sensorProvider: AndroidSensorProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        sensorProvider = AndroidSensorProvider(this)

        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
        }

        initialize(Main(sensorProvider), config)
    }

    override fun onResume() {
        super.onResume()
        sensorProvider.startSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorProvider.stopSensors()
    }
}
