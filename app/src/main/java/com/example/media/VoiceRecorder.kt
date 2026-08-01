package com.example.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    var outputFile: File? = null
        private set

    fun start(): Boolean {
        return try {
            val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setAudioEncodingBitRate(96000)
            rec.setAudioSamplingRate(44100)
            rec.setOutputFile(file.absolutePath)
            rec.prepare()
            rec.start()
            recorder = rec
            outputFile = file
            true
        } catch (e: IOException) {
            release()
            false
        } catch (e: IllegalStateException) {
            release()
            false
        } catch (e: SecurityException) {
            release()
            false
        }
    }

    /** Retourne le fichier enregistré, ou null si l'enregistrement a échoué / a été trop court. */
    fun stop(): File? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            outputFile
        } catch (e: Exception) {
            release()
            null
        }
    }

    fun cancel() {
        release()
        outputFile?.delete()
        outputFile = null
    }

    private fun release() {
        try {
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
    }
}
