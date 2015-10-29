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

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import ch.fhnw.ether.media.AbstractMediaTarget;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.VideoFrame;
import ch.fhnw.util.BufferUtilities;

public abstract class Frame extends AbstractMediaTarget<VideoFrame,IVideoRenderTarget> implements IVideoRenderTarget {

	public enum FileFormat {PNG,JPEG}

	public static final byte        B0    = 0;
	public static final byte        B255  = (byte) 255;
	private static final ByteBuffer EMPTY = BufferUtilities.createDirectByteBuffer(0);

	public ByteBuffer pixels = EMPTY;
	public int        dimI;
	public int        dimJ;
	public int        pixelSize;
	private int       modCount;

	protected Frame(int pixelSize) {
		super(Thread.MIN_PRIORITY);
		this.pixelSize = pixelSize;
	}

	protected Frame(int dimI, int dimJ, byte[] frameBuffer, int pixelSize) {
		super(Thread.MIN_PRIORITY);
		this.pixels = BufferUtilities.createDirectByteBuffer(frameBuffer.length);
		this.pixels.put(frameBuffer);
		this.pixelSize = pixelSize;
		init(dimI, dimJ);
	}

	protected Frame(int dimI, int dimJ, ByteBuffer frameBuffer, int pixelSize) {
		super(Thread.MIN_PRIORITY);
		if (frameBuffer.isDirect()) {
			this.pixels = frameBuffer;
		} else {
			this.pixels = BufferUtilities.createDirectByteBuffer(frameBuffer.capacity());
			this.pixels.put(frameBuffer);
		}
		this.pixelSize = pixelSize;
		init(dimI, dimJ);
	}

