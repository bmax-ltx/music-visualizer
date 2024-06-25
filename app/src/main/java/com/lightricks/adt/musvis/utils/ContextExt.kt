package com.lightricks.adt.musvis.utils

import android.content.Context
import androidx.annotation.RawRes

fun Context.readTextResource(@RawRes resource: Int): String {
    return resources.openRawResource(resource)
        .bufferedReader()
        .use { it.readText() }
}
