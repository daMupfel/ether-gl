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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import ch.fhnw.ether.media.AbstractMediaTarget;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.media.RenderProgram;

public final class JavaSoundTarget extends AbstractMediaTarget<AudioFrame,IAudioRenderTarget> implements IAudioRenderTarget {
	private static final float S2F = Short.MAX_VALUE;

	private AudioFormat    fmt;
	private SourceDataLine out;
	private int            outChannels;
	private int            bytesPerSample;
	private double         sTime;
	private final int      bufferSize;
	
	/**
	 * Create a new audio target using Java sound output.
	 */
	public JavaSoundTarget() {
		this(2048);
	}
	
	/**
	 * Create a new audio target using Java sound output.
	 *
	 * @param bufferSize The output buffer size. Values below 2048 produce audio glitches on most platforms.
	 */
	public JavaSoundTarget(int bufferSize) {
		super(Thread.MAX_PRIORITY);
		this.bufferSize = bufferSize;
	}

	@Override
	public void useProgram(RenderProgram<IAudioRenderTarget> program) throws RenderCommandException {
		try {
			if(out != null && out.isOpen())
				stop();
			
			AbstractAudioSource<?> src = (AbstractAudioSource<?>)program.getFrameSource();
			out            = AudioSystem.getSourceDataLine(new AudioFormat(src.getSampleRate(), 16, src.getNumChannels(), true, true));
			fmt            = out.getFormat();
			outChannels    = fmt.getChannels();
			bytesPerSample = fmt.getSampleSizeInBits() / 8;
			out.open(fmt, bufferSize);
			super.useProgram(program);
		} catch(Throwable t) {
			throw new RenderCommandException(t);
		}
	}

	@Override
	public void render() {

		if(!out.isRunning())
			out.start();

		final float[] samples  = getFrame().samples;
		final int     channels = getFrame().nChannels;

		final byte[] outBuffer   = new byte[(samples.length / channels) * outChannels * bytesPerSample];   
		int          outIdx      = 0;

		for(int i = 0; i < samples.length; i += channels) {
			for(int j = 0; j < outChannels; j++) {
				if(j < channels) {
					int s  = (int) (samples[i+j] * S2F);
					if(s > Short.MAX_VALUE) s = Short.MAX_VALUE;
					if(s < Short.MIN_VALUE) s = Short.MIN_VALUE;
					outBuffer[outIdx++] = (byte) (s >> 8);
					outBuffer[outIdx++] = (byte) s;
				} else {
					outBuffer[outIdx++] = 0;
					outBuffer[outIdx++] = 0;
				}
			}
		}

		sTime += outBuffer.length / 2;
		out.write(outBuffer, 0, outBuffer.length);
	}

	@Override
	public void stop() throws RenderCommandException {
		super.stop();
		out.drain();
		out.flush();
		out.close();
	}

	@Override
	public double getTime() {
		return sTime / (getSampleRate() * getNumChannels());
	}

	@Override
	public int getNumChannels() {
		return fmt.getChannels();
	}

	@Override
	public float getSampleRate() {
		return fmt.getSampleRate();
	}
}
