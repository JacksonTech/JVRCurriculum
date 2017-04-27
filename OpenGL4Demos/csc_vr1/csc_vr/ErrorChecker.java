/*
 * Copyright 2017 Dr. Scott Gordon and Dr. John Clevenger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package csc_vr;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.*;

/* This class is based on lecture slides provided by Dr. Gordon */
public class ErrorChecker {

	private GLU glu;
	private GLAutoDrawable drawable;
	
	/* Constructor, set up drawable */
	public ErrorChecker(GLAutoDrawable g) {
		drawable = g;
		glu = new GLU();
	}
	
	/* See if there was an error in the last operation */
	public boolean checkOpenGLError() {
		GL4 gl = (GL4) drawable.getGL();
		boolean foundError = false;
		int glErr = gl.glGetError();
		while (glErr != GL.GL_NO_ERROR) {
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		return foundError;
	}
	
	/* Print the shader compilation error */
	public void printShaderLog(int shader) {
		GL4 gl = (GL4) drawable.getGL();
		int[] len = new int[1];
		int[] charsWritten = new int[1];
		byte[] log = null;
		
		//determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader,len[0],charsWritten,0,log,0);
			System.out.println("Shader info log: ");
			for (int i = 0; i < log.length; i++) {
				System.out.print((char)log[i]);
			}
		}
	}
	
	/* print the program link log */
	public void printProgramLog(int program) {
		GL4 gl = (GL4) drawable.getGL();
		int[] len = new int[1];
		int[] charsWritten = new int[1];
		byte[] log = null;
		
		//determine the length of the shader compilation log
		gl.glGetProgramiv(program, GL4.GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(program,len[0],charsWritten,0,log,0);
			System.out.println("Program info log: ");
			for (int i = 0; i < log.length; i++) {
				System.out.print((char)log[i]);
			}
		}
	}
}	