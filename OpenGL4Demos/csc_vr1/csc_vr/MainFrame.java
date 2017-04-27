/*
 * Copyright 2017 Cody Jackson, Dr. Scott Gordon, Dr. John Clevenger
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

/* JOGL imports */
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import java.nio.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

/**
 * Terse OpenGL 4.0 demo
 * 
 * @author Cody Jackson
 * @author Scott Gordon
 *
 */
public class MainFrame extends JFrame implements GLEventListener, ActionListener {
	private final int FRAMES_PER_SECOND = 60; 				/* adjust this to vary framerate */
	private GLCanvas myCanvas;
	private Timer timer; 									/* Timer for animation */
	private int rendering_program; 							/* gl ID for rendering program */
	private int tVertexArray[] = new int[1]; 				/* VAO array */	
	private int tVertexAttrib[] = new int[1];				/* vertex attrib (geom) */
	private FloatBuffer bgColorBuffer, vertBuffer;
	private GL4 gl;
	
	private float[] vertsArray = { 	0.25f, -0.25f, 0.5f,	/* a single tri */
								-0.25f, -0.25f, 0.5f,
								0.25f, 0.25f, 0.5f};
	
	public MainFrame() {
		setTitle("JOGL Demo 1");
		setSize(700,500);
		setLocation(300,200);
		myCanvas = new GLCanvas(); 							/* canvas goes in the center */
		myCanvas.addGLEventListener(this);
		this.getContentPane().add(myCanvas, BorderLayout.CENTER);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);		/* set close on X button */
		timer = new Timer(1000 / FRAMES_PER_SECOND, this); 	/* create a timer for animation */
		timer.start();
		this.setVisible(true);
	}

	/* set up OpenGL */
	public void init(GLAutoDrawable drawable) {
		gl = (GL4)drawable.getGL();	
		rendering_program = createShaderPrograms(drawable); /* set up shader programs */
		gl.glGenVertexArrays(tVertexArray.length,tVertexArray, 0); 
		gl.glBindVertexArray(tVertexArray[0]);				/* bind vao */
		gl.glGenBuffers(tVertexAttrib.length, tVertexAttrib, 0);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, tVertexAttrib[0]); /* bind buffer */
	
		bgColorBuffer = FloatBuffer.allocate(4); 			/* set up buffers */
		bgColorBuffer.put(new float[] {0.2f, 0.2f, 0.2f, 1.0f});
		bgColorBuffer.flip(); 								/* ready to read */
		
		int vertAttrPos = gl.glGetAttribLocation(rendering_program, "vs_pos");

		vertBuffer = FloatBuffer.wrap(vertsArray);			/* wrap around our buffer */
		
		gl.glBufferData(GL.GL_ARRAY_BUFFER, vertsArray.length * 4, vertBuffer, GL.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(vertAttrPos, 3, GL.GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(vertAttrPos);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
	}
	
	/* Called when timer ticks. Used for animation */
	public void actionPerformed(ActionEvent e) {
		myCanvas.display(); 								/* tell the canvas to invoke its callback */
	}
	
	/* Update the canvas */
	public void display(GLAutoDrawable drawable) {
		gl.glClearBufferfv(GL4.GL_COLOR, 0, bgColorBuffer); /* clear bg */
		gl.glUseProgram(rendering_program);	
		gl.glDrawArrays(GL4.GL_TRIANGLES, 0, 3);
	}
	
	/* required by GLEventListener */
	public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {}

	/* get rid of OpenGL resources on close */
	public void dispose(GLAutoDrawable drawable) {
			gl.glDeleteVertexArrays(1,tVertexArray, 0);
			gl.glDeleteBuffers(1, tVertexAttrib, 0);
			gl.glDeleteProgram(rendering_program);
	}
	
	/* Sets up vertex/fragment shader, compiles them, links them, 
	   and runs the resulting program. From Dr. Gordon's code. */
	private int createShaderPrograms(GLAutoDrawable drawable) {
		GL4 gl = (GL4) drawable.getGL();

		int[] vertCompiled = new int[1]; 				/* status flags */
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];
														/* Module to check for errors in compile/link */
		ErrorChecker errorChecker = new ErrorChecker(drawable);
		
														/* read in the source for each shader */
		String[] vShaderSource = readShaderSource("vert.shader");
		String[] fShaderSource = readShaderSource("frag.shader");
		
		int lengths[] = new int[vShaderSource.length];	/* number of lines (to store len of each line) */
		for (int i = 0; i < lengths.length; i++) { 		/* count length of each line */
			lengths[i] = vShaderSource[i].length();
		}
														/* compile shader */
		int vShader = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderSource.length, vShaderSource, lengths, 0);
		gl.glCompileShader(vShader);
	
		errorChecker.checkOpenGLError();				/* print out errors */
		gl.glGetShaderiv(vShader, GL4.GL_COMPILE_STATUS, vertCompiled, 0);
		
		if (vertCompiled[0] == 1) {						/* if it didn't compile, scream */
			System.out.println("Vertex shader compiled!");
		} else {
			System.out.println("Vertex shader compile failed!");
			errorChecker.printShaderLog(vShader);
		}
		
		lengths = new int[fShaderSource.length];		/* number of lines (to store len of each line) */
		
		for (int i = 0; i < lengths.length; i++) {		/* count length of each line */
			lengths[i] = fShaderSource[i].length();
		}
														/* compile fragment shader */
		int fShader = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fShaderSource.length, fShaderSource, lengths, 0);
		gl.glCompileShader(fShader);
		
		errorChecker.checkOpenGLError();				/* print out errors */
		gl.glGetShaderiv(fShader, GL4.GL_COMPILE_STATUS, fragCompiled, 0);
		
		if (fragCompiled[0] == 1) {						/* see if fragmentation shader compiled */
			System.out.println("Fragmentation shader compiled!");
		} else {
			System.out.println("Fragmentation shader compile failed!");
			errorChecker.printShaderLog(fShader);
		}
		
		int vfProgram = gl.glCreateProgram(); 
		gl.glAttachShader(vfProgram, vShader);
		gl.glAttachShader(vfProgram, fShader);
		gl.glLinkProgram(vfProgram); 					/* link the two shaders */
		errorChecker.checkOpenGLError();
		gl.glGetProgramiv(vfProgram, GL4.GL_LINK_STATUS, linked, 0);
		
		if (linked[0] == 1) {							/* check for errors */
			System.out.println("Linking succeeded!");
		} else {
			System.out.println("Linking failed!");
			errorChecker.printProgramLog(vfProgram);
		}
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		return vfProgram;
	}
	
	/* Parse shader program from file. Dr. Gordon's code. */
	private String[] readShaderSource(String filename) {
		Vector<String> lines = new Vector<String>();
		Scanner sc = null;
		
		try {
			sc = new Scanner(new File(filename)); 		/* open file */
		} catch (IOException e) {
			System.err.println("IOException reading shader file: " + e);
		}
		
		System.out.println("Reading " + filename + " for shader source...");
		
		/* read lines */
		while (sc.hasNext()) {
			lines.addElement(sc.nextLine());
		}
		
		String[] program = new String[lines.size()]; 	/* make an array to hold program */
		for (int i = 0; i < lines.size(); i++) { 		/* place program into string array */
			program[i] = (String)lines.elementAt(i) + "\n";
		}
		return program;
	}
	
	/* entry point */
	public static void main(String args[]) {
		new MainFrame();
	}
}
