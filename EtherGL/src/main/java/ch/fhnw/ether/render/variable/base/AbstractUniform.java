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
import ch.fhnw.ether.render.variable.IShaderUniform;
import ch.fhnw.ether.scene.attribute.ITypedAttribute;

import com.jogamp.opengl.GL3;

public abstract class AbstractUniform<T> extends AbstractVariable<T> implements IShaderUniform<T> {
	private Supplier<T> supplier;

	protected AbstractUniform(ITypedAttribute<T> attribute, String shaderName) {
		super(attribute, shaderName);
	}

	protected AbstractUniform(ITypedAttribute<T> attribute, String shaderName, Supplier<T> supplier) {
		super(attribute, shaderName);
		this.supplier = supplier;
		if (supplier != null)
			update();
	}

	protected AbstractUniform(String id, String shaderName) {
		super(id, shaderName);
	}

	protected AbstractUniform(String id, String shaderName, Supplier<T> supplier) {
		super(id, shaderName);
		this.supplier = supplier;
		if (supplier != null)
			update();
	}

	protected final T fetch() {
		return supplier.get();
	}

	@Override
	public final boolean hasSupplier() {
		return supplier != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void setSupplier(Supplier<?> supplier) {
		this.supplier = (Supplier<T>) supplier;
	}

	@Override
	public void disable(GL3 gl, Program program) {
	}

	@Override
	protected final int resolveShaderIndex(GL3 gl, Program program, String shaderName) {
		return program.getUniformLocation(gl, shaderName);
	}

	@Override
	public String toString() {
		return super.toString() + "[" + supplier + "]";
	}
}
