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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComboBox;

import ch.fhnw.ether.image.RGBA8Frame;
import ch.fhnw.ether.media.RenderCommandException;
import ch.fhnw.ether.media.RenderProgram;
import ch.fhnw.ether.ui.ParameterWindow;
import ch.fhnw.ether.video.AWTFrameTarget;
import ch.fhnw.ether.video.IVideoRenderTarget;
import ch.fhnw.ether.video.URLVideoSource;
import ch.fhnw.ether.video.fx.AbstractVideoFX;
import ch.fhnw.util.CollectionUtilities;

public class SimpleVideoPlayer {
	public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException, RenderCommandException {
		URLVideoSource track    = new URLVideoSource(new File(args[0]).toURI().toURL());
		URLVideoSource mask     = args.length > 1 ? new URLVideoSource(new File(args[1]).toURI().toURL()) : null;
		AWTFrameTarget videoOut = new AWTFrameTarget();
	
		List<AbstractVideoFX<?>> fxs = CollectionUtilities.asList(
				new AnalogTVFX(),
				new BandPass(),
				new Convolution(),
				new FadeToColor(),
				new FakeThermoCam(),
				new MotionBlur(),
				new Posterize(),
				new RGBGain());
		
		AtomicInteger current = new AtomicInteger(0);

		if(mask != null) {
			RGBA8Frame maskOut  = new RGBA8Frame(track.getWidth(), track.getHeight());
			maskOut.useProgram(new RenderProgram<>(mask));
			fxs.add(new ChromaKey(maskOut));
			maskOut.start();
		}
		
		final RenderProgram<IVideoRenderTarget> program = new RenderProgram<>(track, fxs.get(current.get()));

		final JComboBox<AbstractVideoFX<?>> fxsUI = new JComboBox<>();
		for(AbstractVideoFX<?> fx : fxs)
			fxsUI.addItem(fx);
		fxsUI.addActionListener((ActionEvent e)->{
			int newIdx = fxsUI.getSelectedIndex();
			program.replace(fxs.get(current.get()), fxs.get(newIdx));
			current.set(newIdx);
		});
		new ParameterWindow(fxsUI, program);

		videoOut.useProgram(program);
		videoOut.start();
		Thread.sleep(5 * 60 * 1000);
		videoOut.stop();
	}

}
