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

public class FaceParser extends LineParser {
	private int[] vIndices;
	private int[] nIndices;
	private int[] tIndices;

	public FaceParser() {
	}

	@Override
	public void parse(WavefrontObject object) {
		parseLine(object, words.length - 1);
	}

	private void parseLine(WavefrontObject object, int vertexCount) {
		String[] rawFaces = null;
		int currentValue;

		vIndices = new int[vertexCount];
		nIndices = null;
		tIndices = null;

		for (int i = 0; i < vertexCount; i++) {
			rawFaces = words[i + 1].split("/");

			// save vertex
			vIndices[i] = Integer.parseInt(rawFaces[0]) - 1;
			if(vIndices[i] < 0) 
				vIndices[i] = object.getVertices().size() + vIndices[i] + 1;
			
			if (rawFaces.length == 1)
				continue;

			// save texcoords
			if (!rawFaces[1].equals("")) {
				currentValue = Integer.parseInt(rawFaces[1]);
				// This is to compensate the fact that if no texture is
				// in the obj file, sometimes '1' is put instead of
				// 'blank' (we find coord1/1/coord3 instead of
				// coord1//coord3 or coord1/coord3)
				if (currentValue <= object.getTexCoords().size()) {
					if (tIndices == null)
						tIndices = new int[vertexCount];
					tIndices[i] = currentValue - 1;
				}
				if(tIndices[i] < 0) tIndices[i] = object.getTexCoords().size() + tIndices[i] + 1;
			}
			
			if (rawFaces.length == 2)
				continue;

			// save normal
			if (nIndices == null)
				nIndices = new int[vertexCount];
			nIndices[i] = Integer.parseInt(rawFaces[2]) - 1;
			if(nIndices[i] < 0) nIndices[i] = object.getNormals().size() + nIndices[i] + 1;
		}
	}

	@Override
	public void incoporateResults(WavefrontObject object) {
		object.getCurrentGroup().addFace(new Face(vIndices, tIndices, nIndices));
	}
}
