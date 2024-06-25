#version 320 es

layout(points) in;
layout(line_strip, max_vertices=2) out;

vec3 cmap(float x) {
    return vec3(x*x, sqrt(x), 1.0-sqrt(x));
}

//TODO: declare uniforms if needed

//TODO: declare inputs from the previous stage (vertex shader) if needed

//TODO: declare outputs to the next stage (fragment shader) if needed

void main() {
    //TODO: compute and set the gl_Position, emit vertices, end the primitive
    //Docs: https://www.khronos.org/opengl/wiki/Geometry_Shader
    //Docs: https://registry.khronos.org/OpenGL-Refpages/es3/
    //Hint: Use the gl_in[] array to access the position of input primitives
}
