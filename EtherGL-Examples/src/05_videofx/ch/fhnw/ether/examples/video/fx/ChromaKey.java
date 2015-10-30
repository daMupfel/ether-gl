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

package ch.fhnw.ether.examples.video.fx;

import java.nio.ByteBuffer;

import ch.fhnw.ether.image.Frame;
import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.media.Stateless;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.fx.AbstractVideoFX;
import ch.fhnw.util.color.ColorUtilities;


public class ChromaKey extends AbstractVideoFX<Stateless<IVideoRenderTarget>> {
	private static final Parameter HUE    = new Parameter("hue",   "Hue",                0, 1,    0.5f);
	private static final Parameter RANGE  = new Parameter("range", "Color Range",        0, 0.5f, 0.1f);
	private static final Parameter S_MIN  = new Parameter("sMin",  "Saturation Minimum", 0, 1,    0.1f);
	private static final Parameter B_MIN  = new Parameter("bMin",  "Brightness Minimum", 0, 1,    0.1f);

	private final Frame mask;

	public ChromaKey(Frame mask) {
		super(HUE, RANGE, S_MIN, B_MIN);
		this.mask = mask;
	}

	@Override
	protected void processFrame(double playOutTime, Stateless<IVideoRenderTarget> state, Frame frame) {
		final float h  = getVal(HUE);
		final float r  = getVal(RANGE);
		final float s  = getVal(S_MIN);
		final float b  = getVal(B_MIN);
		final float hh = wrap(h + r);
		final float hl = wrap(h - r);

		frame.processLines((final ByteBuffer pixels, final int j)->{
			final float[] hsb = new float[frame.dimI * 3];
			final int     pos = pixels.position();
			ByteBuffer mask = this.mask.pixels.asReadOnlyBuffer();
			mask.position(pos);
			ColorUtilities.getHSBfromRGB(mask, hsb, this.mask.pixelSize);
			pixels.position(pos);
			for(int i = 0; i < frame.dimI; i++) {
				int idx = i * 3;
				if(hsb[idx+1] > s && hsb[idx+2] > b && hsb[idx+0] > hl && hsb[idx+0] < hh) {
					pixels.get();
					pixels.get();
					pixels.get();
					pixels.get();
				} else {
					pixels.put(toByte(this.mask.getFloatComponent(i, j, 0)));
					pixels.put(toByte(this.mask.getFloatComponent(i, j, 1)));
					pixels.put(toByte(this.mask.getFloatComponent(i, j, 2)));
					pixels.put(toByte(this.mask.getFloatComponent(i, j, 3)));
				}
			}
		});
	}
}
