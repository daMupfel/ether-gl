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

package ch.fhnw.ether.scene.mesh.material;

import java.net.URL;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.media.AbstractMediaTarget;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.media.RenderProgram;
import ch.fhnw.ether.video.AbstractVideoSource;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.URLVideoSource;
import ch.fhnw.ether.video.VideoFrame;
import ch.fhnw.util.Log;
import ch.fhnw.util.UpdateRequest;

import com.jogamp.opengl.GL3;

/**
 * Texture data encapsulation (FIXME: needs extension/generalization, array tex, 3d tex etc)
 *
 * @author radar
 */
public class Texture extends AbstractMediaTarget<VideoFrame, IVideoRenderTarget> implements IVideoRenderTarget {
	private static final Log log = Log.create();

	private final UpdateRequest updater = new UpdateRequest();

	private Frame singleFrame;

	public Texture() {
		super(Thread.MIN_PRIORITY);
	}

	public Texture(Frame frame) {
		this();
		setData(frame);
	}

	public Texture(URL url) {
		this(url, true);
	}

	public Texture(URL url, boolean autoStart) {
		super(Thread.MIN_PRIORITY);
		setData(url);
	}

	public Texture(AbstractVideoSource<?> source) {
		this(source, true);
	}

	public Texture(AbstractVideoSource<?> source, boolean autoStart) {
		super(Thread.MIN_PRIORITY);
		setData(source);
	}

	public void setData(URL url) {
		if(URLVideoSource.isStillImage(url)) {
			try {
				setData(Frame.create(url));
			} catch (Throwable e) {
				throw new IllegalArgumentException("can't load image " + url);
			}
		}
		else
			setData(new URLVideoSource(url));
	}

	public void setData(AbstractVideoSource<?> source) {
		try {
			singleFrame = null;
			useProgram(new RenderProgram<>(source));
			start();
			updater.request();
		} catch (Throwable e) {
			throw new IllegalArgumentException("can't load image " + source);
		}
	}

	@Override
	protected void runOneCycle() throws RenderCommandException {
		super.runOneCycle();
		updater.request();
	}
	
	public void setData(Frame frame) {
		singleFrame = frame;
		try {
			stop();
		} catch (RenderCommandException e) {
			log.warning(e);
		}
		updater.request();
	}

	public boolean needsUpdate() {
		return updater.testAndClear();
	}

	public int getWidth() {
		Frame frame = currentFrame();
		return frame == null ? 0 : frame.dimI; 
	}

	public int getHeight() {
		Frame frame = currentFrame();
		return frame == null ? 0 : frame.dimJ; 
	}

	@Override
	public String toString() {
		return "texture[w=" + getWidth() + " h=" + getHeight() + "]";
	}

	public void update() {
		setRendering(true);
		try {
			runOneCycle();
		} catch (RenderCommandException e) {
			log.warning(e);
		}
		setRendering(false);
		updater.request();
	}

	public void load(GL3 gl, int target, int textureId) {
		Frame frame = currentFrame();
		if(frame != null)
			frame.load(gl, target, textureId);
	}	

	private Frame currentFrame() {
		if(singleFrame != null) 
			return singleFrame;
		VideoFrame frame = getCurrentFrame();
		return frame == null ? null : frame.frame;
	}
}
