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

package ch.fhnw.ether.video;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.media.PerTargetState;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.video.avfoundation.AVAsset;
import ch.fhnw.ether.video.jcodec.SequentialVideoTrack;
import ch.fhnw.util.Log;
import ch.fhnw.util.TextUtilities;

public class URLVideoSource extends AbstractVideoSource<URLVideoSource.State> {
	private static final Log log = Log.create();
	
	private static final boolean USE_AV_FOUNDATION = true;

	private final URL    url;
	private final int    numPlays;
	private int          width;
	private int          height;
	private double       frameRate  = FRAMERATE_UNKNOWN;
	private long         frameCount = FRAMECOUNT_UNKNOWN;

	public URLVideoSource(URL url) {
		this(url, Integer.MAX_VALUE);
	}

	public URLVideoSource(URL url, int numPlays) {
		this.url      = url;
		this.numPlays = numPlays;
		try {
			createState(null);
		} catch(Throwable t) {
			log.warning(t);
		}
	}

	@Override
	protected void run(State state) throws RenderCommandException {
		state.runInternal();
	}

	public static class State extends PerTargetState<IVideoRenderTarget> {
		protected final URL url;
		protected double    playOutTime;
		protected int       numPlays;
		protected double    startTime = -1;
		private final Frame frame;

		State(IVideoRenderTarget target, URL url) throws IOException {
			super(target);
			this.frame    = Frame.create(url);
			this.url      = url;
			this.numPlays = 0;
		}

		protected State(IVideoRenderTarget target, URL url, int numPlays) {
			super(target);
			this.frame    = null;
			this.url      = url;
			this.numPlays = numPlays;
		}

		public double getPlayOutTime() {
			return playOutTime;
		}

		public void runInternal() {
			if(startTime <= 0)
				startTime = getTarget().getTime();
			getTarget().setFrame(new VideoFrame(startTime + getPlayOutTime(), getNextFrame()));
		}

		protected Frame getNextFrame() {
			return frame;
		}
		protected int getWidth() {
			return frame.dimI;
		}
		protected int getHeight() {
			return frame.dimJ;
		}
		protected double getFrameRate() {
			return FRAMERATE_UNKNOWN;
		}
		protected long getFrameCount() {
			return 1;
		}
	}

	@Override
	protected State createState(IVideoRenderTarget target) throws RenderCommandException {
		try {
			State result = isStillImage(url) ? new State(target, url) : USE_AV_FOUNDATION ? new AVAsset(target, url, numPlays) : new SequentialVideoTrack(target, url, numPlays);
			width      = result.getWidth();
			height     = result.getHeight();
			frameRate  = result.getFrameRate();
			frameCount = result.getFrameCount();
			return result;
		} catch(Throwable t) {
			throw new RenderCommandException(t);
		}
	}

	public static boolean isStillImage(URL url) {
		return ImageIO.getImageReadersBySuffix(TextUtilities.getFileExtensionWithoutDot(url.getPath())).hasNext();
	}

	@Override
	public String toString() {
		return url.toString();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public double getFrameRate() {
		return frameRate;
	}

	@Override
	public long getFrameCount() {
		return frameCount;
	}
}
