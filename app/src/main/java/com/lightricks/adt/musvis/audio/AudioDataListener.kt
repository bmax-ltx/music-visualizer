package com.lightricks.adt.musvis.audio

import java.nio.ShortBuffer

interface AudioDataListener {
    fun onSamplesAvailable(samples: ShortBuffer)
    fun onFrequenciesAvailable(frequencies: FloatArray)
}
