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

import ch.fhnw.util.math.IVec4;
import ch.fhnw.util.math.Vec4;

public final class RGBA implements IColor, IVec4 {
	public static final RGBA BLACK = new RGBA(0, 0, 0, 1);
	public static final RGBA WHITE = new RGBA(1, 1, 1, 1);

	public static final RGBA RED = new RGBA(1, 0, 0, 1);
	public static final RGBA GREEN = new RGBA(0, 1, 0, 1);
	public static final RGBA BLUE = new RGBA(0, 0, 1, 1);

	public static final RGBA YELLOW = new RGBA(1, 1, 0, 1);
	public static final RGBA MAGENTA = new RGBA(1, 0, 1, 1);
	public static final RGBA CYAN = new RGBA(0, 1, 1, 1);

	public static final RGBA GRAY = new RGBA(0.5f, 0.5f, 0.5f, 1);
	public static final RGBA LIGHT_GRAY = new RGBA(0.75f, 0.75f, 0.75f, 1);
	public static final RGBA DARK_GRAY = new RGBA(0.25f, 0.25f, 0.25f, 1);

	public final float r;
	public final float g;
	public final float b;
	public final float a;

	public RGBA(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public RGBA(float[] rgba) {
		this(rgba[0], rgba[1], rgba[2], rgba[3]);
	}

	public RGBA(Vec4 c) {
		this(c.x, c.y, c.z, c.w);
	}

	public RGBA(int rgba) {
		this(((rgba >> 24) & 0xFF) / 255f, ((rgba >> 16) & 0xFF) / 255f, ((rgba >> 8) & 0xFF) / 255f, (rgba & 0xFF) / 255f);
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
		return a;
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
	public float w() {
		return a;
	}

	public RGBA scaleRGB(float s) {
		return new RGBA(r * s, g * s, b * s, a);
	}

	public int toRGBA() {
		int r = (int) (this.r * 255);
		int g = (int) (this.g * 255);
		int b = (int) (this.b * 255);
		int a = (int) (this.a * 255);
		return (r << 24 | g << 16 | b << 8 | a << 0);
	}

	public int toABGR() {
		int r = (int) (this.r * 255);
		int g = (int) (this.g * 255);
		int b = (int) (this.b * 255);
		int a = (int) (this.a * 255);
		return (a << 24 | b << 16 | g << 8 | r << 0);
	}
	
	@Override
	public Vec4 toVec4() {
		return new Vec4(r, g, b, a);
	}
	
	@Override
	public float[] toArray() {
		return new float[] { r, g, b, a };
	}

	@Override
	public String toString() {
		return "rgba[" + red() + " " + green() + " " + blue() + " " + alpha() + "]";
	}

	public void set(float[] colors, int idx) {
		colors[idx+0] = r;
		colors[idx+1] = g;
		colors[idx+2] = b;
		colors[idx+3] = a;
	}
}
