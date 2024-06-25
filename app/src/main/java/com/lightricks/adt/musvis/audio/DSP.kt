package com.lightricks.adt.musvis.audio

import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot

/**
 * Functions related to digital signal processing.
 */
object DSP {
    /**
     * Apply the Hamming window to [data] and return the result.
     */
    fun window(data: FloatArray): FloatArray {
        val n = data.size
        val result = FloatArray(n)
        val k = 2f * PI.toFloat() / (n - 1)
        for (i in data.indices) {
            val weight = 0.54f - 0.46f * cos(k * i)
            result[i] = data[i] * weight
        }
        return result
    }

    /**
     * Apply the Fast Fourier Transform to [data] and
     * return the magnitudes of the frequency components.
     * The result has size: data.size / 2.
     */
    fun frequencies(data: FloatArray): FloatArray {
        val n = data.size
        val transformer = FloatFFT_1D(n.toLong())
        transformer.realForward(data)
        val magnitudes = FloatArray(n / 2 + 1)
        magnitudes[n / 2] = abs(data[1])
        for (k in 0 until n / 2) {
            val i = k * 2
            magnitudes[k] = hypot(data[i], data[i + 1])
        }
        return magnitudes.sliceArray(1 ..magnitudes.lastIndex)
    }

    /**
     * Compress the [data] by taking an average of
     * every [ratio] values and return the result.
     * The result has size: data.size / ratio
     */
    fun compress(data: FloatArray, ratio: Int): FloatArray {
        val compressed = FloatArray(data.size / ratio)
        for (i in compressed.indices) {
            var sum = 0.0
            for (j in 0 until ratio) {
                sum += data[i * ratio + j]
            }
            compressed[i] = sum.toFloat() / ratio.toFloat()
        }
        return compressed
    }

    /**
     * Normalize [data] to the range [0, 1].
     */
    fun normalize(data: FloatArray): FloatArray {
        val maximum = data.max()
        val normalized = FloatArray(data.size)
        for (i in data.indices) {
            normalized[i] = data[i] / maximum
        }
        return normalized
    }
}
