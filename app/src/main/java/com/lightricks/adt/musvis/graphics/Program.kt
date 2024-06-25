package com.lightricks.adt.musvis.graphics

import android.opengl.GLES32.*
import com.lightricks.adt.musvis.graphics.GLES.checkGLError
import com.lightricks.adt.musvis.graphics.GLES.compileShader

/**
 * OpenGL ES program consisting of the required vertex and fragment
 * shaders and an optional geometry shader.
 */
class Program(
    vertexShaderCode: String,
    geometryShaderCode: String? = null,
    fragmentShaderCode: String,
    uniforms: List<String> = emptyList(),
    attributes: List<String> = emptyList(),
) {
    val handle: Int

    private val uniformLocations = mutableMapOf<String, Int>()
    private val attributeLocations = mutableMapOf<String, Int>()

    init {
        val vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderCode)
        val geometryShader = if (geometryShaderCode != null) {
            compileShader(GL_GEOMETRY_SHADER, geometryShaderCode)
        } else {
            null
        }
        val fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
        val program = checkGLError { glCreateProgram() }
        checkGLError { glAttachShader(program, vertexShader) }
        if (geometryShader != null) {
            checkGLError { glAttachShader(program, geometryShader) }
        }
        checkGLError { glAttachShader(program, fragmentShader) }
        checkGLError { glLinkProgram(program) }
        val status = IntArray(1)
        glGetProgramiv(program, GL_LINK_STATUS, status, 0)
        check(status[0] == GL_TRUE) {
            val info = glGetProgramInfoLog(program)
            error("Failed to link the program: $info")
        }
        checkGLError { glDeleteShader(vertexShader) }
        if (geometryShader != null) {
            checkGLError { glDeleteShader(geometryShader) }
        }
        checkGLError { glDeleteShader(fragmentShader) }
        handle = program
        uniforms.forEach {
            addUniform(it)
        }
        attributes.forEach {
            addAttribute(it)
        }
    }

    inline fun use(block: (program: Program) -> Unit) {
        checkGLError { glUseProgram(handle) }
        block(this)
        checkGLError { glUseProgram(0) }
    }

    fun getUniform(name: String): Int {
        return uniformLocations[name] ?: error("Unknown uniform $name")
    }

    fun getAttribute(name: String): Int {
        return attributeLocations[name] ?: error("Unknown attribute $name")
    }

    fun release() {
        checkGLError { glDeleteProgram(handle) }
    }

    private fun addUniform(name: String) {
        uniformLocations[name] = checkGLError { glGetUniformLocation(handle, name) }
    }

    private fun addAttribute(name: String) {
        attributeLocations[name] = checkGLError { glGetAttribLocation(handle, name) }
    }
}
