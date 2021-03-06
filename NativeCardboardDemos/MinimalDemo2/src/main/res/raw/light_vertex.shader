uniform mat4 u_MVP;

attribute vec4 a_Position;
attribute vec4 a_Color;

varying vec4 v_Color;
varying vec3 v_Grid;

void main() {
   v_Color = a_Color;
   gl_Position = u_MVP * a_Position;
}
