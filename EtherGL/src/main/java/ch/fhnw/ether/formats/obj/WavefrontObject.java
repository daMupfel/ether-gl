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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fhnw.util.math.Vec2;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;

public class WavefrontObject {
	private final String fileName;
	private final String contextFolder;

	private List<Vec3> vertices  = new ArrayList<>();
	private List<Vec3> normals   = new ArrayList<>();
	private List<Vec2> texCoords = new ArrayList<>();

	private List<Group> groups   = new ArrayList<>();

	private Map<String, Material> materials = new HashMap<>();

	private BoundingBox bounds;

	private ObjLineParserFactory parserFactory;

	private Material currentMaterial;
	private Group currentGroup;

	public WavefrontObject(String path) {
		this.fileName = path;
		String folder = "";
		int lastSlashIndex = path.lastIndexOf('/');
		if (lastSlashIndex != -1)
			folder = path.substring(0, lastSlashIndex + 1);

		lastSlashIndex = path.lastIndexOf('\\');
		if (lastSlashIndex != -1)
			folder = path.substring(0, lastSlashIndex + 1);
		this.contextFolder = folder;
	}
	
	public WavefrontObject(String path, InputStream in) {
		this(path);
		try {
			parse(in);
		} catch (Exception e) {
			System.out.println("Error, could not load obj:" + path);
		}
	}

	public String getContextfolder() {
		return contextFolder;
	}

	public List<Vec3> getVertices() {
		return vertices;
	}

	public List<Vec3> getNormals() {
		return normals;
	}

	public List<Vec2> getTexCoords() {
		return texCoords;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public Map<String, Material> getMaterials() {
		return this.materials;
	}

	public BoundingBox getBounds() {
		if (bounds == null) {
			bounds = new BoundingBox();
			bounds.add(getVertices());
		}
		return bounds;
	}

	public Material getCurrentMaterial() {
		return currentMaterial;
	}

	public void setCurrentMaterial(Material currentMaterial) {
		this.currentMaterial = currentMaterial;
	}

	public Group getCurrentGroup() {
		if (currentGroup == null) {
			setCurrentGroup(new Group("default"));
		}
		return currentGroup;
	}

	public void setCurrentGroup(Group group) {
		groups.add(group);
		currentGroup = group;
	}

	private void parse(InputStream input) {
		parserFactory = new ObjLineParserFactory(this);

		try (BufferedReader in = new BufferedReader(new InputStreamReader(input))) {
			for (String currentLine = null; (currentLine = in.readLine()) != null;) {
				parseLine(currentLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading file :'" + fileName + "'");
		}
	}

	private void parseLine(String currentLine) {
		if ("".equals(currentLine))
			return;

		LineParser parser = parserFactory.getLineParser(currentLine);
		if (parser != null) {
			parser.parse(this);
			parser.incoporateResults(this);
		}
	}

	public void write(PrintWriter out) {
		for(Vec3 v : vertices)
			out.println("v " + v.x + " " + v.y + " " + v.z);
		for(Vec2 t : texCoords)
			out.println("vt " + t.x + " " + t.y);
		for(Vec3 n : normals)
			out.println("vn " + n.x + " " + n.y + " " + n.z);
		for(Group g : groups)
			g.write(out);
	}
}
