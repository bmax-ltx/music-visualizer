package com.lightricks.adt.musvis.ui

import android.content.Context
import android.opengl.GLES32.*
import android.opengl.GLSurfaceView
import com.lightricks.adt.musvis.R
import com.lightricks.adt.musvis.graphics.Program
import com.lightricks.adt.musvis.utils.readTextResource
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VisualizerRenderer(context: Context) : GLSurfaceView.Renderer {
    private val vertexShaderCode = context.readTextResource(R.raw.vertex)
    //TODO: read the geometry shader source code if needed
    private val fragmentShaderCode = context.readTextResource(R.raw.fragment)

    //TODO: setup a vertex array and an array buffer in order to provide vertex attributes.
    //Docs: https://www.khronos.org/opengl/wiki/Vertex_Specification
    private val vertexArray = IntArray(1)
    private val vertexBuffer = IntArray(1)

    var channelCount: Int = 2
    var bytesPerSample: Int = 2
    var samples: ShortBuffer = ShortBuffer.allocate(0)
    var frequencies = FloatArray(0)

    private lateinit var program: Program

    /** Public methods */

    fun release() {
        //TODO: release the program
    }

    /** GLSurfaceView.Renderer implementation */

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        //TODO: initialize the program, specify attribute names and uniform names
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        if (width >= height) {
            glViewport(0, 0, width, height)
        } else {
            val aspectRatio = width.toFloat() / height.toFloat()
            val newHeight = width.toFloat() * aspectRatio
            val bottom = height / 2 - newHeight.toInt() / 2
            glViewport(0, bottom, width, newHeight.toInt())
        }
    }

    override fun onDrawFrame(gl: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)
        //TODO: draw something using the program
        //Docs: https://registry.khronos.org/OpenGL-Refpages/es3/
        //Hint: Search for glUniform3f, glUniform1i, glBindVertexArray,
        // glBindBuffer, glBufferData, glEnableVertexAttribArray,
        // glVertexAttribPointer, glDrawArrays
    }
}
