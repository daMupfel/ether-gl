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

package ch.fhnw.ether.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import ch.fhnw.util.BufferUtilities;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

public final class FloatFrame extends Frame {
	private static final float[] MIN_MAX_0_1 = { 0f, 1f };
	public float[] originalMinMax = MIN_MAX_0_1;
	FloatBuffer buffer;

	protected FloatFrame() {
		super(4);
		buffer = pixels.asFloatBuffer();
	}

	public FloatFrame(Frame frame) {
		this(frame, MIN_MAX_0_1);
	}

	public FloatFrame(FloatFrame frame) {
		this(frame, frame.originalMinMax);
	}

	public FloatFrame(int dimI, int dimJ) {
		super(4);
		buffer = pixels.asFloatBuffer();
		init(dimI, dimJ);
	}

	public FloatFrame(int dimI, int dimJ, ByteBuffer frameBuffer) {
		super(dimI, dimJ, frameBuffer, 4);
		pixels.rewind();
		buffer = pixels.asFloatBuffer();
	}

	@Override
	public FloatFrame create(int dimI, int dimJ) {
		return new FloatFrame(dimI, dimJ);
	}
	
	public FloatFrame(Frame frame, float[] minMax) {
		this(frame.dimI, frame.dimJ);
		if (pixelSize == frame.pixelSize && frame instanceof FloatFrame)
			BufferUtilities.arraycopy(frame.pixels, 0, pixels, 0, pixels.capacity());
		else {
			Grey16Codec encoder = new Grey16Codec(minMax);
			final ByteBuffer src = frame.pixels;
			int sps = frame.pixelSize;
			if (frame instanceof Grey16Frame) {
				int spos = 0;
				int dpos = 0;
				for (int j = 0; j < dimJ; j++) {
					for (int i = 0; i < dimI; i++) {
						buffer.put(dpos++, encoder.decode(src.get(spos + 1), src.get(spos)));
						spos += sps;
					}
				}
			} else {
				int spos = 0;
				int dpos = 0;
				for (int j = 0; j < dimJ; j++) {
					for (int i = 0; i < dimI; i++) {
						int val = src.get(spos) & 0xFF;
						val += src.get(spos) & 0xFF;
						val += src.get(spos) & 0xFF;
						val /= 3;
						buffer.put(dpos++, encoder.decode(val, val));
						spos += sps;
					}
				}
			}
		}
		originalMinMax = minMax;
	}

	@Override
	protected void init(int dimI, int dimJ) {
		super.init(dimI, dimJ);
		buffer = pixels.asFloatBuffer();
	}

	@Override
	public void setPixels(int x, int y, int w, int h, BufferedImage img, int flags) {
		final int dstll = dimI;
		int dstyoff = dstll * ((dimJ - 1) - y);

		if (img.getType() != BufferedImage.TYPE_USHORT_GRAY) {
			BufferedImage tmp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
			Graphics g = tmp.getGraphics();
			g.drawImage(img, x, y, x + w, y + h, x, y, x + w, y + h, ImageScaler.AWT_OBSERVER);
			g.dispose();
			img = tmp;
		}

		Grey16Codec encoder = new Grey16Codec(new float[] { .0f, 1.0f });

		final short[] src = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
		final int srcll = img.getWidth();
		int srcyoff = srcll * y + x;
		for (; h > 0; h--) {
			buffer.position(dstyoff + x);
			for (int i = 0; i < w; i++)
				buffer.put(encoder.decode(src[srcyoff + i]));
			srcyoff += srcll;
			dstyoff -= dstll;
		}
	}

	@Override
	public BufferedImage toBufferedImage() {
		BufferedImage result = new BufferedImage(dimI, dimJ, BufferedImage.TYPE_USHORT_GRAY);
		short[] line = new short[dimI];

		Grey16Codec encoder = new Grey16Codec(getMinMax());

		int y = 0;
		for (int j = dimJ; --j >= 0; y++) {
			for (int i = 0; i < line.length; i++)
				line[i] = encoder.encode(buffer.get(j * dimI + i));

			result.getRaster().setDataElements(0, y, dimI, 1, line);
		}
		return result;
	}

	@Override
	public Frame copy() {
		Frame result = new FloatFrame(this);
		return result;
	}

	@Override
	public Frame alloc() {
		return new FloatFrame(dimI, dimJ);
	}

	@Override
	public float getBrightnessBilinear(double u, double v) {
		return getComponentBilinear(u, v, 0);
	}

	@Override
	public float getFloatComponent(int i, int j, int component) {
		float out = buffer.get(j * dimI + i);

		if (component == 3)
			return Float.isNaN(out) ? 0 : 1;

		return out;
	}

	public float[] getMinMax() {
		float minMax[] = { Float.MAX_VALUE, -Float.MAX_VALUE };
		buffer.clear();
		for (int i = buffer.capacity(); --i >= 0;) {
			float val = buffer.get();

			if (Float.isNaN(val))
				continue;

			minMax[0] = Math.min(minMax[0], val);
			minMax[1] = Math.max(minMax[1], val);
		}
		return minMax;
	}

	private static class Grey16Codec {
		private static int MAX_USHORT = 0xffff;

		private final float min;
		private final float max;
		private final float range;

		public Grey16Codec(float[] minMax) {
			this.min = minMax[0];
			this.max = minMax[1];
			this.range = minMax[1] - minMax[0];
		}

		public short encode(float value) {
			int ival = Math.round(((value - min) / range) * MAX_USHORT);

			return (short) (ival & 0xffff);
		}

		public float decode(int val) {
			int ival = val & 0xffff;
			return linearInterpolate(min, max, ival / (float) MAX_USHORT);
		}

		public float decode(int msb, int lsb) {
			return decode(msb << 8 & 0xff00 | lsb & 0xff);
		}
	}

	@Override
	public float getBrightness(int i, int j) {
		return buffer.get(j * dimI + i);
	}

	@Override
	public void setARGB(int i, int j, int argb) {
		buffer.put((j * dimI) + i, ((((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF)) / 765f));
	}

	public void setBrightness(int i, int j, float value) {
		buffer.put(j * dimI + i, value);
	}

	@Override
	public int getARGB(int i, int j) {
		int rgb = (int) (buffer.get(j * dimI + i) * 255f) & 0xFF;
		return rgb << 16 | rgb << 8 | rgb | 0xFF000000;
	}

	@Override
	public void getRGBBilinear(double u, double v, byte[] rgb) {
		rgb[0] = rgb[1] = rgb[2] = (byte) (getComponentBilinear(u, v, 0) * 255f);
	}

	@Override
	public FloatFrame getSubframe(int i, int j, int dimI, int dimJ) {
		FloatFrame result = new FloatFrame(dimI, dimJ);
		getSubframeImpl(i, j, result);
		return result;
	}

	@Override
	public void setSubframe(int i, int j, Frame src) {
		if (src.getClass() != getClass())
			src = new FloatFrame(src);
		setSubframeImpl(i, j, src);
	}

	@Override
	protected void loadInternal(GL3 gl, int target, int textureId) {
		gl.glTexImage2D(target, 0, GL3.GL_RED, dimI, dimJ, 0, GL3.GL_RED, GL.GL_FLOAT, pixels);
	}	
}
