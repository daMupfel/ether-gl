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

package ch.fhnw.ether.formats.obj;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.fhnw.ether.formats.AbstractModelReader;
import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.util.IntList;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.math.Vec2;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.GeometryUtilities;

public class OBJReader extends AbstractModelReader {
	private final List<IMesh> meshes = new ArrayList<>();

	public OBJReader(File file) throws IOException {
		this(file.toURI().toURL());
	}
	
	public OBJReader(URL resource) throws IOException {
		super(resource);
		decode(resource.getFile(), resource.openStream());
	}
	
	@Override
	public List<IMesh> getMeshes() {
		return Collections.unmodifiableList(meshes);
	}

	private List<IMesh> decode(String path, InputStream in) {
		WavefrontObject obj = new WavefrontObject(path, in);
		List<Vec3> vertices = obj.getVertices();
		List<Vec3> normals = obj.getNormals();
		List<Vec2> texCoords = obj.getTexCoords();

		for (Group group : obj.getGroups()) {
			List<Face> faces = group.getFaces();
			if (faces.isEmpty())
				continue;
			boolean hasNormals = faces.get(0).nIndices != null;
			boolean hasTexCoords = faces.get(0).tIndices != null;

			List<Vec3> triVertices = new ArrayList<>();
			List<Vec3> triNormals = hasNormals ? new ArrayList<>() : null;
			List<Vec2> triTexCoords = hasTexCoords ? new ArrayList<>() : null;

			for (Face face : group.getFaces()) {
				final int[] vs = face.vIndices;
				final int[] ns = face.nIndices;
				final int[] ts = face.tIndices;

				List<Vec3> polyVertices = new ArrayList<>();
				for (int i = 0; i < vs.length; ++i) {
					polyVertices.add(vertices.get(vs[i]));
				}

				IntList triangulation = GeometryUtilities.triangulate(polyVertices);

				for (int i = 0; i < triangulation.size(); ++i) {
					int idx = triangulation.get(i);
					triVertices.add(vertices.get(vs[idx]));
					if (hasNormals)
						triNormals.add(ns != null ? normals.get(ns[idx]) : Vec3.Z);
					if (hasTexCoords)
						triTexCoords.add(ts != null ? texCoords.get(ts[idx]) : Vec2.ZERO);
				}
			}

			// TODO: proper material handling
			Material mat = group.getMaterial();
			IMaterial material;
			if (mat != null) {
				Frame frame = mat.getTexture();
				material = new ShadedMaterial(RGB.BLACK, mat.getKa(), mat.getKd(), mat.getKs(), mat.getShininess(), 1, 1, frame != null ? new Texture(mat.getTexture()) : null);
			} else {
				material = new ShadedMaterial(RGB.WHITE);
			}
			
			float[] tv = Vec3.toArray(triVertices);
			float[] tn = hasNormals ? Vec3.toArray(triNormals) : GeometryUtilities.calculateNormals(tv);
			float[] tt = Vec2.toArray(triTexCoords);

			IGeometry geometry;
			if (hasTexCoords)
				geometry = DefaultGeometry.createVNM(Primitive.TRIANGLES, tv, tn, tt);
			else
				geometry = DefaultGeometry.createVN(Primitive.TRIANGLES, tv, tn);

			DefaultMesh mesh = new DefaultMesh(material, geometry);
			mesh.setName(path + '/' + group.getName());
			meshes.add(mesh);
		}
		return meshes;
	}
}
