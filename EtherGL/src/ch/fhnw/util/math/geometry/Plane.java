/*
 * Copyright (c) 2013 - 2014 Stefan Muller Arisona, Simon Schubiger, Samuel von Stachelski
 * Copyright (c) 2013 - 2014 FHNW & ETH Zurich
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

import ch.fhnw.util.math.Vec3;

/**
 * Very basic plane class. Loosely based on Apache Commons Math3.
 * 
 * @author radar
 *
 */
public class Plane {
	private Vec3 origin;
	private Vec3 normal;
	private float offset;

	public Plane(Vec3 normal) {
		this.origin = Vec3.ZERO;
		this.normal = normal.normalize();
		this.offset = 0;
	}

	public Plane(Vec3 origin, Vec3 normal) {
		this.origin = origin;
		this.normal = normal.normalize();
		this.offset = -origin.dot(this.normal);
	}

	public Vec3 getOrigin() {
		return origin;
	}

	public Vec3 getNormal() {
		return normal;
	}

	public float getOffset() {
		return offset;
	}

	public Vec3 intersection(Line line) {
		float dot = normal.dot(line.getDirection());
		if (dot == 0)
			return null;
		float k = -(offset + normal.dot(line.getOrigin())) / dot;
		return line.getOrigin().add(line.getDirection().scale(k));
	}
}