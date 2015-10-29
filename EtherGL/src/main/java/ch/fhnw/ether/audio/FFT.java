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

package ch.fhnw.ether.audio;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jtransforms.fft.FloatFFT_1D;

import ch.fhnw.ether.audio.AudioUtilities.Window;
import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.PerTargetState;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.util.IModifier;
import ch.fhnw.util.Log;
import ch.fhnw.util.math.MathUtilities;

public class FFT extends AbstractRenderCommand<IAudioRenderTarget,FFT.State> {
	private static final Log log = Log.create();

	private final float  minFreq;
	private final Window windowType;

	public class State extends PerTargetState<IAudioRenderTarget> {
		private final FloatFFT_1D   fft;
		private final BlockBuffer   buffer;
		private final int           fftSize;
		private final int           fftSize2;
		private final List<float[]> spectrum = new LinkedList<>();
		private       float[]       block;
		private final float         sRate;
		private final float[]       power;
		private       float[]       pcm0;
		private       int           pcm0rd;
		private       float[]       pcm1;
		private       int           pcm1rd;

		State(IAudioRenderTarget target) {
			super(target);
			sRate    = target.getSampleRate();
			fftSize  = MathUtilities.nextPowerOfTwo((int)(sRate / minFreq));
			fftSize2 = fftSize / 2;
			log.info("FFT of " + fftSize + " at " + sRate + " Hz");
			fft      = new FloatFFT_1D(fftSize);
			buffer   = new BlockBuffer(fftSize, true, windowType);
			block    = new float[fftSize];
			power    = new float[fftSize2];
			pcm1     = new float[fftSize];
			pcm1rd   = fftSize2;
			pcm0rd   = fftSize;
		}

		void process(AudioFrame frame) {
			buffer.add(frame.getMonoSamples());
			int nBlocks = 0;
			for(block = buffer.nextBlock(); block  != null; block = buffer.nextBlock()) {
				if(nBlocks == 0)
					Arrays.fill(power, 0f);

				fft.realForward(block);
				spectrum.add(block);
				final int lim = block.length / 2;
				for(int i = 0; i < lim; i+= 2) {
					final float  re = block[i+0];
					final float  im = block[i+1];
					final double p  = Math.sqrt(re * re + im * im);
					power[i >> 1] += (float)p;
				}
				nBlocks++;
			}

			if(nBlocks > 0) {
				float div = nBlocks;
				for(int i = 0; i < power.length; i++)
					power[i] /= div;
			}
		}

		public float power(float fLow, float fHigh) {
			return power(fLow, fHigh, power);
		}

		public float power(final float fLow, final float fHigh, final float[] power) {
			int iLow  = f2idx(fLow);
			int iHigh = f2idx(fHigh);
			if(iHigh <= iLow) iHigh = iLow + 1;
			if(iHigh >= fftSize) iHigh = fftSize;
			if(iLow  >= iHigh) iLow = iHigh - 1;
			double result = 0;
			for(int i = iLow; i < iHigh; i++)
				result += power[i];

			return (float) result;
		}

		public float[] power() {
			return power;
		}

		public int f2idx(float f) {
			int result = (int) ((fftSize * f) / sRate);
			if(result < 0)         return 0;
			if(result >= fftSize2) return fftSize2 - 1;
			return result;
		}

		public float idx2f(int idx) {
			return (idx * sRate) / fftSize;
		}

		public int size() {
			return fftSize;
		}

		public void inverse() {
			final AudioFrame frame   = getTarget().getFrame();
			final float[]    samples = frame.samples;

			if(spectrum.size() < Math.max(1, 2 * (frame.samples.length / frame.nChannels) / fftSize)) {
				Arrays.fill(samples, 0f);
				return;
			}

			final int nChannels = frame.nChannels;
			if(frame.isModified()) {
				for(int i = 0; i < samples.length; i += nChannels) {
					if(pcm0rd >= fftSize) {
						pcm0  = spectrum.remove(0);
						pcm0rd = 0;
					}
					if(pcm1rd >= fftSize) {
						pcm1  = spectrum.remove(0);
						pcm1rd = 0;
					}
					pcm0rd++;
					pcm1rd++;
				}
			} else {
				for(int i = 0; i < samples.length; i += nChannels) {
					if(pcm0rd >= fftSize) {
						pcm0  = spectrum.remove(0);
						fft.realInverse(pcm0, true);
						pcm0rd = 0;
					}
					if(pcm1rd >= fftSize) {
						pcm1  = spectrum.remove(0);
						fft.realInverse(pcm1, true);
						pcm1rd = 0;
					}
					float sample = (pcm0[pcm0rd++] + pcm1[pcm1rd++]) / 2;
					for(int c = 0; c < nChannels; c++)
						samples[i+c] = sample;
				}
				frame.modified();
			}
		}

		public void modifySpectrum(IModifier<float[]> modifier) throws RenderCommandException {
			for(float[] spectrum : state().get(target).spectrum)
				modifier.modify(spectrum);
		}
	}

	public FFT(float minFreq, Window windowType) {
		this.minFreq    = minFreq;
		this.windowType = windowType;
	}

	@Override
	protected void run(State state) throws RenderCommandException {
		state.process(state.getTarget().getFrame());
	}	

	@Override
	protected State createState(IAudioRenderTarget target) throws RenderCommandException {
		return new State(target);
	}

	public int size(IAudioRenderTarget target) throws RenderCommandException {
		return state().get(target).size();
	}

	public Window getWindowType() {
		return windowType;
	}

	public float getMinFreq() {
		return minFreq;
	}
}
