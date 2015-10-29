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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import ch.fhnw.util.BufferUtilities;

import com.jogamp.opengl.GL3;

public class RGB8Frame extends Frame {

	public RGB8Frame(int dimI, int dimJ) {
		this(dimI, dimJ, 3);
	}

	protected RGB8Frame(int dimI, int dimJ, int pixelSize) {
		super(pixelSize);
		init(dimI, dimJ);
	}

	public RGB8Frame(int dimI, int dimJ, ByteBuffer targetBuffer) {
		this(dimI, dimJ, targetBuffer, 3);
	}

	public RGB8Frame(int dimI, int dimJ, byte[] targetBuffer) {
		super(dimI, dimJ, targetBuffer, 3);
	}

	protected RGB8Frame(int dimI, int dimJ, ByteBuffer targetBuffer, int pixelSize) {
		super(dimI, dimJ, targetBuffer, pixelSize);
	}

	protected RGB8Frame(int dimI, int dimJ, byte[] targetBuffer, int pixelSize) {
		super(dimI, dimJ, targetBuffer, pixelSize);
	}

	public RGB8Frame(Frame frame) {
		this(frame.dimI, frame.dimJ, 3);
		if (pixelSize == frame.pixelSize)
			BufferUtilities.arraycopy(frame.pixels, 0, pixels, 0, pixels.capacity());
		else {
			if (frame instanceof Grey16Frame) {
				final ByteBuffer src = frame.pixels;
				final ByteBuffer dst = pixels;
				int sps = frame.pixelSize;
				int spos = 0;
				spos++; // assume little endian
				dst.position(0);
				for (int j = 0; j < dimJ; j++) {
					for (int i = 0; i < dimI; i++) {
						byte val = src.get(spos);
						dst.put(val);
						dst.put(val);
						dst.put(val);
						spos += sps;
					}
				}
			} else if (frame instanceof FloatFrame) {
				FloatBuffer src = ((FloatFrame) frame).pixels.asFloatBuffer();
				final float min = ((FloatFrame) frame).getMinMax()[0];
				final float rng = ((FloatFrame) frame).getMinMax()[1] - min;

				final ByteBuffer dst = pixels;
				int spos = 0;
				dst.position(0);
				for (int j = 0; j < dimJ; j++) {
					for (int i = 0; i < dimI; i++) {
						byte val = (byte) ((255f * (src.get(spos) - min)) / rng);
						dst.put(val);
						dst.put(val);
						dst.put(val);
						spos++;
					}
				}
			} else {
				final ByteBuffer src = frame.pixels;
				final ByteBuffer dst = pixels;
				int sps = frame.pixelSize;
				int spos = 0;
				dst.position(0);
				for (int j = 0; j < dimJ; j++) {
					for (int i = 0; i < dimI; i++) {
						dst.put(src.get(spos));
						dst.put(src.get(spos + 1));
						dst.put(src.get(spos + 2));
						spos += sps;
					}
				}
			}
		}
	}

	@Override
	public RGB8Frame create(int dimI, int dimJ) {
		return new RGB8Frame(dimI, dimJ);
	}

	public void setPixels(int i, int j, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
		if (model instanceof DirectColorModel) {
			DirectColorModel dm = (DirectColorModel) model;

			int redMask = dm.getRedMask();
			int blueMask = dm.getBlueMask();
			int greenMask = dm.getGreenMask();

			int redShift = 0;
			int blueShift = 0;
			int greenShift = 0;

			switch (redMask) {
			case 0xFF0000:
				redShift = 16;
				greenShift = 8;
				blueShift = 0;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported color model:" + dm);
			}

			h += j;
			w += i;
			final ByteBuffer dst = this.pixels;
			for (int jj = j; jj < h; jj++) {
				int lineoff = jj * scansize + off;
				dst.position(((jj * dimI) + i) * pixelSize);
				for (int ii = i; ii < w; ii++) {
					int pixelValue = pixels[lineoff + ii];
					dst.put((byte) ((pixelValue & redMask) >> redShift));
					dst.put((byte) ((pixelValue & greenMask) >> greenShift));
					dst.put((byte) ((pixelValue & blueMask) >> blueShift));
				}
			}
		} else {
			throw new UnsupportedOperationException("only direct color supported");
		}
	}

	public void setPixels(int i, int j, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
		throw new UnsupportedOperationException("setPixels with byte array");
	}

