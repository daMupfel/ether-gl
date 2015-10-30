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

package ch.fhnw.ether.mapping.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.controller.tool.AbstractTool;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.MeshLibrary;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;

public final class FillTool extends AbstractTool {
	static final String[] FILL_HELP = { "Fill Tool for Projector Adjustment", "", "[0] Return" };

	private final IMesh quads = makeQuads();

	public FillTool(IController controller) {
		super(controller);
	}

	@Override
	public void activate() {
		getController().getRenderManager().addMesh(quads);
	}

	@Override
	public void deactivate() {
		getController().getRenderManager().removeMesh(quads);
		getController().enableViews(null);
	}

	@Override
	public void refresh(IView view) {
		getController().enableViews(Collections.singleton(getController().getCurrentView()));
	}

	private static DefaultMesh makeQuads() {
		List<Vec3> dst = new ArrayList<>();
		MeshLibrary.addRectangle(dst, -1.0f, -1.0f, -0.1f, -0.1f);
		MeshLibrary.addRectangle(dst, 0.1f, -1.0f, 1.0f, -0.1f);
		MeshLibrary.addRectangle(dst, 0.1f, 0.1f, 1.0f, 1.0f);
		MeshLibrary.addRectangle(dst, -1.0f, 0.1f, -0.1f, 1.0f);
		return new DefaultMesh(new ColorMaterial(RGBA.WHITE), DefaultGeometry.createV(Primitive.TRIANGLES, Vec3.toArray(dst)), Queue.DEVICE_SPACE_OVERLAY);
	}
}
