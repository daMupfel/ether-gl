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

package ch.fhnw.ether.media;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ch.fhnw.util.Log;

public abstract class AbstractMediaTarget<F extends AbstractFrame, T extends IRenderTarget> implements IRenderTarget {
	private static final Log log = Log.create();

	public static final double SEC2NS = 1000 * 1000 * 1000;
	public static final double SEC2US = 1000 * 1000;
	public static final double SEC2MS = 1000;

	private   final int                                                priority;
	protected RenderProgram<T>                                         program;
	protected final AtomicBoolean                                      isRendering  = new AtomicBoolean();
	private   final Map<AbstractRenderCommand<T,?>, PerTargetState<T>> state        = new WeakHashMap<>();
	private   final long                                               startTime    = System.nanoTime();
	private   final AtomicReference<F>                                 frame        = new AtomicReference<>();
	private         F                                                  currentFrame;
	private         CountDownLatch                                     startLatch;

	protected AbstractMediaTarget(int threadPriority) {
		this.priority = threadPriority;
	}

	@Override
	public final void start() {
		if(program.getFrameSource().getFrameCount() == 1) {
			try {
				setRendering(true);
				runOneCycle();
			} catch(Throwable e) {
				log.severe(e);
			}
			setRendering(false);
		} else {
			startLatch = new CountDownLatch(1);
			Thread t = new Thread(()->{
				try {
					setRendering(true);
					startLatch.countDown();
					while(isRendering())
						runOneCycle();
				} catch(Throwable e) {
					setRendering(false);
					log.severe(e);
				}
			}, getClass().getName());
			t.setDaemon(true);
			t.setPriority(priority);
			t.start();
			try {
				startLatch.await();
			} catch (InterruptedException e) {
				log.severe(e);
			}
		}
	}

	protected void runOneCycle() throws RenderCommandException {
		program.runInternal(this);
		AbstractFrame tmp = getFrame();
		if(tmp != null) {
			render();
			currentFrame = frame.getAndSet(null);
			if(currentFrame.isLast())
				setRendering(false);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PerTargetState<?> getState(AbstractRenderCommand<?,?> cmd) throws RenderCommandException {
		synchronized (state) {
			PerTargetState<T> result = state.get(cmd);
			if(result == null) {
				result = cmd.createStateInternal(this);
				state.put((AbstractRenderCommand<T,?>) cmd, result);
			}
			return result;
		}
	}

	@SuppressWarnings("unused")
	public void useProgram(RenderProgram<T> program) throws RenderCommandException {
		this.program = program;	
		program.addTarget(this);
	}

	@Override
	public void stop() throws RenderCommandException {
		setRendering(false);
	}

	@Override
	public void render() throws RenderCommandException {}

	@Override
	public boolean isRendering() {
		return isRendering.get();
	}

	protected void setRendering(boolean state) {
		isRendering.set(state);
	}

	@Override
	public double getTime() {
		return (System.nanoTime() - startTime) / SEC2NS; 
	}

	@Override
	public void sleepUntil(double time) {
		try {
			if(time == NOT_RENDERING) {
				while(isRendering()) {
					Thread.sleep(2);
				}
			} else {
				time *= SEC2NS;
				long deadline = startTime + (long)time;
				long wait     = deadline - System.nanoTime();
				if(wait > 0) {
					Thread.sleep(wait / 1000000L, (int)(wait % 1000000L));
				} else {
					//	log.warning("Missed deadline by " + -wait + "ns");
				}
			}
		} catch(Throwable t) {
			log.severe(t);
		}
	}

	public F getFrame() {
		return frame.get();
	}

	public void setFrame(F frame) {
		this.frame.set(frame);
	}

	public F getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public AbstractFrameSource<T, ?> getFrameSource() {
		return program.getFrameSource();
	}	
}
