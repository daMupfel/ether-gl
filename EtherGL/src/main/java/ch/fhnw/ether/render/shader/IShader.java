/*
 * Copyright (c) 2013 - 2015 Stefan Muller Arisona, Simon Schubiger, Samuel von Stachelski
 * Copyright (c) 2013 - 2015 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.fhnw.ether.render.shader;

import java.util.List;

import ch.fhnw.ether.render.IVertexBuffer;
import ch.fhnw.ether.render.variable.IShaderArray;
import ch.fhnw.ether.render.variable.IShaderUniform;

import com.jogamp.opengl.GL3;

public interface IShader {
	public static String VERSION_GL_3_1_GLSL_1_40 = "140";
	public static String VERSION_GL_3_2_GLSL_1_50 = "150";
	public static String VERSION_GL_3_3_GLSL_3_30 = "330";
	public static String VERSION_GL_4_0_GLSL_3_40 = "340";
	public static String VERSION_GL_4_1_GLSL_4_10 = "410";
	public static String VERSION_GL_4_2_GLSL_4_20 = "420";
	public static String VERSION_GL_4_3_GLSL_4_30 = "420";
	public static String VERSION_GL_4_4_GLSL_4_40 = "440";
	public static String VERSION_GL_4_5_GLSL_4_50 = "450";

	String id();
	
	void update(GL3 gl);

	void enable(GL3 gl);

	void render(GL3 gl, IVertexBuffer buffer);

	void disable(GL3 gl);

	List<IShaderUniform<?>> getUniforms();

	List<IShaderArray<?>> getArrays();
}
