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

import java.nio.ByteBuffer;

import ch.fhnw.ether.video.fx.AbstractVideoFX;

public final class ColorUtilities {
	public static final float EPS = 216.f / 24389.f;
	public static final float K   = 24389.f / 27.f;

	public static final float XR   = 0.964221f; // reference white D50
	public static final float YR   = 1.0f;
	public static final float ZR   = 0.825211f;

	public static float[] getLUVfromRGB(final ByteBuffer pixels, final float[] result, int pixelSize) {
		int idx = 0;
		for(int i = result.length / 3; --i >= 0;) {
			// http://www.brucelindbloom.com

			float r, g, b, X, Y, Z, yr;
			float L;

			// RGB to XYZ

			r = AbstractVideoFX.toFloat(pixels.get()); // R 0..1
			g = AbstractVideoFX.toFloat(pixels.get()); // G 0..1
			b = AbstractVideoFX.toFloat(pixels.get()); // B 0..1

			if (r == 0 && g == 0 && b == 0) {
				result[idx++] = 0.0f;
				result[idx++] = 0.0f;
				result[idx++] = 0.0f;
				continue;
			}

			// System.out.println("r = "+r+" g = "+g+" b = "+b);

			// assuming sRGB (D65)
			if (r <= 0.04045f)
				r = r / 12.92f;
			else
				r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

			if (g <= 0.04045f)
				g = g / 12.92f;
			else
				g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

			if (b <= 0.04045f)
				b = b / 12.92f;
			else
				b = (float) Math.pow((b + 0.055) / 1.055, 2.4);

			/*
			 * X = 0.436052025f*r + 0.385081593f*g + 0.143087414f *b; Y = 0.222491598f*r + 0.71688606f *g + 0.060621486f *b;
			 * Z = 0.013929122f*r + 0.097097002f*g + 0.71418547f *b;
			 */

			X = 0.4360747f * r + 0.3850649f * g + 0.1430804f * b;
			Y = 0.2225045f * r + 0.7168786f * g + 0.0606169f * b;
			Z = 0.0139322f * r + 0.0971045f * g + 0.7141733f * b;

			// XYZ to Luv

			float u, v, u_, v_, ur_, vr_;

			u_ = 4.f * X / (X + 15.f * Y + 3.f * Z);
			v_ = 9.f * Y / (X + 15.f * Y + 3.f * Z);

			ur_ = 4.f * XR / (XR + 15.f * YR + 3.f * ZR);
			vr_ = 9.f * YR / (XR + 15.f * YR + 3.f * ZR);

			yr = Y / YR;

			if (yr > EPS)
				L = (116.f * (float) Math.pow(yr, 1 / 3.f) - 16.f);
			else
				L = K * yr;

			u = 13.f * L * (u_ - ur_);
			v = 13.f * L * (v_ - vr_);

			result[idx++] = L;
			result[idx++] = u;
			result[idx++] = v;
			// System.out.println("L="+L+" u="+u+" v="+v);
			/*
			 * luv[0] = (int) (2.55*L + .5); luv[1] = (int) (u + .5); luv[2] = (int) (v + .5);
			 */
			if(pixelSize == 4) pixels.get();
		}

		return result;
	}

	public static float[] getHSBfromRGB(final ByteBuffer pixels, final float[] result, int pixelSize) {
		int idx = 0;
		for(int i = result.length / 3; --i >= 0;) {
			float       hue;
			final float saturation;
			final float brightness;
			final int   r = pixels.get() & 0xFF;
			final int   g = pixels.get() & 0xFF;
			final int   b = pixels.get() & 0xFF;

			int cmax = (r > g) ? r : g;
			if (b > cmax) cmax = b;
			int cmin = (r < g) ? r : g;
			if (b < cmin) cmin = b;

			brightness = (cmax) / 255.0f;
			saturation = cmax != 0 ? ((float) (cmax - cmin)) / ((float) cmax) : 0f;

			if (saturation == 0)
				hue = 0;
			else {
				float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
				float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
				float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
				if (r == cmax)
					hue = bluec - greenc;
				else if (g == cmax)
					hue = 2.0f + redc - bluec;
				else
					hue = 4.0f + greenc - redc;
				hue = hue / 6.0f;
				if (hue < 0)
					hue = hue + 1.0f;
			}
			result[idx++] = hue;
			result[idx++] = saturation;
			result[idx++] = brightness;
			if(pixelSize == 4) pixels.get();
		}
		return result;
	}

	public static void putRGBfromHSB(final ByteBuffer pixels, final float[] hsb, int pixelSize) {
		int idx = 0;
		for(int i = hsb.length / 3; --i >= 0;) {
			int r = 0, g = 0, b = 0;
			if (hsb[idx+1] == 0)
				r = g = b = (int) (hsb[idx+2] * 255.0f + 0.5f);
			else {
				float h = (hsb[idx+0] - (float)Math.floor(hsb[idx+0])) * 6.0f;
				float f = h - (float)Math.floor(h);
				float p = hsb[idx+2] * (1.0f - hsb[idx+1]);
				float q = hsb[idx+2] * (1.0f - hsb[idx+1] * f);
				float t = hsb[idx+2] * (1.0f - (hsb[idx+1] * (1.0f - f)));
				switch ((int) h) {
				case 0:
					r = (int) (hsb[idx+2] * 255.0f + 0.5f);
					g = (int) (t * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
					break;
				case 1:
					r = (int) (q * 255.0f + 0.5f);
					g = (int) (hsb[idx+2] * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
					break;
				case 2:
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (hsb[idx+2] * 255.0f + 0.5f);
					b = (int) (t * 255.0f + 0.5f);
					break;
				case 3:
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (q * 255.0f + 0.5f);
					b = (int) (hsb[idx+2] * 255.0f + 0.5f);
					break;
				case 4:
					r = (int) (t * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (hsb[idx+2] * 255.0f + 0.5f);
					break;
				case 5:
					r = (int) (hsb[idx+2] * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (q * 255.0f + 0.5f);
					break;
				}
			}
			pixels.put((byte) r);
			pixels.put((byte) g);
			pixels.put((byte) b);
			if(pixelSize == 4) pixels.get();
			idx += 3;
		}
	}

	public static float[] getYUVfromRGB(final ByteBuffer pixels, final float[] result, int pixelSize) {
		int idx = 0;
		for(int i = result.length / 3; --i >= 0;) {
			final float r = AbstractVideoFX.toFloat(pixels.get());
			final float g = AbstractVideoFX.toFloat(pixels.get());
			final float b = AbstractVideoFX.toFloat(pixels.get());
			result[idx+0] = 0.299f*r+0.587f*g+0.114f*b;
			result[idx+1] = 0.492f*(b-result[idx]);
			result[idx+2] = 0.877f*(r-result[idx]);
			if(pixelSize == 4) pixels.get();
			idx += 3;
		}
		return result;
	}

	public static void putRGBfromYUV(final ByteBuffer pixels, final float[] yuv, int pixelSize) {
		int idx = 0;
		for(int i = yuv.length / 3; --i >= 0;) {		
			final float r = yuv[idx] + 1.14f  * yuv[idx+2];
			final float g = yuv[idx] - 0.395f * yuv[idx+1] - 0.581f * yuv[idx+2];
			final float b = yuv[idx] + 2.033f * yuv[idx+1];
			pixels.put(AbstractVideoFX.toByte(r));
			pixels.put(AbstractVideoFX.toByte(g));
			pixels.put(AbstractVideoFX.toByte(b));
			if(pixelSize == 4) pixels.get();
			idx += 3;
		}
	}	
}
