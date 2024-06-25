package com.lightricks.adt.musvis.ui

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lightricks.adt.musvis.audio.AudioDataListener
import com.lightricks.adt.musvis.audio.AudioPlayer
import com.lightricks.adt.musvis.databinding.VisualizerActivityBinding
import com.lightricks.adt.musvis.utils.ErrorListener
import java.lang.Exception
import java.nio.ShortBuffer

class VisualizerActivity : AppCompatActivity(), AudioDataListener, ErrorListener {
    private lateinit var binding: VisualizerActivityBinding
    private lateinit var renderer: VisualizerRenderer
    private lateinit var player: AudioPlayer

    /** Activity methods */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VisualizerActivityBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        setupRenderer()
        setupPlayer()
    }

    override fun onPause() {
        super.onPause()
        binding.glSurfaceView.onPause()
        player.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.glSurfaceView.onResume()
        player.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        renderer.release()
        player.release()
    }

    /** AudioDataListener implementation */

    override fun onSamplesAvailable(samples: ShortBuffer) {
        with(binding.glSurfaceView) {
            queueEvent { renderer.samples = samples }
            requestRender()
        }
    }

    override fun onFrequenciesAvailable(frequencies: FloatArray) {
        with(binding.glSurfaceView) {
            queueEvent { renderer.frequencies = frequencies }
            requestRender()
        }
    }

    /** ErrorListener implementation */

    override fun onError(exception: Exception) {
        showMessage(exception.message ?: "Unknown error")
    }

    /** Private methods */

    private fun setupPlayer() {
        player = AudioPlayer(
            context = applicationContext,
            assetName = ASSET_NAME,
            trackIndex = TRACK_INDEX,
            isLooping = true,
            errorListener = this,
            dataListener = this
        )
    }

    private fun setupRenderer() {
        renderer = VisualizerRenderer(applicationContext).apply {
            bytesPerSample = 2
            channelCount = 2
        }
        with(binding.glSurfaceView) {
            setEGLContextClientVersion(3)
            setOnClickListener { player.toggle() }
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val ASSET_NAME = "1.mp3"
        private const val TRACK_INDEX = 0
    }
}
