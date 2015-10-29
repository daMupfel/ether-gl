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
 */package ch.fhnw.ether.audio.fx;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ch.fhnw.ether.audio.AudioUtilities;
import ch.fhnw.ether.audio.IAudioRenderTarget;
import ch.fhnw.ether.audio.FFT;
import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.PerTargetState;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.media.StateHandle;
import ch.fhnw.util.ClassUtilities;
import ch.fhnw.util.math.Vec2;

public class PitchDetect extends AbstractRenderCommand<IAudioRenderTarget,PitchDetect.State> {
	private static final float THRESHOLD = 0.2f;

	private final StateHandle<FFT.State> spectrum;
	private final int                    nHarmonics;
	
	public class State extends PerTargetState<IAudioRenderTarget> {
		private final AtomicReference<float[]> pitch = new AtomicReference<>(ClassUtilities.EMPTY_floatA);
		private final List<Vec2>               peaks = new ArrayList<>();

		public State(IAudioRenderTarget target) {
			super(target);
		}

		void process() throws RenderCommandException {
			final IAudioRenderTarget target = getTarget();
			final float[]            spec   = spectrum.get(target).power().clone();

			AudioUtilities.multiplyHarmonics(spec, nHarmonics);
			
			final BitSet peaks  = AudioUtilities.peaks(spec, 3, THRESHOLD);
			this.peaks.clear();

			for (int i = peaks.nextSetBit(0); i >= 0; i = peaks.nextSetBit(i+1))
				this.peaks.add(new Vec2(spec[i], spectrum.get(target).idx2f(i)));

			Collections.sort(this.peaks, (Vec2 v0, Vec2 v1)->v0.x < v1.x ? 1 : v0.x > v1.x ? -1 : 0);
			
			float[] pitch = new float[this.peaks.size()];
			for(int i = 0; i < pitch.length; i++)
				pitch[i] = this.peaks.get(i).y;
			this.pitch.set(pitch);
		}

		public float[] pitch() {
			return pitch.get();
		}
	}

	public PitchDetect(StateHandle<FFT.State> fftState, int nHarmonics) {
		this.spectrum   = fftState;
		this.nHarmonics = nHarmonics;
	}

	@Override
	protected void run(State state) throws RenderCommandException {
		state.process();
	}	

	@Override
	protected State createState(IAudioRenderTarget target) throws RenderCommandException {
		return new State(target);
	}
}
