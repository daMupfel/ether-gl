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

package ch.fhnw.util.color;

import ch.fhnw.util.math.IVec3;
import ch.fhnw.util.math.Vec3;

public final class RGB implements IColor, IVec3 {
	public static final RGB BLACK = new RGB(0, 0, 0);
	public static final RGB WHITE = new RGB(1, 1, 1);

	public static final RGB RED = new RGB(1, 0, 0);
	public static final RGB GREEN = new RGB(0, 1, 0);
	public static final RGB BLUE = new RGB(0, 0, 1);

	public static final RGB YELLOW = new RGB(1, 1, 0);
	public static final RGB MAGENTA = new RGB(1, 0, 1);
	public static final RGB CYAN = new RGB(0, 1, 1);

	public static final RGB GRAY = new RGB(0.5f, 0.5f, 0.5f);
	public static final RGB LIGHT_GRAY = new RGB(0.75f, 0.75f, 0.75f);
	public static final RGB DARK_GRAY = new RGB(0.25f, 0.25f, 0.25f);

	public static final RGB GRAY10 = new RGB(0.1f, 0.1f, 0.1f);
	public static final RGB GRAY20 = new RGB(0.2f, 0.2f, 0.2f);
	public static final RGB GRAY30 = new RGB(0.3f, 0.3f, 0.3f);
	public static final RGB GRAY40 = new RGB(0.4f, 0.4f, 0.4f);
	public static final RGB GRAY50 = new RGB(0.5f, 0.5f, 0.5f);
	public static final RGB GRAY60 = new RGB(0.6f, 0.6f, 0.6f);
	public static final RGB GRAY70 = new RGB(0.7f, 0.7f, 0.7f);
	public static final RGB GRAY80 = new RGB(0.8f, 0.8f, 0.7f);
	public static final RGB GRAY90 = new RGB(0.9f, 0.9f, 0.9f);

	public final float r;
	public final float g;
	public final float b;

	public RGB(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public RGB(float[] rgb) {
		this(rgb[0], rgb[1], rgb[2]);
	}

	public RGB(Vec3 v) {
		this(v.x, v.y, v.z);
	}

	@Override
	public float red() {
		return r;
	}

	@Override
	public float green() {
		return g;
	}

	@Override
	public float blue() {
		return b;
	}

	@Override
	public float alpha() {
		return 1f;
	}

	@Override
	public float x() {
		return r;
	}
	
	@Override
	public float y() {
		return g;
	}

	@Override
	public float z() {
		return b;
	}

	@Override
	public Vec3 toVec3() {
		return new Vec3(r, g, b);
	}

	@Override
	public float[] toArray() {
		return new float[] { r, g, b };
	}

	@Override
	public String toString() {
		return "rgb[" + red() + " " + green() + " " + blue() + "]";
	}
}
