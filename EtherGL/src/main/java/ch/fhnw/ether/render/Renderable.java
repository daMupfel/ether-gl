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

package ch.fhnw.ether.render;

import java.util.List;

import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.scene.attribute.IAttributeProvider;
import ch.fhnw.ether.scene.mesh.IMesh;

import com.jogamp.opengl.GL3;

public final class Renderable {
	private final IShader shader;
	private final IMesh mesh;
	private final IVertexBuffer buffer;

	// FIXME: let's get rid of this providers list somehow (an unmodifiable map of provided arrays would be fine)
	public Renderable(IMesh mesh, List<IAttributeProvider> providers) {
		this(null, mesh, providers);
	}

	public Renderable(IShader shader, IMesh mesh, List<IAttributeProvider> providers) {
		this.shader = ShaderBuilder.create(shader, mesh, providers);
		this.mesh = mesh;
		this.buffer = new VertexBuffer(this.shader, this.mesh);

		// make sure update flag is set, so everything get initialized on the next render cycle
		mesh.updateRequest(null);
	}

	public void update(GL3 gl) {
		if (mesh.needsMaterialUpdate())
			shader.update(gl);

		if (mesh.needsGeometryUpdate())
			buffer.load(gl, shader, mesh);
	}

	public void render(GL3 gl) {
		shader.enable(gl);
		shader.render(gl, buffer);
		shader.disable(gl);
	}

	public IMesh.Queue getQueue() {
		return mesh.getQueue();
	}

	public boolean containsFlag(IMesh.Flags flag) {
		return mesh.getFlags().contains(flag);
	}

	public IVertexBuffer getBuffer() {
		return buffer;
	}

	@Override
	public String toString() {
		return "renderable[queue=" + mesh.getQueue() + " shader=" + shader + " buffer=" + buffer + "]";
	}
}
