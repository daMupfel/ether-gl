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

package ch.fhnw.ether.examples.raytracing.surface;

import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.Line;

public class Plane implements IParametricSurface {

	private float distance = 0;
	private Vec3 normal = Vec3.Z;

	public Plane(Vec3 normal, float distance) {
		this.normal = normal;
		this.distance = distance;
	}

	public Plane() {
	}

	// From http://www.trenki.net/files/Raytracing1.pdf
	@Override
	public Vec3 intersect(Line ray) {
		float t = -(normal.dot(ray.getOrigin()) + distance) / normal.dot(ray.getDirection());
		return t < 0 ? null : ray.getOrigin().add(ray.getDirection().scale(t));
	}

	@Override
	public Vec3 getNormalAt(Vec3 position) {
		return normal;
	}

	@Override
	public String toString() {
		return "plane(n=" + normal + ",d=" + distance + ")";
	}

	@Override
	public void setPosition(Vec3 position) {
	}

	@Override
	public Vec3 getPosition() {
		return normal.scale(distance);
	}

}