	@Override
	public int hashCode() {
		return dimI << 18 | dimJ << 2 | pixelSize;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Frame) {
			Frame other = (Frame) obj;
			pixels.rewind();
			other.pixels.rewind();
			return dimI == other.dimI && dimJ == other.dimJ && pixelSize == other.pixelSize && pixels.equals(other.pixels);
		}
		return false;
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + dimI + "x" + dimJ;
	}

	protected void init(int dimI, int dimJ) {
		this.dimI = dimI;
		this.dimJ = dimJ;
		int bufsize = dimI * dimJ * pixelSize;
		if (this.pixels.capacity() < bufsize)
			this.pixels = BufferUtilities.createDirectByteBuffer(bufsize);
	}

	public static Frame create(int dimI, int dimJ, int pixelSize, ByteBuffer buffer) {
		switch (pixelSize) {
		case 2:
			return new Grey16Frame(dimI, dimJ, buffer);
		case 3:
			return new RGB8Frame(dimI, dimJ, buffer);
		case 4:
			return new RGBA8Frame(dimI, dimJ, buffer);
		default:
			throw new IllegalArgumentException("Can't create frame wiht pixelSize=" + pixelSize);
		}
	}

	public static Frame create(int dimI, int dimJ, int pixelSize, byte[] pixelsData) {
		return create(dimI, dimJ, pixelSize, ByteBuffer.wrap(pixelsData));
	}

	public static Frame create(BufferedImage img) {
		return create(img, 0);
	}

	public static Frame create(BufferedImage img, int flags) {
		Frame result = null;
		switch (img.getType()) {
		case BufferedImage.TYPE_BYTE_BINARY:
		case BufferedImage.TYPE_CUSTOM:
			if (img.getColorModel().getNumColorComponents() == 1)
				result = new Grey16Frame(img.getWidth(), img.getHeight());
			else {
				if (img.getColorModel().hasAlpha())
					result = new RGBA8Frame(img.getWidth(), img.getHeight());
				else
					result = new RGB8Frame(img.getWidth(), img.getHeight());
			}
			break;
		case BufferedImage.TYPE_4BYTE_ABGR:
		case BufferedImage.TYPE_INT_ARGB:
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
		case BufferedImage.TYPE_INT_ARGB_PRE:
		case BufferedImage.TYPE_BYTE_INDEXED:
			result = new RGBA8Frame(img.getWidth(), img.getHeight());
			break;
		case BufferedImage.TYPE_USHORT_555_RGB:
		case BufferedImage.TYPE_USHORT_565_RGB:
		case BufferedImage.TYPE_INT_RGB:
		case BufferedImage.TYPE_3BYTE_BGR:
			result = new RGB8Frame(img.getWidth(), img.getHeight());
			break;
		case BufferedImage.TYPE_BYTE_GRAY:
		case BufferedImage.TYPE_USHORT_GRAY:
			result = new Grey16Frame(img.getWidth(), img.getHeight());
			break;
		default:
			throw new RuntimeException("Unsupported image type " + img.getType());
		}

		result.setPixels(0, 0, img.getWidth(), img.getHeight(), img, flags);
		return result;
	}

	public static Frame create(File file) throws IOException {
		return create(ImageIO.read(file));
	}

	public static Frame create(URL url) throws IOException {
		return create(ImageIO.read(url));
	}

	public static Frame create(InputStream in) throws IOException {
		return create(ImageIO.read(in));
	}

	public abstract Frame create(int width, int height);

	public static Frame create(GL3 gl, int target, int textureId) {
		Frame result = null;
		int[] tmpi   = new int[1];
		int width;
		int height;
		int internalFormat;
		gl.glBindTexture(target, textureId);
		gl.glGetTexLevelParameteriv(target, 0, GL3.GL_TEXTURE_INTERNAL_FORMAT, tmpi, 0);	internalFormat = tmpi[0];
		gl.glGetTexLevelParameteriv(target, 0, GL3.GL_TEXTURE_WIDTH,      tmpi, 0);			width          = tmpi[0];
		gl.glGetTexLevelParameteriv(target, 0, GL3.GL_TEXTURE_HEIGHT,     tmpi, 0);			height         = tmpi[0];
		switch(internalFormat) {
		case GL.GL_RGB:
			result = new RGB8Frame(width, height);
			break;
		case GL.GL_RGBA:
			result = new RGBA8Frame(width, height);
			break;
		default:
			throw new IllegalArgumentException("Unsupported format:" + internalFormat);
		}
		result.pixels.clear();
		gl.glReadnPixels(0, 0, width, height, internalFormat, GL.GL_UNSIGNED_BYTE, result.pixels.capacity(), result.pixels);
		gl.glBindTexture(target, 0);
		return result;
	}

	public final void setPixels(int i, int j, int width, int height, BufferedImage img) {
		setPixels(i, j, width, height, img, 0);
	}

	public abstract void setPixels(int i, int j, int width, int height, BufferedImage img, int flags);

	public abstract Frame getSubframe(int i, int j, int dimI, int dimJ);

	public abstract void setSubframe(int i, int j, Frame frame);

	protected void getSubframeImpl(int i, int j, Frame dst) {
		if (i + dst.dimI > dimI)
			throw new IllegalArgumentException("i(" + i + ")+dst.dimI(" + dst.dimI + ") > dimI(" + dimI + ")");
		if (j + dst.dimJ > dimJ)
			throw new IllegalArgumentException("j(" + j + ")+dst.dimJ(" + dst.dimJ + ") > dimJ(" + dimJ + ")");
		int slnsize = dimI;
		int dlnsize = dst.dimI;
		for (int jj = 0; jj < dst.dimJ; jj++) {
			BufferUtilities.arraycopy(pixels, ((j + jj) * slnsize + i) * pixelSize, dst.pixels, jj * dlnsize * pixelSize, dlnsize * pixelSize);
		}
	}

	protected void setSubframeImpl(int i, int j, Frame src) {
		if (i + src.dimI > dimI)
			throw new IllegalArgumentException("i(" + i + ")+src.dimI(" + src.dimI + ") > dimI(" + dimI + ")");
		if (j + src.dimJ > dimJ)
			throw new IllegalArgumentException("j(" + j + ")+src.dimJ(" + src.dimJ + ") > dimJ(" + dimJ + ")");
		int slnsize = src.dimI;
		int dlnsize = dimI;
		for (int jj = 0; jj < src.dimJ; jj++) {
			BufferUtilities.arraycopy(src.pixels, jj * slnsize * pixelSize, pixels, ((j + jj) * dlnsize + i) * pixelSize, slnsize * pixelSize);
		}
	}	

	public static Frame copyTo(Frame src, Frame dst) {
		if (src.getClass() == dst.getClass()) {
			for (int j = Math.min(src.dimJ, dst.dimJ); --j >= 0;)
				BufferUtilities.arraycopy(src.pixels, (j * src.dimI) * src.pixelSize, dst.pixels, (j * dst.dimI) * dst.pixelSize, Math.min(src.dimI, dst.dimI)
						* src.pixelSize);
		} else {
			for (int j = Math.min(src.dimJ, dst.dimJ); --j >= 0;)
				for (int i = Math.min(src.dimI, dst.dimI); --i >= 0;)
					dst.setARGB(i, j, src.getARGB(i, j));
		}
		dst.modified();
		return dst;
	}

	public abstract BufferedImage toBufferedImage();

	public void modified() {
		modCount++;
	}

	public int getModCount() {
		return modCount;
	}

	public abstract Frame copy();

	public abstract Frame alloc();

	public void getRGBUnsigned(int i, int j, int[] rgb) {
		int irgb = getARGB(i, j);
		rgb[0] = (irgb >> 16) & 0xFF;
		rgb[1] = (irgb >> 8) & 0xFF;
		rgb[2] = irgb & 0xFF;
	}

	public void getRGB(int i, int j, byte[] rgb) {
		int irgb = getARGB(i, j);
		rgb[0] = (byte) (irgb >> 16);
		rgb[1] = (byte) (irgb >> 8);
		rgb[2] = (byte) irgb;
	}

	public void setRGB(int i, int j, byte[] rgb) {
		int argb = (rgb[0] & 0xFF) << 16;
		argb |= (rgb[1] & 0xFF) << 8;
		argb |= (rgb[2] & 0xFF);
		argb |= 0xFF000000;
		setARGB(i, j, argb);
	}

	public final float getComponentBilinear(double u, double v, int component) {
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
		final double w = (u - i0 / (double) dimI_) * dimI_;
		final double h = (v - j0 / (double) dimJ_) * dimJ_;

		float c00 = getFloatComponent(i0, j0, component);
		float c01 = getFloatComponent(i0, j1, component);
		float c10 = getFloatComponent(i1, j0, component);
		float c11 = getFloatComponent(i1, j1, component);

		float c = (float) (h * ((1 - w) * c01 + w * c11) + (1 - h) * ((1 - w) * c00 + w * c10));

		return c;
	}

	public boolean hasAlpha() {
		return false;
	}

	public float getAlphaBilinear(double u, double v) {
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
		final double w = (u - i0 / (double) dimI_) * dimI_;
		final double h = (v - j0 / (double) dimJ_) * dimJ_;

		float c00 = getAlphaComponent(i0, j0);
		float c01 = getAlphaComponent(i0, j1);
		float c10 = getAlphaComponent(i1, j0);
		float c11 = getAlphaComponent(i1, j1);

		float c = (float) (h * ((1 - w) * c01 + w * c11) + (1 - h) * ((1 - w) * c00 + w * c10));

		return c;
	}

	public float getAlphaComponent(int x, int y) {
		throw new Error("Alpha not defined");
	}

	public void clear() {
		BufferUtilities.fill(pixels, 0, pixels.capacity(), B0);
	}

	public abstract float getBrightness(int i, int j);

	public abstract float getBrightnessBilinear(double u, double v);

	public abstract void getRGBBilinear(double u, double v, byte[] rgb);

	public abstract float getFloatComponent(int i, int j, int component);

	public abstract void setARGB(int i, int j, int argb);

	public abstract int getARGB(int i, int j);

	protected static final float linearInterpolate(float low, float high, float weight) {
		return low + ((high - low) * weight);
	}

	public void write(File file, FileFormat format) throws IOException {
		ImageIO.write(toBufferedImage(), format.toString(), file);
	}

	public void write(OutputStream out, FileFormat format) throws IOException {
		ImageIO.write(toBufferedImage(), format.toString(), out);
	}

	public void load(GL3 gl, int target, int textureId) {
		gl.glBindTexture(target, textureId);
		gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);
		pixels.rewind();
		loadInternal(gl, target, textureId);
		gl.glGenerateMipmap(target);
		gl.glBindTexture(target, 0);
	}

	protected abstract void loadInternal(GL3 gl, int target, int textureId);

	static final ExecutorService POOL       = Executors.newCachedThreadPool();
	static final int             NUM_CHUNKS = Runtime.getRuntime().availableProcessors(); 

	final static class Chunk implements Runnable {
		private final int            from;
		private final int            to;
		private final ByteBuffer     pixels;
		private final ILineProcessor processor;
		private final int            lineLength;

		Chunk(Frame frame, int from, int to, ILineProcessor processor) {
			this.from       = from;
			this.to         = to;
			this.pixels     = frame.pixels.duplicate();
			this.processor  = processor;
			this.lineLength = frame.dimI * frame.pixelSize;
		}

		@Override
		public void run() {
			for(int j = from; j < to; j++) {
				pixels.position(j * lineLength);
				processor.process(pixels, j);
			}
		}
	}

	public final void processLines(ILineProcessor processor) {
		List<Future<?>> result = new ArrayList<>(NUM_CHUNKS + 1);
		int inc  = Math.max(32, dimJ / NUM_CHUNKS);
		for(int from = 0; from < dimJ; from += inc)
			result.add(POOL.submit(new Chunk(this, from, Math.min(from + inc, dimJ), processor)));
		try {
			for(Future<?> f : result)
				f.get();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public final void position(ByteBuffer pixels, int i, int j) {
		pixels.position((j * dimI + i) * pixelSize);
	}

	@Override
	public void render() throws RenderCommandException {
		VideoFrame vf = getFrame();
		if(dimI != vf.frame.dimI || dimJ != vf.frame.dimJ) {
			setPixels(0, 0, dimI, dimJ, ImageScaler.getScaledInstance(vf.frame.toBufferedImage(), dimI, dimJ, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false));
		} else
			setSubframe(0, 0, vf.frame);
	}
}
