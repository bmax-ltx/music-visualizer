package com.lightricks.adt.musvis.utils

import java.nio.ShortBuffer

fun ShortBuffer.splitChannels(channelCount: Int): List<FloatArray> {
    rewind()
    val frameCount = remaining() / channelCount
    val channels = List(channelCount) {
        FloatArray(frameCount)
    }
    for (i in 0 until frameCount) {
        for (c in 0 until channelCount) {
            channels[c][i] = get(i*channelCount+c).toFloat()
        }
    }
    rewind()
    return channels
}
