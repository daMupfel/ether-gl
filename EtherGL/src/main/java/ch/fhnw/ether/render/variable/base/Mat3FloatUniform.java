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

package ch.fhnw.ether.render.variable.base;

import java.util.function.Supplier;

import ch.fhnw.ether.render.gl.Program;
import ch.fhnw.ether.scene.attribute.ITypedAttribute;
import ch.fhnw.util.math.Mat3;

import com.jogamp.opengl.GL3;

public class Mat3FloatUniform extends AbstractUniform<Mat3> {
	private float[] value;
	
	public Mat3FloatUniform(ITypedAttribute<Mat3> attribute, String shaderName) {
		super(attribute, shaderName);
	}

	public Mat3FloatUniform(ITypedAttribute<Mat3> attribute, String shaderName, Supplier<Mat3> supplier) {
		super(attribute, shaderName, supplier);
	}

	public Mat3FloatUniform(String id, String shaderName, Supplier<Mat3> supplier) {
		super(id, shaderName, supplier);
	}

	public Mat3FloatUniform(String id, String shaderName) {
		super(id, shaderName);
	}

	@Override
	public final void update() {
		value = fetch().toArray();
	}

	@Override
	public final void enable(GL3 gl, Program program) {
		program.setUniformMat3(gl, getShaderIndex(gl, program), value);
	}
}
