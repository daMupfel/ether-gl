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

import ch.fhnw.ether.audio.AudioFrame;
import ch.fhnw.ether.audio.IAudioRenderTarget;
import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.media.PerTargetState;
import ch.fhnw.ether.media.RenderCommandException;

/**
 * This is an empty template for an audio command with state. 
 * Use this as your starting point for your own commands that
 * need to keep audio data around (e.g. delay buffers).
 * @author sschubiger
 *
 */
public class Template extends AbstractRenderCommand<IAudioRenderTarget,Template.State> {
	/* Expose a runtime parameter */
	private static final Parameter PARAM = new Parameter("p", "Some Param", 0, 1, 0);

	/**
	 * Per-target state. The same program - and hence commands - may be used on
	 * multiple targets. Thus any target specific state such as audio buffers
	 * should kept in a separate class called "State" by convention. 
	 */
	public class State extends PerTargetState<IAudioRenderTarget> {
		public State(IAudioRenderTarget target) {
			super(target);
		}

		/**
		 * Process one audio frame. 
		 * @param frame The frame to process.
		 */
		@SuppressWarnings("unused")
		public void process(AudioFrame frame) {
			final float   param     = getVal(PARAM);   // Get the param value
			final float[] samples   = frame.samples;   // Get the samples in the frame
			final int     nChannels = frame.nChannels; // Get the number of channels (1=Mono, 2=Stereo, ...)
			for(int i = 0; i < samples.length; i += nChannels)
				for(int c = 0; c < nChannels; c++) {
					// do some audio processing here
				}
			
			/* If you modified the frame, signal it to the system.
			 * It updates internal data structures and caches if necessary. 
			 */
			frame.modified();
		}
	}

	public Template() {
		/* Pass all your params to the super class. */
		super(PARAM);
	}

	@Override
	protected void run(State state) throws RenderCommandException {
		state.process(state.getTarget().getFrame());
	}

	@Override
	public State createState(IAudioRenderTarget target) throws RenderCommandException {
		return new State(target);
	}
}