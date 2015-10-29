package ch.fhnw.ether.audio;

import java.util.Arrays;

import ch.fhnw.util.IModifierFloat;

public class Delay {
	private float[]     buffer;
	private int         ptr;
	private int         len;
	private final int   nChannels;
	private final float sRate;

	public Delay(IAudioRenderTarget target, double lengthInSec) {
		nChannels = target.getNumChannels();
		sRate     = target.getSampleRate();
		len       = sec2samples(lengthInSec);
		buffer    = new float[len];
	}

	private int sec2samples(final double lengthInSec) {
		return (int)(nChannels * lengthInSec * sRate) + 1;
	}

	public void setLength(double lengthInSec) {
		int newLen = sec2samples(lengthInSec);
		if(newLen > len) {
			buffer = Arrays.copyOf(buffer, newLen);
			int length = (len - ptr) - 1;
			System.arraycopy(buffer, ptr + 1, buffer, buffer.length - length, length);
		}
		len = newLen;
	}

	public void modifySamples(AudioFrame frame, IModifierFloat modifier) {
		final float[] samples = frame.samples;
		for(int i = 0; i < samples.length; i++) {
			final float sample = samples[i];
			buffer[ptr++] = sample;
			if(ptr >= len) ptr = 0;
			samples[i] = modifier.modify(sample, i);
		}
		frame.modified();
	}

	public float get(final double delay) {
		final int c   = ptr % nChannels;
		final int off = (int)(delay * sRate);
		return buffer[(ptr + len - (off * nChannels + c)) % len];
	}
}
