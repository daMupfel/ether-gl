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

package ch.fhnw.ether.render.forward;

import java.util.List;

import ch.fhnw.ether.render.Renderable;
import ch.fhnw.ether.render.ShaderBuilder;
import ch.fhnw.ether.render.shader.builtin.ShadowVolumeShader;
import ch.fhnw.ether.render.shader.builtin.TrivialDeviceSpaceShader;
import ch.fhnw.ether.scene.attribute.IAttributeProvider;
import ch.fhnw.ether.scene.light.GenericLight;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Flags;
import ch.fhnw.ether.scene.mesh.MeshLibrary;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.EmptyMaterial;
import ch.fhnw.util.color.RGBA;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

public final class ShadowVolumes {
	private static final IMesh OVERLAY_MESH = new DefaultMesh(new EmptyMaterial(), DefaultGeometry.createV(Primitive.TRIANGLES,
			MeshLibrary.DEFAULT_QUAD_TRIANGLES));

	private ShadowVolumeShader volumeShader;
	private Renderable overlay;

	private int lightIndex;
	private float extrudeDistance = 1000;
	private RGBA volumeColor = new RGBA(1, 0, 0, 0.2f);
	private RGBA overlayColor = new RGBA(0, 0, 0, 0.9f);

	public ShadowVolumes(List<IAttributeProvider> providers) {
		volumeShader = ShaderBuilder.create(new ShadowVolumeShader(() -> lightIndex, () -> extrudeDistance, () -> volumeColor), null, providers);

		overlay = new Renderable(new TrivialDeviceSpaceShader(() -> overlayColor), OVERLAY_MESH, providers);
	}

	// http://ogldev.atspace.co.uk/www/tutorial40/tutorial40.html
	public void render(GL3 gl, IMesh.Queue pass, boolean interactive, List<Renderable> renderables, List<GenericLight> lights) {
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_ALPHA);
		gl.glDepthMask(false);
		gl.glEnable(GL3.GL_DEPTH_CLAMP);

		lightIndex = 0;
		for (@SuppressWarnings("unused") GenericLight light : lights) {
			gl.glClear(GL.GL_STENCIL_BUFFER_BIT);

			gl.glColorMask(false, false, false, false);

			gl.glEnable(GL.GL_STENCIL_TEST);

			gl.glStencilFuncSeparate(GL.GL_FRONT, GL.GL_ALWAYS, 0, 0xffffffff);
			gl.glStencilOpSeparate(GL.GL_FRONT, GL.GL_KEEP, GL.GL_DECR_WRAP, GL.GL_KEEP);

			gl.glStencilFuncSeparate(GL.GL_BACK, GL.GL_ALWAYS, 0, 0xffffffff);
			gl.glStencilOpSeparate(GL.GL_BACK, GL.GL_KEEP, GL.GL_INCR_WRAP, GL.GL_KEEP);

			volumeShader.update(gl);
			volumeShader.enable(gl);
			for (Renderable renderable : renderables) {
				if (renderable.containsFlag(Flags.INTERACTIVE_VIEWS_ONLY) && !interactive)
					continue;
				if (renderable.containsFlag(Flags.DONT_CAST_SHADOW))
					continue;
				if (renderable.getQueue() != pass)
					continue;

				volumeShader.render(gl, renderable.getBuffer());
			}
			volumeShader.disable(gl);

			gl.glColorMask(true, true, true, true);

			gl.glStencilFunc(GL.GL_NOTEQUAL, 0x0, 0xffffffff);
			gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);

			overlay.update(gl);
			overlay.render(gl);

			gl.glDisable(GL.GL_STENCIL_TEST);

			lightIndex++;
		}
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL.GL_BLEND);
		gl.glDepthMask(true);
		gl.glDisable(GL3.GL_DEPTH_CLAMP);
	}
}
