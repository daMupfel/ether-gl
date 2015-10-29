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

package ch.fhnw.ether.video.avfoundation;

import java.lang.ref.ReferenceQueue;
import java.net.URL;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.image.RGBA8Frame;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.URLVideoSource;
import ch.fhnw.util.AutoDisposer;
import ch.fhnw.util.AutoDisposer.Reference;

public final class AVAsset extends URLVideoSource.State {
	private static boolean READY = true;

	public static class AVAssetRef extends Reference<AVAsset> {
		private final long nativeHandle;
		
		public AVAssetRef(AVAsset referent, ReferenceQueue<? super AVAsset> q) {
			super(referent, q);
			nativeHandle = referent.nativeHandle;
		}

		@Override
		public void dispose() {
			nativeDispose(nativeHandle);
		}
	}

	public static final AutoDisposer<AVAsset> autoDispose = new AutoDisposer<>(AVAssetRef.class);
	
	static {
		try {
			System.loadLibrary("etherglvideo");
		} catch (Exception e) {
			READY = false;
		}
	}

	public static boolean isReady() {
		return READY;
	}

	private long   nativeHandle;
	private double duration;
	private double frameRate;
	private long   frameCount;
	private int    width;
	private int    height;
	
	public AVAsset(IVideoRenderTarget target, URL url, int numPlays) {
		super(target, url, numPlays);		
		nativeHandle = nativeCreate(url.toString());
		if (nativeHandle == 0)
			throw new IllegalArgumentException("cannot create avasset from " + url);
		
		autoDispose.add(this);
		duration   = nativeGetDuration(nativeHandle);
		frameRate  = nativeGetFrameRate(nativeHandle);
		frameCount = nativeGetFrameCount(nativeHandle);
		width      = nativeGetWidth(nativeHandle);
		height     = nativeGetHeight(nativeHandle);
	}

	public URL getURL() {
		return url;
	}

	public double getDuration() {
		return duration;
	}

	@Override
	public double getFrameRate() {
		return frameRate;
	}

	@Override
	public long getFrameCount() {
		return frameCount;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void rewind() {
		playOutTime = 0;
		startTime   = -1;
		nativeRewind(nativeHandle);
	}
	
	@Override
	protected Frame getNextFrame() {
		playOutTime += 1.0 / getFrameRate();
		byte[] pixels = nativeGetNextFrame(nativeHandle);
		if(pixels == null) {
			if(--numPlays <= 0)
				return null;
			rewind();
			pixels = nativeGetNextFrame(nativeHandle);
		}
		return new RGBA8Frame(getWidth(), getHeight(), pixels);
	}

	
	@Override
	public String toString() {
		return getURL() + " (d=" + getDuration() + " fr=" + getFrameRate() + " fc=" + getFrameCount() + " w=" + getWidth() + " h=" + getHeight() + ")";
	}

	private double frameToTime(long frame) {
		return getDuration() * getFrameRate() / frame;
	}

	private static native long nativeCreate(String url);

	private static native void nativeDispose(long nativeHandle);

	private static native double nativeGetDuration(long nativeHandle);

	private static native double nativeGetFrameRate(long nativeHandle);

	private static native long nativeGetFrameCount(long nativeHandle);

	private static native int nativeGetWidth(long nativeHandle);

	private static native int nativeGetHeight(long nativeHandle);

	private static native void nativeRewind(long nativeHandle);

	private static native byte[] nativeGetFrame(long nativeHandle, double time);

	private static native byte[] nativeGetNextFrame(long nativeHandle);

	private static native int nativeLoadFrame(long nativeHandle, double time, int textureId);

	private static native int nativeLoadFrames(long nativeHandle, int numFrames, int textureId);
}
