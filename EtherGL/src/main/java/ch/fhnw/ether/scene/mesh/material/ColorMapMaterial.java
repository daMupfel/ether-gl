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

package ch.fhnw.ether.scene.mesh.material;

import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.util.color.RGBA;

public class ColorMapMaterial extends ColorMaterial {
	private volatile Texture colorMap;

	public ColorMapMaterial(Texture colorMap) {
		this(RGBA.WHITE, colorMap);
	}
	
	public ColorMapMaterial(RGBA color, Texture colorMap) {
		this(color, colorMap, false);
	}

	public ColorMapMaterial(RGBA color, Texture colorMap, boolean perVertexColor) {
		super(color, perVertexColor);
		this.colorMap = colorMap;
	}
	
	public final Texture getColorMap() {
		return colorMap;
	}
	
	public final void setColorMap(Texture colorMap) {
		this.colorMap = colorMap;
	}

	@Override
	public void getAttributes(IAttributes attributes) {
		attributes.provide(IMaterial.COLOR_MAP, () -> colorMap);
		attributes.require(IGeometry.COLOR_MAP_ARRAY);
		super.getAttributes(attributes);
	}
}
