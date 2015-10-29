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

package ch.fhnw.ether.controller.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.util.Pair;

public class DefaultScheduler implements IScheduler {
	private static final long START_TIME = System.nanoTime();

	private final IController controller;
	
	private final double interval;

	private final Thread sceneThread;
	private final Thread renderThread;

	private final List<IAnimationAction> animations = new ArrayList<>();
	private final List<Pair<Double, IAction>> actions = new ArrayList<>();

	private final AtomicBoolean repaint = new AtomicBoolean();
	
	private final Semaphore renderMonitor = new Semaphore(0);
	private final AtomicReference<Runnable> renderRunnable = new AtomicReference<>();

	public DefaultScheduler(IController controller, float fps) {
		this.controller = controller;
		this.interval = 1 / fps;
		this.sceneThread = new Thread(this::runSceneThread, "scenethread");
		this.renderThread = new Thread(this::runRenderThread, "renderthread");

		sceneThread.start();
		renderThread.start();		
	}

	@Override
	public void animate(IAnimationAction action) {
		synchronized (animations) {
			animations.add(action);
		}
	}

	@Override
	public void run(IAction action) {
		synchronized (actions) {
			actions.add(new Pair<>(0.0, action));
		}
	}

	@Override
	public void run(double delay, IAction action) {
		synchronized (actions) {
			actions.add(new Pair<>(getTime() + delay, action));
		}
	}

	@Override
	public void repaint() {
		repaint.set(true);
	}

	@Override
	public boolean isSceneThread() {
		return Thread.currentThread().equals(sceneThread);
	}

	@Override
	public boolean isRenderThread() {
		return Thread.currentThread().equals(renderThread);
	}

	private void runSceneThread() {
		while (true) {
			double time = getTime();

			// run actions first
			{
				List<Pair<Double, IAction>> aa;
				synchronized (actions) {
					aa = new ArrayList<>();
					for (Pair<Double, IAction> p : actions) {
						if (time > p.left)
							aa.add(p);
					}
					if (!aa.isEmpty())
						actions.removeAll(aa);
				}
				for (Pair<Double, IAction> a : aa) {
					try {
						a.right.run(time);
					} catch (Exception e) {
						e.printStackTrace();
					}
					repaint.set(true);
				}
			}

			// run animations second
			{
				List<IAnimationAction> aa;
				synchronized (animations) {
					aa = new ArrayList<>(animations);
				}
				List<IAnimationAction> ra = new ArrayList<>();
				for (IAnimationAction a : aa) {
					try {
						if (!a.run(time, interval))
							ra.add(a);
					} catch (Exception e) {
						e.printStackTrace();
					}
					repaint.set(true);
				}
				if (!ra.isEmpty()) {
					synchronized (animations) {
						animations.removeAll(ra);
					}
				}
			}


			if (repaint.getAndSet(false) && renderMonitor.availablePermits() < 1) {
				renderRunnable.set(controller.getRenderManager().getRenderRunnable());
				renderMonitor.release();
			}

			double elapsed = getTime() - time;
			double remaining = interval - elapsed;
			if (remaining > 0) {
				try {
					// System.out.println("sleep for " + remaining);
					Thread.sleep((long) (remaining * 1000));
				} catch (Exception e) {
				}
			} else {
				System.out.println("scene thread overload: max=" + interval + " used=" + elapsed);
			}
		}
	}

	private void runRenderThread() {
		while (true) {
			try {
				renderMonitor.acquire();
				renderRunnable.get().run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private double getTime() {
		long elapsed = System.nanoTime() - START_TIME;
		return elapsed / 1000000000.0;
	}
}
