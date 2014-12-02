/*
 * Copyright (c) 2013 - 2014 Stefan Muller Arisona, Simon Schubiger, Samuel von Stachelski
 * Copyright (c) 2013 - 2014 FHNW & ETH Zurich
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

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import ch.fhnw.ether.render.gl.Program;
import ch.fhnw.ether.scene.attribute.ITypedAttribute;
import ch.fhnw.ether.scene.mesh.material.Texture;

public class SamplerUniform extends AbstractUniform<Texture> {
	private final int unit;
	private final int target;

	private int[] tex;

	public SamplerUniform(ITypedAttribute<Texture> attribute, String shaderName, int unit, int target) {
		this(attribute.id(), shaderName, unit, target, null);
	}

	public SamplerUniform(ITypedAttribute<Texture> attribute, String shaderName, int unit, int target, Supplier<Texture> supplier) {
		this(attribute.id(), shaderName, unit, target, supplier);
	}

	public SamplerUniform(String id, String shaderName, int unit, int target, Supplier<Texture> supplier) {
		super(id, shaderName, supplier);
		this.unit = unit;
		this.target = target;
	}

	public SamplerUniform(String id, String shaderName, int unit, int target) {
		this(id, shaderName, unit, target, null);
	}

	@Override
	public void dispose(GL3 gl) {
		if (tex != null) {
			gl.glDeleteTextures(1, tex, 0);
			tex = null;
		}
	}

	@Override
	public void enable(GL3 gl, Program program) {
		Texture texture = get();
		if (texture == null)
			return;
		load(gl);
		if (tex == null)
			return;

		gl.glActiveTexture(GL.GL_TEXTURE0 + unit);
		gl.glBindTexture(target, tex[0]);
		program.setUniformSampler(gl, getShaderIndex(gl, program), unit);
		gl.glActiveTexture(GL.GL_TEXTURE0);
	}

	@Override
	public void disable(GL3 gl, Program program) {
		Texture texture = get();
		if (texture == null || tex == null)
			return;

		gl.glActiveTexture(GL.GL_TEXTURE0 + unit);
		gl.glBindTexture(target, 0);
		gl.glActiveTexture(GL.GL_TEXTURE0);
	}

	private void load(GL gl) {
		Texture texture = get();
		if (texture == null)
			return;

		if (texture.needsUpdate()) {
			if (tex == null) {
				tex = new int[1];
				gl.glGenTextures(1, tex, 0);
				gl.glBindTexture(target, tex[0]);
				gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
				gl.glTexParameteri(target, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
				gl.glTexParameterf(target, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				gl.glTexParameterf(target, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			}

			texture.load(gl, target, tex[0]);
		}
	}
}