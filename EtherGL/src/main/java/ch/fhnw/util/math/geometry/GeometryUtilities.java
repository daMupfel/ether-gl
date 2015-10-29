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

package ch.fhnw.util.math.geometry;

import java.util.List;

import ch.fhnw.util.IntList;
import ch.fhnw.util.math.Vec3;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallback;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;

public class GeometryUtilities {
	private static final IntList TRIANGLE = new IntList(new int[] { 0, 1, 2 });

	public static IntList triangulate(List<Vec3> polygon) {
		return triangulate(Vec3.toArray(polygon));
	}
	
	public static IntList triangulate(float[] polygon) {
		if (polygon.length == 9)
			return TRIANGLE;

		final IntList result = new IntList(polygon.length * 2);

		if (isConvex(polygon)) {
			for (int i = 2; i < polygon.length / 3; i++) {
				result.add(0);
				result.add(i - 1);
				result.add(i);
			}
			return result;
		}

		GLUtessellatorCallback callback = new GLUtessellatorCallbackAdapter() {
			@Override
			public void vertex(Object vertexData) {
				if (vertexData instanceof Integer)
					result.add(((Integer) vertexData).intValue());
			}

			@Override
			public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
				System.out.println("GeometryUtil.triangulate(): combine not supported");
			}
		};
		GLUtessellator tess = GLU.gluNewTess();
		GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG_DATA, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_END, callback);

		GLU.gluTessBeginPolygon(tess, null);
		GLU.gluTessBeginContour(tess);
		double[] tmp = new double[3];
		for (int i = 0; i < polygon.length; i += 3) {
			tmp[0] = polygon[i + 0];
			tmp[1] = polygon[i + 1];
			tmp[2] = polygon[i + 2];
			GLU.gluTessVertex(tess, tmp, 0, Integer.valueOf(i / 3));
		}
		GLU.gluTessEndContour(tess);
		GLU.gluTessEndPolygon(tess);
		GLU.gluDeleteTess(tess);

		return result;
	}

	public static boolean isConvex(float[] polygon) {
		if (polygon.length < 9)
			return false;
		if (polygon.length == 9)
			return true;

		// At this point we know that the polygon has at least 4 sides.

		/*
		 * Process will be to step through the sides, 3 vertices at a time. As long the signed area for the triangles
		 * formed by each set of vertices is the same (negative or positive), then the polygon is convex.
		 * 
		 * Using a shortcut by projecting onto the (x, z) or (x, y) plane for all calculations. For a proper polygon, if
		 * the 2D projection is convex, the 3D polygon is convex.
		 * 
		 * There is one special case: A polygon that is vertical. I.e. 2D on the (x, z) plane. This is detected during
		 * the first test.
		 */

		int offset = 2; // Start by projecting to the (x, z) plane.

		int pStartVert = 0;

		float initDirection = getSignedAreaX2(polygon[pStartVert], polygon[pStartVert + 2], polygon[pStartVert + 3], polygon[pStartVert + 5],
				polygon[pStartVert + 6], polygon[pStartVert + 8]);

		if (initDirection > -2 * EPSILON && initDirection < 2 * EPSILON) {
			// The polygon is on or very close to the vertical plane. Switch to projecting on the (x, y) plane.
			offset = 1;
			initDirection = getSignedAreaX2(polygon[pStartVert], polygon[pStartVert + 1], polygon[pStartVert + 3], polygon[pStartVert + 4],
					polygon[pStartVert + 6], polygon[pStartVert + 7]);
			// Dev note: This is meant to be a strict zero test.
			if (initDirection == 0)
				// Some sort of problem. Should very rarely ever get here.
				return false;
		}

		int vertLength = polygon.length;
		for (int vertAPointer = pStartVert + 3; vertAPointer < vertLength; vertAPointer += 3) {
			int vertBPointer = vertAPointer + 3;
			if (vertBPointer >= vertLength)
				// Wrap it back to the start.
				vertBPointer = pStartVert;
			int vertCPointer = vertBPointer + 3;
			if (vertCPointer >= vertLength)
				// Wrap it back to the start.
				vertCPointer = pStartVert;
			float direction = getSignedAreaX2(polygon[vertAPointer], polygon[vertAPointer + offset], polygon[vertBPointer], polygon[vertBPointer + offset],
					polygon[vertCPointer], polygon[vertCPointer + offset]);
			if (!(initDirection < 0 && direction < 0) && !(initDirection > 0 && direction > 0))
				// The sign of the current direction is not the same as the sign of the
				// initial direction. Can't be convex.
				return false;
		}

		return true;
	}

	/**
	 * The absolute value of the returned value is two times the area of the triangle ABC.
	 * <p>
	 * A positive value indicates:
	 * </p>
	 * <ul>
	 * <li>Counterclockwise wrapping of the vertices.</li>
	 * <li>Vertex B lies to the right of line AC, looking from A toward C.</li>
	 * </ul>
	 * <p>
	 * A negative value indicates:
	 * </p>
	 * <ul>
	 * <li>Clockwise wrapping of the vertices.</li>
	 * <li>Vertex B lies to the left of line AC, looking from A toward C.</li>
	 * </ul>
	 * <p>
	 * A value of zero indicates that all points are collinear or represent the same point.
	 * </p>
	 * <p>
	 * This is a low cost operation.
	 * </p>
	 * 
	 * @param ax
	 *            The x-value for vertex A in triangle ABC
	 * @param ay
	 *            The y-value for vertex A in triangle ABC
	 * @param bx
	 *            The x-value for vertex B in triangle ABC
	 * @param by
	 *            The y-value for vertex B in triangle ABC
	 * @param cx
	 *            The x-value for vertex C in triangle ABC
	 * @param cy
	 *            The y-value for vertex C in triangle ABC
	 * @return The absolute value of the returned value is two times the area of the triangle ABC.
	 */
	private static float getSignedAreaX2(float ax, float ay, float bx, float by, float cx, float cy) {
		// References:
		// http://softsurfer.com/Archive/algorithm_0101/algorithm_0101.htm#Modern%20Triangles
		// http://mathworld.wolfram.com/TriangleArea.html (Search for "signed".)
		return (bx - ax) * (cy - ay) - (cx - ax) * (by - ay);
	}

	public static float[] calculateNormals(float[] triangles) {
		float[] normals = new float[triangles.length];
		for (int i = 0; i < triangles.length; i += 9) {
			Vec3 n;
			Vec3 a = new Vec3(triangles[i + 3] - triangles[i], triangles[i + 4] - triangles[i + 1], triangles[i + 5] - triangles[i + 2]);
			Vec3 b = new Vec3(triangles[i + 6] - triangles[i], triangles[i + 7] - triangles[i + 1], triangles[i + 8] - triangles[i + 2]);
			n = a.cross(b).normalize();
			if (n == null)
				n = new Vec3(0, 0, 1);
			normals[i] = normals[i + 3] = normals[i + 6] = n.x;
			normals[i + 1] = normals[i + 4] = normals[i + 7] = n.y;
			normals[i + 2] = normals[i + 5] = normals[i + 8] = n.z;
		}
		return normals;
	}

	public static float intersectRayWithTriangle(Vec3 rayOrigin, Vec3 rayDirection, float[] triangle, int index) {
		return intersectRayWithTriangleOrPlane(rayOrigin, rayDirection, triangle, index, true);
	}

	public static float intersectScreenRayWithTriangle(float x, float y, float[] triangle, int index) {
		return intersectRayWithTriangleOrPlane(new Vec3(x, y, 0), new Vec3(0, 0, -1), triangle, index, true);
	}

	public static float intersectRayWithPlane(Vec3 rayOrigin, Vec3 rayDirection, float[] triangle, int index) {
		return intersectRayWithTriangleOrPlane(rayOrigin, rayDirection, triangle, index, false);
	}

	public static float intersectScreenRayWithPlane(float x, float y, float[] triangle, int index) {
		return intersectRayWithTriangleOrPlane(new Vec3(x, y, 0), new Vec3(0, 0, -1), triangle, index, false);
	}

	// http://en.wikipedia.org/wiki/Möller–Trumbore_intersection_algorithm
	private static final float EPSILON = 0.000001f;

	private static float intersectRayWithTriangleOrPlane(Vec3 rayOrigin, Vec3 rayDirection, float[] triangle, int index, boolean testBounds) {
		Vec3 o = rayOrigin;
		Vec3 d = rayDirection;

		// edge e1 = p2 - p1
		float e1x = triangle[index + 3] - triangle[index];
		float e1y = triangle[index + 4] - triangle[index + 1];
		float e1z = triangle[index + 5] - triangle[index + 2];

		// edge e2 = p3 - p1
		float e2x = triangle[index + 6] - triangle[index];
		float e2y = triangle[index + 7] - triangle[index + 1];
		float e2z = triangle[index + 8] - triangle[index + 2];

		// Vec3 p = d x e2
		float px = d.y * e2z - d.z * e2y;
		float py = d.z * e2x - d.x * e2z;
		float pz = d.x * e2y - d.y * e2x;

		// float det = e1 * p
		float det = e1x * px + e1y * py + e1z * pz;

		if (det > -EPSILON && det < EPSILON)
			return Float.POSITIVE_INFINITY;
		float detInv = 1f / det;

		// Vec3 t = o - p1 (distance from p1 to ray origin)
		float tx = o.x - triangle[index];
		float ty = o.y - triangle[index + 1];
		float tz = o.z - triangle[index + 2];

		// float u = (t * p) * detInv
		float u = (tx * px + ty * py + tz * pz) * detInv;

		if (testBounds && (u < 0 || u > 1))
			return Float.POSITIVE_INFINITY;

		// Vec3 q = t x e1
		float qx = ty * e1z - tz * e1y;
		float qy = tz * e1x - tx * e1z;
		float qz = tx * e1y - ty * e1x;

		// float v = (d, q) * detInv
		float v = (d.x * qx + d.y * qy + d.z * qz) * detInv;

		if (testBounds && (v < 0 || u + v > 1))
			return Float.POSITIVE_INFINITY;

		// t = (e2 * q) * detInv;
		float t = (e2x * qx + e2y * qy + e2z * qz) * detInv;

		// done
		return (t > EPSILON) ? t : Float.POSITIVE_INFINITY;
	}

	public static boolean is2DPointInTriangle(float x, float y, float[] triangle) {
		return is2DPointInTriangle(x, y, triangle, 0);
	}

	public static boolean is2DPointInTriangle(float x, float y, float[] triangle, int index) {
		boolean b1 = sign(x, y, triangle[index], triangle[index + 1], triangle[index + 3], triangle[index + 4]) < 0.0f;
		boolean b2 = sign(x, y, triangle[index + 3], triangle[index + 4], triangle[index + 6], triangle[index + 7]) < 0.0f;
		boolean b3 = sign(x, y, triangle[index + 6], triangle[index + 7], triangle[index], triangle[index + 1]) < 0.0f;
		return ((b1 == b2) && (b2 == b3));
	}

	private static float sign(float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
		return (p1x - p3x) * (p2y - p3y) - (p2x - p3x) * (p1y - p3y);
	}

	public static boolean is2DPointInPolygon(float x, float y, float[] polygon) {
		boolean oddNodes = false;
		int j = polygon.length - 3;
		for (int i = 0; i < polygon.length; i += 3) {
			float ax = polygon[i];
			float ay = polygon[i + 1];
			float bx = polygon[j];
			float by = polygon[j + 1];
			if ((ay < y && by >= y) || (by < y && ay >= y)) {
				if (ax + (y - ay) / (by - ay) * (bx - ax) < x) {
					oddNodes = !oddNodes;
				}
			}
			j = i;
		}
		return oddNodes;
	}

	public static boolean is2DPointInPolygon(float x, float y, List<Vec3> polygon) {
		boolean oddNodes = false;
		int j = polygon.size() - 1;
		for (int i = 0; i < polygon.size(); i++) {
			Vec3 a = polygon.get(i);
			Vec3 b = polygon.get(j);
			if ((a.y < y && b.y >= y) || (b.y < y && a.y >= y)) {
				if (a.x + (y - a.y) / (b.y - a.y) * (b.x - a.x) < x) {
					oddNodes = !oddNodes;
				}
			}
			j = i;
		}
		return oddNodes;
	}
}
