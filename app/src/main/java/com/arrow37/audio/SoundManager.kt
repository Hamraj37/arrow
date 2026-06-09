package com.arrow37.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * Manages game sound effects using SoundPool for low-latency playback.
 * 
 * To add sounds:
 * 1. Place your files in app/src/main/res/raw/
 * 2. Ensure they are lowercase with underscores (e.g., tap.ogg, game_over.wav)
 * 3. The manager will automatically try to load them by name.
 */
class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private var tapId: Int = -1
    private var escapeId: Int = -1
    private var collisionId: Int = -1
    private var winId: Int = -1
    private var gameOverId: Int = -1

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Safely load sounds by name. This prevents compile errors if files are missing.
        tapId = loadSoundByName(context, "tap")
        escapeId = loadSoundByName(context, "whoosh_effect") 
        collisionId = loadSoundByName(context, "collision")
        winId = loadSoundByName(context, "win")
        gameOverId = loadSoundByName(context, "game_over")
    }

    /**
     * Finds a resource in res/raw by its string name and loads it into SoundPool.
     */
    private fun loadSoundByName(context: Context, name: String): Int {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        return if (resId != 0) {
            try {
                soundPool.load(context, resId, 1)
            } catch (e: Exception) {
                Log.e("SoundManager", "Error loading sound '$name': ${e.message}")
                -1
            }
        } else {
            Log.w("SoundManager", "Sound file 'res/raw/$name' not found. skipping.")
            -1
        }
    }

    fun playTap() = play(tapId)
    fun playEscape() = play(escapeId)
    fun playCollision() = play(collisionId)
    fun playWin() = play(winId)
    fun playGameOver() = play(gameOverId)

    private fun play(soundId: Int) {
        if (soundId != -1) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
