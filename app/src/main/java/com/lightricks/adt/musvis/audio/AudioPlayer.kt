package com.lightricks.adt.musvis.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodec.CodecException
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import com.lightricks.adt.musvis.audio.AudioFormatUtils.getChannelConfig
import com.lightricks.adt.musvis.audio.AudioFormatUtils.getPcmEncoding
import com.lightricks.adt.musvis.utils.ErrorListener
import com.lightricks.adt.musvis.utils.splitChannels
import java.nio.ShortBuffer

class AudioPlayer(
    context: Context,
    assetName: String,
    trackIndex: Int,
    private val isLooping: Boolean = false,
    private val errorListener: ErrorListener,
    private val dataListener: AudioDataListener? = null
) : MediaCodec.Callback() {
    private val extractor: MediaExtractor = MediaExtractor()
    private val looperThread = HandlerThread("AudioPlayer").apply { start() }
    private val looperThreadHandler = Handler(looperThread.looper)
    private val pendingBuffers = mutableListOf<Pair<Int, MediaCodec.BufferInfo>>()
    private val audioDecoder: MediaCodec
    private val audioTrack: AudioTrack
    private val trackFormat: MediaFormat

    private var channelCount = 1
    private var isPlaying = false

    init {
        val assetFd = context.assets.openFd(assetName)
        extractor.setDataSource(assetFd.fileDescriptor, assetFd.startOffset, assetFd.length)
        extractor.selectTrack(trackIndex)

        trackFormat = extractor.getTrackFormat(trackIndex)
        channelCount = trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        audioDecoder = setupAudioDecoder(trackFormat)
        audioTrack = setupAudioTrack(trackFormat)
    }

    fun play() {
        looperThreadHandler.post {
            isPlaying = true
            audioTrack.play()
            while (pendingBuffers.isNotEmpty()) {
                val (pendingIndex, pendingInfo) = pendingBuffers.removeFirst()
                writeBuffer(audioDecoder, pendingIndex, pendingInfo)
            }
        }
    }

    fun pause() {
        looperThreadHandler.post {
            isPlaying = false
            audioTrack.pause()
        }
    }

    fun toggle() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun release() {
        looperThreadHandler.post {
            extractor.release()
            audioDecoder.release()
            audioTrack.release()
        }
        looperThread.quitSafely()
    }

    /** MediaCodec.Callback implementation  */

    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        val buffer = codec.getInputBuffer(index) ?: return
        val sampleSize = extractor.readSampleData(buffer, 0)
        if (sampleSize > 0) {
            codec.queueInputBuffer(index, 0, sampleSize, extractor.sampleTime, extractor.sampleFlags)
        }

        if (sampleSize < 0 || !extractor.advance()) {
            if (isLooping) {
                extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            } else {
                codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }
        }
    }

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        if (isPlaying) {
            writeBuffer(codec, index, info)
        } else {
            pendingBuffers.add(Pair(index, info))
        }
    }

    override fun onError(codec: MediaCodec, e: CodecException) {
        errorListener.onError(e)
    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        // unused
    }

    /** Private methods  */

    private fun setupAudioDecoder(trackFormat: MediaFormat): MediaCodec {
        val mediaType = trackFormat.getString(MediaFormat.KEY_MIME)
            ?: error("Could not find the MIME type")

        return MediaCodec.createDecoderByType(mediaType).apply {
            setCallback(this@AudioPlayer, looperThreadHandler)
            configure(trackFormat, null, null, 0)
            start()
        }
    }

    private fun setupAudioTrack(trackFormat: MediaFormat): AudioTrack {
        val sampleRate = trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val pcmEncoding = getPcmEncoding(trackFormat)
        val channelCount = trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val channelMask = getChannelConfig(channelCount)
        val minBuffSize = AudioTrack.getMinBufferSize(sampleRate, channelMask, pcmEncoding)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setEncoding(pcmEncoding)
            .setSampleRate(sampleRate)
            .setChannelMask(channelMask)
            .build()

        return AudioTrack.Builder()
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(minBuffSize)
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .build()
    }

    private fun writeBuffer(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        val buffer = codec.getOutputBuffer(index) ?: return
        if (info.size > 0) {
            val frequencies = getFrequencies(buffer.asShortBuffer(), channelCount)
            val normalized = DSP.normalize(DSP.compress(frequencies, COMPRESSION_RATIO))
            dataListener?.run {
                onFrequenciesAvailable(normalized)
                onSamplesAvailable(buffer.asShortBuffer())
                buffer.rewind()
            }
            audioTrack.write(buffer, buffer.remaining(), AudioTrack.WRITE_BLOCKING)
        }

        codec.releaseOutputBuffer(index, false)
    }

    private fun getFrequencies(buffer: ShortBuffer, channelCount: Int): FloatArray {
        if (!buffer.hasRemaining()) {
            return FloatArray(0)
        }

        val oneChannel = buffer.splitChannels(channelCount)[0]
        return DSP.frequencies(DSP.window(oneChannel))
    }

    companion object {
        private const val COMPRESSION_RATIO = 16
    }
}
