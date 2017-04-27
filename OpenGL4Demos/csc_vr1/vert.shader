#version 430 core

in vec4 vs_pos; 				// vertex position
out vec4 vs_color; 				// output to fragmentation shader

void main(void) {
	vs_color = 					// set color
		vec4(0.6f, 0.8f, 0.6f, 1.0f); 	
	gl_Position = vs_pos; 		// output location 
}