	@Override
	public RGB8Frame getSubframe(int i, int j, int dimI, int dimJ) {
		RGB8Frame result = new RGB8Frame(dimI, dimJ);
		getSubframeImpl(i, j, result);
		return result;
	}

	@Override
	public BufferedImage toBufferedImage() {
		BufferedImage result = new BufferedImage(dimI, dimJ, BufferedImage.TYPE_INT_RGB);
		final int[] line = new int[dimI];
		final ByteBuffer src = pixels;
		src.clear();
		for (int j = dimJ; --j >= 0;) {
			for (int i = 0; i < line.length; i++) {
				int tmp = (src.get() & 0xFF) << 16;
				tmp |= (src.get() & 0xFF) << 8;
				tmp |= (src.get() & 0xFF);
				tmp |= 0xFF000000;
				line[i] = tmp;
			}
			result.setRGB(0, j, dimI, 1, line, 0, dimI);
		}
		return result;
	}

	@Override
	public float getFloatComponent(int i, int j, int component) {
		if (component == 3)
			return 1.0f;
		return (pixels.get((j * dimI + i) * pixelSize + component) & 0xFF) / 255f;
	}

	@Override
	public Frame copy() {
		Frame result = new RGB8Frame(this);
		return result;
	}

	@Override
	public Frame alloc() {
		return new RGB8Frame(dimI, dimJ);
	}

	protected final static byte bits2byte(int val, final int shift, final int size, int m) {
		val &= m;
		if (shift > 0)
			val >>= shift;
				else
					val <<= shift;
				val |= val >> size;
				return (byte) val;
	}

