package com.lightricks.adt.musvis.graphics

import android.opengl.GLES32.*
import android.opengl.GLU

/**
 * Functions related to OpenGL ES.
 */
object GLES {
    fun compileShader(type: Int, shaderCode: String): Int {
        val shader = checkGLError { glCreateShader(type) }
        checkGLError { glShaderSource(shader, shaderCode) }
        checkGLError { glCompileShader(shader) }
        val status = IntArray(1)
        glGetShaderiv(shader, GL_COMPILE_STATUS, status, 0)
        check(status[0] == GL_TRUE) {
            val info = glGetShaderInfoLog(shader)
            error("Failed to compile the shader: $info")
        }
        return shader
    }

    inline fun <R> checkGLError(block: () -> R): R {
        val v = block()
        val error = glGetError()
        check(error == GL_NO_ERROR) {
            String.format("GL Error 0x%04X (%s)", error, GLU.gluErrorString(error))
        }
        return v
    }
}
