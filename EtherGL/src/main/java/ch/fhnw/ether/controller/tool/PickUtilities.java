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

package ch.fhnw.ether.controller.tool;

import java.util.Map;
import java.util.TreeMap;

import ch.fhnw.ether.scene.I3DObject;
import ch.fhnw.ether.scene.camera.IViewCameraState;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.IGeometryAttribute;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.ProjectionUtilities;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;
import ch.fhnw.util.math.geometry.GeometryUtilities;

/**
 * Utilities for 3D object picking
 */
public final class PickUtilities {
	public enum PickMode {
		POINT,
		// TODO: not implemented yet
		// INSIDE, 
		// INTERSECT
	}

	private static final float PICK_DISTANCE = 5;

	public static Map<Float, I3DObject> pickFromScene(PickMode mode, int x, int y, int w, int h, IView view) {
		IViewCameraState vcs = view.getController().getRenderManager().getViewCameraState(view);
		final Map<Float, I3DObject> pickables = new TreeMap<>();
		float[][] transformedData = new float[1][0];
		for (I3DObject object : view.getController().getScene().get3DObjects()) {
			BoundingBox b = object.getBounds();
			
			if (b == null)
				continue;
			float d = pickBoundingBox(mode, x, y, w, h, vcs, b);
			if (d == Float.POSITIVE_INFINITY)
				continue;

			if (!(object instanceof IMesh)) {
				pickables.put(d, object);
				continue;
			}
			
			IMesh mesh = (IMesh)object;
			IGeometry geometry = mesh.getGeometry();
			Mat4 transform = mesh.getTransform();
			mesh.getGeometry().inspect(0, (IGeometryAttribute attribute, float[] data) -> {
				if (transform != Mat4.ID) {
					if (data.length > transformedData[0].length)
						transformedData[0] = new float[data.length];
					data = transform.transform(data, transformedData[0]);
				}
				
				float dd = Float.POSITIVE_INFINITY;
				switch (geometry.getType()) {
				case LINES:
					dd = pickEdges(mode, x, y, w, h, vcs, data);
					break;
				case POINTS:
					dd = pickPoints(mode, x, y, w, h, vcs, data);
					break;
				case TRIANGLES:
					dd = pickTriangles(mode, x, y, w, h, vcs, data);
					break;
				}
				if (dd < Float.POSITIVE_INFINITY)
					pickables.put(dd, object);
			});
		}
		return pickables;
	}

	public static float pickBoundingBox(PickMode mode, int x, int y, int w, int h, IViewCameraState vcs, BoundingBox bounds) {
		BoundingBox b = new BoundingBox();
		float xmin = bounds.getMinX();
		float xmax = bounds.getMaxX();
		float ymin = bounds.getMinY();
		float ymax = bounds.getMaxY();
		float zmin = bounds.getMinZ();
		float zmax = bounds.getMaxZ();

		float[] v = new float[] { xmin, ymin, zmin, xmin, ymin, zmax, xmin, ymax, zmin, xmin, ymax, zmax, xmax, ymin, zmin, xmax, ymin, zmax, xmax, ymax, zmin,
				xmax, ymax, zmax, };
		b.add(ProjectionUtilities.projectToScreen(vcs.getViewProjMatrix(), vcs.getViewport(), v));
		b.grow(PICK_DISTANCE, PICK_DISTANCE, 0);

		if (b.getMaxZ() > 0 && x > b.getMinX() && x < b.getMaxX() && y > b.getMinY() && y < b.getMaxY())
			return Math.max(0, b.getMinZ());

		return Float.POSITIVE_INFINITY;
	}

	public static float pickTriangles(PickMode mode, int x, int y, int w, int h, IViewCameraState vcs, float[] triangles) {
		triangles = ProjectionUtilities.projectToScreen(vcs, triangles);

		Vec3 o = new Vec3(x, y, 0);
		Vec3 d = Vec3.Z;
		float zMin = Float.POSITIVE_INFINITY;
		for (int i = 0; i < triangles.length; i += 9) {
			float z = GeometryUtilities.intersectRayWithTriangle(o, d, triangles, i);
			zMin = Math.min(zMin, z);
		}
		return zMin;
	}

	public static float pickEdges(PickMode mode, int x, int y, int w, int h, IViewCameraState vcs, float[] edges) {
		edges = ProjectionUtilities.projectToScreen(vcs, edges);

		float zMin = Float.POSITIVE_INFINITY;
		for (int i = 0; i < edges.length; i += 6) {
			float dx = edges[i + 3] - edges[i];
			float dy = edges[i + 4] - edges[i + 1];
			float dl2 = dx * dx + dy * dy;
			if (dl2 == 0)
				continue;

			float mx = x - edges[i];
			float my = y - edges[i + 1];

			float t = (mx * dx + my * dy) / dl2;
			if (t < 0 || t > 1)
				continue;

			float px = t * dx;
			float py = t * dy;

			float d2 = (mx - px) * (mx - px) + (my - py) * (my - py);
			if (d2 > PICK_DISTANCE * PICK_DISTANCE)
				continue;

			float z = edges[i + 2] + t * (edges[i + 5] - edges[i + 2]);
			zMin = Math.min(zMin, z);
		}
		return zMin;
	}

	public static float pickPoints(PickMode mode, int x, int y, int w, int h, IViewCameraState vcs, float[] points) {
		points = ProjectionUtilities.projectToScreen(vcs, points);

		float zMin = Float.POSITIVE_INFINITY;
		for (int i = 0; i < points.length; i += 3) {
			float d = (float) Math.sqrt((points[i] - x) * (points[i] - x) + (points[i + 1] - y) * (points[i + 1] - y));
			if (d < PICK_DISTANCE)
				zMin = Math.min(zMin, points[i + 2]);
		}
		return zMin;
	}
}
