package ch.fhnw.ether.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import ch.fhnw.ether.media.AbstractMediaTarget;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.util.FloatList;

public class FileAudioTarget extends AbstractMediaTarget<AudioFrame,IAudioRenderTarget> implements IAudioRenderTarget {
	private final int        numChannels;
	private final float      sRate;
	private double           sTime;
	private final File       file;
	private final FloatList  buffer = new FloatList();

	public FileAudioTarget(File file, int numChannels, float sampleRate) {
		super(Thread.NORM_PRIORITY);
		this.numChannels = numChannels;
		this.sRate       = sampleRate;
		this.file        = file;
	}

	@Override
	public void render() {
		sTime += getFrame().samples.length;
		buffer.addAll(getFrame().samples);
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

	@Override
	public void stop() throws RenderCommandException {
		byte[] bytes = new byte[buffer.size() * 2];
		for(int i = buffer.size(); --i >= 0;) {
			int val = (int) (buffer.get(i) * Short.MAX_VALUE);
			bytes[i*2+0] = (byte) val;
			bytes[i*2+1] = (byte) (val >> 8);
		}
		AudioInputStream in = new AudioInputStream(new ByteArrayInputStream(bytes), 
				new AudioFormat(sRate, 16, 1, true, false), 
				buffer.size());
		try(FileOutputStream out = new FileOutputStream(file)) {
			AudioSystem.write(in, Type.WAVE, out);
		} catch (IOException e) {
			throw new RenderCommandException(e);
		}
		super.stop();
	}
}
