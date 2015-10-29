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

import ch.fhnw.util.math.Vec3;

/**
 * Very basic line class. Loosely based on Apache Commons Math3.
 * 
 * @author radar
 *
 */
public final class Line {
	private final Vec3 origin;
	private final Vec3 direction;

	public Line(Vec3 origin, Vec3 direction) {
		this.origin = origin;
		this.direction = direction.normalize();
	}
	
	public Line(Vec3 origin, Vec3 direction, boolean normalize) {
		this.origin = origin;
		this.direction = normalize ? direction.normalize() : direction;
	}
	
	public static Line fromPoints(Vec3 p1, Vec3 p2) {
		Vec3 delta = p2.subtract(p1);
		float length = delta.length();
		if (length == 0.0) {
			throw new IllegalArgumentException();
		}
		Vec3 direction = delta.normalize();
		Vec3 origin = p1.add(direction.scale(-p1.dot(delta) / length));
		return new Line(origin, direction, false);
	}

	public Vec3 getOrigin() {
		return origin;
	}

	public Vec3 getDirection() {
		return direction;
	}

	@Override
	public String toString() {
		return "[origin:" + origin + ", direction:" + direction + "]";
	}	
}
