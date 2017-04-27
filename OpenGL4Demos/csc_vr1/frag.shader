#version 430 core

in vec4 vs_color; // input from vertex shader
out vec4 color; // output 

void main(void) {
	color = vs_color; // color passthrough
}