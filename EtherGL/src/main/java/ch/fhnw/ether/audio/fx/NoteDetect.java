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

package ch.fhnw.ether.audio.fx;

import java.util.Arrays;

import ch.fhnw.ether.audio.IAudioRenderTarget;
import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.media.PerTargetState;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.media.StateHandle;

public class NoteDetect extends AbstractRenderCommand<IAudioRenderTarget,NoteDetect.State> {
	private static final Parameter TRESH     = new Parameter("tresh", "Threshold",       0,    1,     0.4f);
	private static final Parameter DELAY     = new Parameter("delay", "Sample delay",    0,    0.05f, 0.01f);
	private static final Parameter HARMONIC0 = new Parameter("h0",    "1st harmonic",    0,    1,     0f);
	private static final Parameter HARMONIC1 = new Parameter("h1",    "2nd harmonic",    0,    1,     0f);

	private final StateHandle<BandsButterworth.State> bands;
	private final StateHandle<OnsetDetect.State>      onset;
	private final double                              holdTime;

	public class State extends PerTargetState<IAudioRenderTarget> {
		private final int       N;
		private       float     onsetv;
		private final float[]   values;
		private final float[]   velocities;
		private final double[]  noteTimes;
		private final boolean[] notes;
		private       double    sampleTime;
		private       double    now;
		
		public State(IAudioRenderTarget target) throws RenderCommandException {
			super(target);
			N          = bands.get(target).numBands();
			values     = new float[N+36];
			velocities = new float[values.length];
			notes      = new boolean[values.length];
			noteTimes  = new double[values.length];
		}

		public void process(State state) throws RenderCommandException {
			final IAudioRenderTarget target = state.getTarget();
			now    = target.getTime();
			onsetv = onset.get(target).onset();
			if(onsetv > 0.3f)
				sampleTime = now + getVal(DELAY); 
			if(now > sampleTime) {
				bands.get(target).power(values);
				float h0 = getVal(HARMONIC0);
				float h1 = getVal(HARMONIC1);
				for(int i = 0; i < N; i++) {
					if(values[i] > 0.3f) {
						values[i + 12] *= h0;
						values[i + 24] *= h1;
					}
				} 
				System.arraycopy(values, 0, velocities, 0, velocities.length);
				sampleTime = 0;
			}
			else
				Arrays.fill(values, 0f);
			final float tresh = getVal(TRESH);
			for(int i = 0; i < values.length; i++)
				if(values[i] > tresh)
					noteTimes[i] = now + holdTime;
		}

		public boolean[] notes() {
			for(int i = 0; i < notes.length; i++)
				notes[i] = now < noteTimes[i];
			return notes;
		}
		
		public float[] velocities() {
			return velocities;
		}
	}

	public NoteDetect(StateHandle<BandsButterworth.State> bands, StateHandle<OnsetDetect.State> onset, double holdTimeInSec) {
		super(TRESH, DELAY, HARMONIC0, HARMONIC1);
		this.bands    = bands;
		this.onset    = onset;
		this.holdTime = holdTimeInSec;
	}

	@Override
	protected void run(State state) throws RenderCommandException {
		state.process(state);
	}

	@Override
	public State createState(IAudioRenderTarget target) throws RenderCommandException {
		return new State(target);
	}
}