	@Override
	public void setPixels(final int x, final int y, final int w, int h, BufferedImage img, final int flags) {
		if (img.getType() == BufferedImage.TYPE_CUSTOM || img.getType() == BufferedImage.TYPE_BYTE_BINARY)
			img = ImageScaler.copy(img, new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB));
		final ByteBuffer dst = pixels;
		final int dstll = dimI * pixelSize;
		int dstyoff = dstll * ((dimJ - 1) - y);
		switch (img.getType()) {
		case BufferedImage.TYPE_4BYTE_ABGR:
		case BufferedImage.TYPE_4BYTE_ABGR_PRE: {
			final byte[] src = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
			final int srcll = img.getWidth() * 4;
			int srcyoff = srcll * y + x * 4;
			final int copylen = w * 4;
			for (; h > 0; h--) {
				dst.position(dstyoff + x * pixelSize);
				for (int i = 0; i < copylen; i += 4) {
					dst.put(src[srcyoff + i + 3]);
					dst.put(src[srcyoff + i + 2]);
					dst.put(src[srcyoff + i + 1]);
				}
				srcyoff += srcll;
				dstyoff -= dstll;
			}
			break;
		}
		case BufferedImage.TYPE_INT_BGR: {
			final int[] src = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
			final int srcll = img.getWidth();
			int srcyoff = srcll * y + x;
			for (; h > 0; h--) {
				dst.position(dstyoff + x * pixelSize);
				for (int i = 0; i < w; i++) {
					final int rgb = src[srcyoff + i];

					dst.put((byte) rgb);
					dst.put((byte) (rgb >> 8));
					dst.put((byte) (rgb >> 16));
				}
				srcyoff += srcll;
				dstyoff -= dstll;
			}
			break;
		}

		case BufferedImage.TYPE_INT_RGB:
		case BufferedImage.TYPE_INT_ARGB:
		case BufferedImage.TYPE_INT_ARGB_PRE: {
			final int[] src = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
			final int srcll = img.getWidth();
			int srcyoff = srcll * y + x;
			for (; h > 0; h--) {
				dst.position(dstyoff + x * pixelSize);
				for (int i = 0; i < w; i++) {
					final int rgb = src[srcyoff + i];

					dst.put((byte) (rgb >> 16));
					dst.put((byte) (rgb >> 8));
					dst.put((byte) rgb);
				}
				srcyoff += srcll;
				dstyoff -= dstll;
			}
			break;
		}
		case BufferedImage.TYPE_3BYTE_BGR: {
			final byte[] src = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
			final int srcll = img.getWidth() * 3;
			int srcyoff = srcll * y + x * 3;
			final int linelen = w * 3;
			for (; h > 0; h--) {
				dst.position(dstyoff + x * pixelSize);
				for (int i = 0; i < linelen; i += 3) {
					dst.put(src[srcyoff + i + 2]);
					dst.put(src[srcyoff + i + 1]);
					dst.put(src[srcyoff + i + 0]);
				}
				srcyoff += srcll;
				dstyoff -= dstll;
			}
			break;
		}
		case BufferedImage.TYPE_BYTE_GRAY: {
			final byte[] src = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
			final int srcll = img.getWidth();
			int srcyoff = srcll * y + x;
			for (; h > 0; h--) {
				dst.position(dstyoff + x * pixelSize);
				for (int i = 0; i < w; i++) {
					final byte grey = src[srcyoff + i];
					dst.put(grey);
					dst.put(grey);
					dst.put(grey);
				}
				srcyoff += srcll;
				dstyoff -= dstll;
			}
			break;
		}
		case BufferedImage.TYPE_BYTE_INDEXED: {
			final byte[] src = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
			final ColorModel cModel = img.getColorModel();
			final int srcll = img.getWidth();
			int srcyoff = srcll * y + x;
			for (; h > 0; h--) {
				dst.position(dstyoff + x * pixelSize);
				for (int i = 0; i < w; i++) {
					final int rgb = cModel.getRGB(src[srcyoff + i] & 0xFF);

					dst.put((byte) (rgb >> 16));
					dst.put((byte) (rgb >> 8));
					dst.put((byte) rgb);
				}
				srcyoff += srcll;
				dstyoff -= dstll;
			}
			break;
		}
		case BufferedImage.TYPE_USHORT_GRAY: {
			final short[] src = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
			int srcyoff = w * y + x;
			for (; h > 0; h--) {
				dst.position(dstyoff + x * pixelSize);
				if (pixelSize == 3)
					for (int i = 0; i < w; i++) {
						final byte grey = (byte) (src[srcyoff + i] >> 8);
						dst.put(grey);
						dst.put(grey);
						dst.put(grey);
					}
				else
					for (int i = 0; i < w; i++) {
						final byte grey = (byte) (src[srcyoff + i] >> 8);
						dst.put(grey);
						dst.put(grey);
						dst.put(grey);
						dst.put(B255);
					}
				srcyoff += w;
				dstyoff -= dstll;
			}
			break;
		}
		case BufferedImage.TYPE_USHORT_555_RGB:
		case BufferedImage.TYPE_USHORT_565_RGB: {
			final short[] src = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
			int srcyoff = w * y + x;
			final int type = img.getType();
			final int r = type == BufferedImage.TYPE_USHORT_555_RGB ? 7 : 8;
			final int gl = type == BufferedImage.TYPE_USHORT_555_RGB ? 5 : 6;
			final int rm = type == BufferedImage.TYPE_USHORT_555_RGB ? 0x7C00 : 0xF800;
			final int gm = type == BufferedImage.TYPE_USHORT_555_RGB ? 0x03E0 : 0x07E0;
			for (; h > 0; h--) {
				dst.position(dstyoff + x * pixelSize);
				if (pixelSize == 3) {
					for (int i = 0; i < w; i++) {
						final short val = src[srcyoff + i];
						dst.put(bits2byte(val, r, 5, rm));
						dst.put(bits2byte(val, 3, gl, gm));
						dst.put(bits2byte(val, -3, 5, 0x001F));
					}
				} else {
					for (int i = 0; i < w; i++) {
						final short val = src[srcyoff + i];
						dst.put(bits2byte(val, r, 5, rm));
						dst.put(bits2byte(val, 3, gl, gm));
						dst.put(bits2byte(val, -3, 5, 0x001F));
						dst.put(B255);
					}
				}
				srcyoff += w;
				dstyoff -= dstll;
			}
			break;
		}
		default:
			throw new RuntimeException("Unsupported image type " + img.getType());
		}

