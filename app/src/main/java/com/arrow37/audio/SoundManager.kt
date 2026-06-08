package com.arrow37.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val tapSound: Int
    private val collisionSound: Int
    private val winSound: Int
    private val gameOverSound: Int
    private val escapeSound: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sounds from assets or raw folder
        escapeSound = loadSound(context, "whoosh_effect.mp3")
        tapSound = loadSound(context, "tap.mp3")
        collisionSound = loadSound(context, "collision.mp3")
        winSound = loadSound(context, "win.mp3")
        gameOverSound = loadSound(context, "game_over.mp3")
    }

    private fun loadSound(context: Context, fileName: String): Int {
        // 1. Try loading from assets
        try {
            val afd: AssetFileDescriptor = context.assets.openFd(fileName)
            return soundPool.load(afd, 1)
        } catch (e: Exception) {
            // Not in assets, try raw
        }

        // 2. Try loading from res/raw
        try {
            val resName = fileName.substringBeforeLast(".")
            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (resId != 0) {
                val afd = context.resources.openRawResourceFd(resId)
                return soundPool.load(afd, 1)
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Could not load sound $fileName from assets or raw: ${e.message}")
        }

        Log.w("SoundManager", "Sound file not found: $fileName")
        return -1
    }

    fun playTap() = play(tapSound)
    fun playCollision() = play(collisionSound)
    fun playWin() = play(winSound)
    fun playGameOver() = play(gameOverSound)
    fun playEscape() = play(escapeSound)

    private fun play(soundId: Int) {
        if (soundId != -1) {
            Log.d("SoundManager", "Playing sound: $soundId")
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.w("SoundManager", "Attempted to play sound with invalid ID (-1)")
        }
    }

    fun release() {
        soundPool.release()
    }
}
