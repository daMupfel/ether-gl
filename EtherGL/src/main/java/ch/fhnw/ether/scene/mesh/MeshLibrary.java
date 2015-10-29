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

package ch.fhnw.ether.scene.mesh;

import java.util.List;

import ch.fhnw.ether.scene.mesh.IMesh.Flags;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.GeometryUtilities;

public class MeshLibrary {

	//@formatter:off
	public static final float[] UNIT_CUBE_TRIANGLES = {
		// bottom
		-0.5f, -0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, +0.5f, -0.5f,
		-0.5f, -0.5f, -0.5f, +0.5f, +0.5f, -0.5f, +0.5f, -0.5f, -0.5f,

		// top
		+0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, -0.5f, +0.5f, +0.5f, 
		+0.5f, -0.5f, +0.5f, -0.5f, +0.5f, +0.5f, -0.5f, -0.5f, +0.5f,

		// front
		-0.5f, -0.5f, -0.5f, +0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, 
		-0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, -0.5f, -0.5f, +0.5f,

		// back
		+0.5f, +0.5f, -0.5f, -0.5f, +0.5f, -0.5f, -0.5f, +0.5f, +0.5f, 
		+0.5f, +0.5f, -0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, +0.5f,

		// left
		-0.5f, +0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, +0.5f, 
		-0.5f, +0.5f, -0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, +0.5f,

		// right
		+0.5f, -0.5f, -0.5f, +0.5f, +0.5f, -0.5f, +0.5f, +0.5f, +0.5f, 
		+0.5f, -0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, -0.5f, +0.5f 
	};

	public static final float[] UNIT_CUBE_NORMALS = GeometryUtilities.calculateNormals(UNIT_CUBE_TRIANGLES);
	
	public static final float[] UNIT_CUBE_EDGES = {
		// bottom
		-0.5f, -0.5f, -0.5f, -0.5f, +0.5f, -0.5f, 
		-0.5f, +0.5f, -0.5f, +0.5f, +0.5f, -0.5f,
		+0.5f, +0.5f, -0.5f, +0.5f, -0.5f, -0.5f, 
		+0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,

		// top
		+0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, 
		+0.5f, +0.5f, +0.5f, -0.5f, +0.5f, +0.5f, 
		-0.5f, +0.5f, +0.5f, -0.5f, -0.5f, +0.5f, 
		-0.5f, -0.5f, +0.5f, +0.5f, -0.5f, +0.5f,

		// vertical
		-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, +0.5f, 
		+0.5f, -0.5f, -0.5f, +0.5f, -0.5f, +0.5f, 
		+0.5f, +0.5f, -0.5f, +0.5f, +0.5f, +0.5f, 
		-0.5f, +0.5f, -0.5f, -0.5f, +0.5f, +0.5f 
	};

	public static final float[] UNIT_CUBE_POINTS = { 
		-0.5f, -0.5f, -0.5f, -0.5f, +0.5f, -0.5f, 
		+0.5f, +0.5f, -0.5f, +0.5f, -0.5f, -0.5f,
		+0.5f, -0.5f, +0.5f, +0.5f, +0.5f, +0.5f, 
		-0.5f, +0.5f, +0.5f, -0.5f, -0.5f, +0.5f
	};

	public static final float[] DEFAULT_QUAD_TRIANGLES = { 
		-1, -1, 0,   1, -1, 0,    1, 1, 0,
		-1, -1, 0,   1,  1, 0,   -1, 1, 0 
	};

	public static final float[] DEFAULT_QUAD_NORMALS = { 
		0, 0, 1, 0, 0, 1, 0, 0, 1,
		0, 0, 1, 0, 0, 1, 0, 0, 1
	};

	public static final float[] DEFAULT_QUAD_TEX_COORDS = { 
		0, 0, 1, 0, 1, 1,
		0, 0, 1, 1, 0, 1 
	};
	//@formatter:on

	private final static DefaultGeometry CUBE_GEOMETRY = DefaultGeometry.createVN(Primitive.TRIANGLES, UNIT_CUBE_TRIANGLES, UNIT_CUBE_NORMALS);

	public static IMesh createCube() {
		return createCube(new ColorMaterial(RGBA.WHITE));
	}
	
	public static IMesh createCube(IMaterial material) {
		return new DefaultMesh(material, CUBE_GEOMETRY.copy());
	}
	
	public static IMesh createGroundPlane() {
		return createGroundPlane(new ShadedMaterial(RGB.WHITE));
	}
	
	public static IMesh createGroundPlane(IMaterial material) {
		float e = 1000;
		float z = 0;
		float[] v = { -e, -e, z, e, -e, z, e, e, z, -e, -e, z, e, e, z, -e, e, z };
		float[] n = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
		return new DefaultMesh(material, DefaultGeometry.createVN(Primitive.TRIANGLES, v, n), Flags.DONT_CAST_SHADOW);
	}

	// FIXME: this needs some revision / organization
	
	public static void addLine(List<Vec3> dst, float x0, float y0, float x1, float y1) {
		dst.add(new Vec3(x0, y0, 0));
		dst.add(new Vec3(x1, y1, 0));
	}

	public static void addLine(List<Vec3> dst, float x0, float y0, float z0, float x1, float y1, float z1) {
		dst.add(new Vec3(x0, y0, z0));
		dst.add(new Vec3(x1, y1, z1));
	}

	public static void addRectangle(List<Vec3> dst, float x0, float y0, float x1, float y1) {
		addRectangle(dst, x0, y0, x1, y1, 0);
	}

	public static void addRectangle(List<Vec3> dst, float x0, float y0, float x1, float y1, float z) {
		dst.add(new Vec3(x0, y0, z));
		dst.add(new Vec3(x1, y0, z));
		dst.add(new Vec3(x1, y1, z));

		dst.add(new Vec3(x0, y0, z));
		dst.add(new Vec3(x1, y1, z));
		dst.add(new Vec3(x0, y1, z));
	}

	public static void addCube(List<Vec3> dst, float tx, float ty, float tz, float sx, float sy, float sz) {
		for (int i = 0; i < UNIT_CUBE_TRIANGLES.length; i += 3) {
			dst.add(new Vec3((UNIT_CUBE_TRIANGLES[i] * sx) + tx, (UNIT_CUBE_TRIANGLES[i + 1] * sy) + ty, (UNIT_CUBE_TRIANGLES[i + 2] * sz) + tz));
		}
	}
	
}
