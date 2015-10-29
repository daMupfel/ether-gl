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
import ch.fhnw.util.color.RGB;

// TODO: support for per vertex color (if that's ever needed)
public class ShadedMaterial extends AbstractMaterial {
	private volatile RGB emission;
	private volatile RGB ambient;
	private volatile RGB diffuse;
	private volatile RGB specular;
	private volatile float shininess;
	private volatile float strength;
	private volatile float alpha;
	
	private volatile Texture colorMap;
	
	public ShadedMaterial(RGB diffuse) {
		this(RGB.BLACK, RGB.BLACK, diffuse, RGB.BLACK, 0, 0, 1);
	}

	public ShadedMaterial(RGB emission, RGB ambient, RGB diffuse, RGB specular, float shininess, float strength, float alpha) {
		this(emission, ambient, diffuse, specular, shininess, strength, alpha, null);
	}

	public ShadedMaterial(RGB emission, RGB ambient, RGB diffuse, RGB specular, float shininess, float strength, float alpha, Texture colorMap) {
		this.emission = emission;
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
		this.strength = strength;
		this.alpha = alpha;
		this.colorMap = colorMap;
	}
	
	public final RGB getEmission() {
		return emission;
	}
	
	public final void setEmission(RGB emission) {
		this.emission = emission;
	}
	
	public final RGB getAmbient() {
		return ambient;
	}
	
	public final void setAmbient(RGB ambient) {
		this.ambient = ambient;
	}
	
	public final RGB getDiffuse() {
		return diffuse;
	}
	
	public final void setDiffuse(RGB diffuse) {
		this.diffuse = diffuse;
	}
	
	public final RGB getSpecular() {
		return specular;
	}
	
	public final void setSpecular(RGB specular) {
		this.specular = specular;
	}
	
	public final float getShininess() {
		return shininess;
	}
	
	public final void setShininess(float shininess) {
		this.shininess = shininess;
	}
	
	public final float getStrength() {
		return strength;
	}
	
	public final void setStrength(float strength) {
		this.strength = strength;
	}
	
	public final float getAlpha() {
		return alpha;
	}
	
	public final void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	public final Texture getColorMap() {
		return colorMap;
	}
	
	// TODO: need to handle this correctly (with / without color map)
	public final void setColorMap(Texture colorMap) {
		this.colorMap = colorMap;
	}

	
	@Override
	public void getAttributes(IAttributes attributes) {
		attributes.provide(IMaterial.EMISSION, () -> emission);
		attributes.provide(IMaterial.AMBIENT, () -> ambient);
		attributes.provide(IMaterial.DIFFUSE, () -> diffuse);
		attributes.provide(IMaterial.SPECULAR, () -> specular);
		attributes.provide(IMaterial.SHININESS, () -> shininess);
		attributes.provide(IMaterial.STRENGTH, () -> strength);
		attributes.provide(IMaterial.ALPHA, () -> alpha);

		if (colorMap != null) {
			attributes.provide(IMaterial.COLOR_MAP, () -> colorMap);
			attributes.require(IGeometry.COLOR_MAP_ARRAY);
		}
	}
}
