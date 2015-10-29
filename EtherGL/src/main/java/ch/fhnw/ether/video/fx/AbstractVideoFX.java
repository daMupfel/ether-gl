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

package ch.fhnw.ether.video.fx;

import java.util.Set;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.image.RGBA8Frame;
import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.media.PerTargetState;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.VideoFrame;
import ch.fhnw.util.TextUtilities;

public abstract class AbstractVideoFX<S extends PerTargetState<IVideoRenderTarget>> extends AbstractRenderCommand<IVideoRenderTarget, S> {
	protected final Frame EMPTY = new RGBA8Frame(1,1);
	
	protected long                        frame;
	protected Class<? extends Frame>[]    frameTypes;
	protected Set<Class<? extends Frame>> preferredTypes;

	protected AbstractVideoFX(Parameter ... parameters) {
		super(parameters);
	}

	public final static float toFloat(final byte v) {
		return (v & 0xFF) / 255f;
	}

	public final static byte toByte(final float v) {
		if(v < 0f) return 0;
		if(v > 1f) return -1;
		return (byte) (v * 255f);
	}

	public final static byte toByte(final double v) {
		if(v < 0.0) return 0;
		if(v > 1.0) return -1;
		return (byte) (v * 255.0);
	}

	public final static float wrap(final float v) {
		float result = v % 1f;
		return result < 0 ? result + 1 : result;
	}

	public final static float mix(final float val0, final float val1, float w) {
		return val0 * w + (1f-w) * val1;
	}
	
	@Override
	protected final void run(S state) throws RenderCommandException {
		VideoFrame frame = state.getTarget().getFrame();
		processFrame(frame.playOutTime, state, frame.frame);
	}
	
	protected abstract void processFrame(double playOutTime, S state, Frame frame);
	
	@Override
	public String toString() {
		return TextUtilities.getShortClassName(this);
	}
}