		modified();
	}

	@Override
	public int getARGB(int i, int j) {
		pixels.position((j * dimI + i) * pixelSize);
		int result = pixels.get() & 0xFF;
		result <<= 8;
		result |= pixels.get() & 0xFF;
		result <<= 8;
		result |= pixels.get() & 0xFF;
		return result | 0xFF000000;
	}

	@Override
	public void getRGB(int i, int j, byte[] rgb) {
		pixels.position((j * dimI + i) * pixelSize);
		rgb[0] = pixels.get();
		rgb[1] = pixels.get();
		rgb[2] = pixels.get();
	}

	@Override
	public final void getRGBUnsigned(int i, int j, int[] rgb) {
		pixels.position((j * dimI + i) * pixelSize);
		rgb[0] = pixels.get() & 0xFF;
		rgb[1] = pixels.get() & 0xFF;
		rgb[2] = pixels.get() & 0xFF;
	}

	public final void getRGBFloat(int i, int j, float[] rgb) {
		pixels.position((j * dimI + i) * pixelSize);
		rgb[0] = (pixels.get() & 0xFF) / 255.0f;
		rgb[1] = (pixels.get() & 0xFF) / 255.0f;
		rgb[2] = (pixels.get() & 0xFF) / 255.0f;
	}

	@Override
	public void setRGB(int i, int j, byte[] rgb) {
		pixels.position((j * dimI + i) * pixelSize);
		pixels.put(rgb[0]);
		pixels.put(rgb[1]);
		pixels.put(rgb[2]);
	}

	@Override
	public void setARGB(int i, int j, int argb) {
		pixels.position((j * dimI + i) * pixelSize);
		pixels.put((byte) (argb >> 16));
		pixels.put((byte) (argb >> 8));
		pixels.put((byte) argb);
	}

	protected byte[] rgb00 = new byte[3];
	protected byte[] rgb01 = new byte[3];
	protected byte[] rgb10 = new byte[3];
	protected byte[] rgb11 = new byte[3];

	@Override
	public final void getRGBBilinear(double u, double v, byte[] rgb) {
		// bilinear interpolation
		final int dimI_ = dimI - 1;
		final int dimJ_ = dimJ - 1;

		int i0 = (int) (u * dimI_);
		int j0 = (int) (v * dimJ_);

		if (i0 < 0)
			i0 = 0;
		else if (i0 > dimI_)
			i0 = dimI_;
		if (j0 < 0)
			j0 = 0;
		else if (j0 > dimJ_)
			j0 = dimJ_;

		int i1 = i0 + 1;
		int j1 = j0 + 1;

		if (i1 < 0)
			i1 = 0;
		else if (i1 > dimI_)
			i1 = dimI_;
		if (j1 < 0)
			j1 = 0;
		else if (j1 > dimJ_)
			j1 = dimJ_;

		// interpolate
		final double w = (u - i0 / (float) dimI_) * dimI_;
		final double h = (v - j0 / (float) dimJ_) * dimJ_;

		final byte[] rgb00 = this.rgb00;
		final byte[] rgb01 = this.rgb01;
		final byte[] rgb10 = this.rgb10;
		final byte[] rgb11 = this.rgb11;

		getRGB(i0, j0, rgb00);
		getRGB(i0, j1, rgb01);
		getRGB(i1, j0, rgb10);
		getRGB(i1, j1, rgb11);

		for (int i = 0; i < 3; ++i) {

			double f = h * ((1 - w) * (rgb01[i] & 0xFF) + w * (rgb11[i] & 0xFF)) + (1 - h) * ((1 - w) * (rgb00[i] & 0xFF) + w * (rgb10[i] & 0xFF));

			rgb[i] = (byte) f;

		}
	}

	protected byte[] rgb20 = new byte[3];

	@Override
	public final float getBrightnessBilinear(double u, double v) {
		final byte[] rgb = rgb20;
		getRGBBilinear(u, v, rgb);
		float result = rgb[0] & 0xFF;
		result += rgb[1] & 0xFF;
		result += rgb[2] & 0xFF;
		result /= 765f;
		return result;
	}

	@Override
	public final float getBrightness(int i, int j) {
		pixels.position((j * dimI + i) * pixelSize);
		int result = pixels.get() & 0xFF;
		result += pixels.get() & 0xFF;
		result += pixels.get() & 0xFF;
		return result / 765f;
	}

	@Override
	public void setSubframe(int i, int j, Frame src) {
		if (src.getClass() != getClass())
			src = new RGB8Frame(src);
		setSubframeImpl(i, j, src);
	}

	@Override
	protected void loadInternal(GL3 gl, int target, int textureId) {
		gl.glTexImage2D(target, 0, GL3.GL_RGB, dimI, dimJ, 0, GL3.GL_RGB, GL3.GL_UNSIGNED_BYTE, pixels);
	}
}
