package ch.fhnw.ether.audio.fx;

import java.util.Arrays;

import ch.fhnw.ether.audio.AudioFrame;
import ch.fhnw.ether.audio.AudioUtilities;
import ch.fhnw.ether.audio.BlockBuffer;
import ch.fhnw.ether.audio.ButterworthFilter;
import ch.fhnw.ether.audio.IAudioRenderTarget;
import ch.fhnw.ether.audio.AudioUtilities.Window;
import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.media.PerTargetState;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.media.StateHandle;

public class OnsetDetect extends AbstractRenderCommand<IAudioRenderTarget,OnsetDetect.State> {
	private static final Parameter SENS       = new Parameter("sens",   "Sensitivity",    0f,    100f,    100-25);
	private static final Parameter BAND_DECAY = new Parameter("bDecay", "Per band decay", 0.88f, 0.9999f, 0.9f);
	private static final Parameter AVG_DECAY  = new Parameter("aDecay", "Average decay",  0.88f, 0.9999f, 0.999f);
	
	private static final float[] BANDS      = { 80, 2000, 6000, 13000, 16000 };
	private static final double  CHUNK_SIZE = 0.02;
	private static final float   ATTACK     = 0.9f;

	private final StateHandle<BandsButterworth.State> bands;

	public class State extends PerTargetState<IAudioRenderTarget> {
		private final float[]               lastBands;
		private final float[]               bands;
		private final float[]               fluxBands;
		private final float[]               thresholds;
		private final ButterworthFilter[]   filters;
		private float                       flux;
		private float                       threshold;
		private final BlockBuffer           buffer;
		
		public State(IAudioRenderTarget target) throws RenderCommandException {
			super(target);
			this.buffer = new BlockBuffer((int) (target.getSampleRate() * CHUNK_SIZE), false, Window.RECTANGLE);
			if(OnsetDetect.this.bands == null) {
				lastBands  = new float[BANDS.length - 1];
				bands      = new float[BANDS.length - 1];
				fluxBands  = new float[BANDS.length - 1];
				thresholds = new float[BANDS.length - 1];
				filters   = new ButterworthFilter[BANDS.length - 1];
				for(int i = 0; i < filters.length; i++)
					filters[i] = ButterworthFilter.getBandpassFilter(target.getSampleRate(), BANDS[i], BANDS[i+1]);
			} else {
				lastBands  = new float[OnsetDetect.this.bands.get(target).numBands()];
				bands      = new float[OnsetDetect.this.bands.get(target).numBands()];
				fluxBands  = new float[OnsetDetect.this.bands.get(target).numBands()];
				thresholds = new float[OnsetDetect.this.bands.get(target).numBands()];
				filters    = new ButterworthFilter[OnsetDetect.this.bands.get(target).numBands()];
			}
		}

		public void process(AudioFrame frame) throws RenderCommandException {
			final float decay = getVal(BAND_DECAY);
			final float sens  = Math.max(0.1f, getMax(SENS) - getVal(SENS));
			if(OnsetDetect.this.bands == null) {
				final float[] monoSamples = frame.getMonoSamples();
				buffer.add(monoSamples);
				
				flux = 0;
				for(;;) {
					float[] block = buffer.nextBlock();
					if(block == null) break;
					for(int band = 0; band < BANDS.length - 1; band++) {
						final float[] samples = block.clone();
						if(samples.length > 5) {
							filters[band].processBand(samples);
							bands[band] = AudioUtilities.energy(samples);
							processBand(band, decay, sens);
						}
					}
				}
			} else {
				OnsetDetect.this.bands.get(getTarget()).power(bands);
				for(int band = 0; band < bands.length; band++)
					processBand(band, decay, sens);
			}

			if(flux > threshold)
				threshold = threshold * ATTACK + (1-ATTACK) * flux;
			else
				threshold *= getVal(AVG_DECAY);

			System.arraycopy(bands, 0, lastBands, 0, bands.length );
		}

		private void processBand(final int band, final float decay, final float sens) {
			float d = bands[band] - lastBands[band];
			fluxBands[band] = d;
			if(d > thresholds[band]) {
				thresholds[band] = thresholds[band] * ATTACK + (1-ATTACK) * d;
				flux += d / (sens * thresholds[band]);
			}
			else
				thresholds[band] *= decay;
		}

		public float[] fluxBands() {
			return fluxBands;
		}

		public float[] thresholds() {
			return thresholds;
		}

		public float flux() {
			return flux;
		}

		public float threshold() {
			return threshold;
		}

		public float onset() {
			float result = flux() / threshold();
			if(result > 1) result = 1;
			return result;
		}

		public void reset() {
			threshold = 0;
			Arrays.fill(bands, 0f);
			Arrays.fill(lastBands, 0f);
			Arrays.fill(fluxBands, 0f);
			Arrays.fill(thresholds, 0f);
		}
	}

	public OnsetDetect() {
		super(SENS, BAND_DECAY, AVG_DECAY);
		this.bands = null;
	}

	public OnsetDetect(StateHandle<BandsButterworth.State> bands) {
		this.bands = bands;
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
