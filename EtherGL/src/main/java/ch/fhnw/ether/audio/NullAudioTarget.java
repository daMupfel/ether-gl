package ch.fhnw.ether.audio;

import ch.fhnw.ether.media.AbstractMediaTarget;

public class NullAudioTarget extends AbstractMediaTarget<AudioFrame,IAudioRenderTarget> implements IAudioRenderTarget {
	private final int   numChannels;
	private final float sRate;
	private double      sTime;

	public NullAudioTarget(int numChannels, float sampleRate) {
		super(Thread.NORM_PRIORITY);
		this.numChannels = numChannels;
		this.sRate       = sampleRate;
	}

	@Override
	public void render() {
		sTime += getFrame().samples.length;
	}

	@Override
	public double getTime() {
		return sTime / (getSampleRate() * getNumChannels());
	}

	@Override
	public int getNumChannels() {
		return numChannels;
	}

	@Override
	public float getSampleRate() {
		return sRate;
	}
}
