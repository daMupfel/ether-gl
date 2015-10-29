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

/**
 * Scheduler interface for execution of actions on model thread and animation.
 * 
 * Thread-safety: All methods are thread safe and can be called from any thread.
 * 
 * @author radar
 */
public interface IScheduler {
	interface IAction {
		/**
		 * Action to be run, implemented by client.
		 * 
		 * @param time
		 *            time since application start, in seconds
		 */
		void run(double time);
	}

	interface IAnimationAction {
		/**
		 * Repeated animation action to be run, implemented by client.
		 * 
		 * @param time
		 *            time since application start, in seconds
		 * @param interval
		 *            interval of repeated action, in seconds
		 * @return false to stop repeated execution of action, true otherwise.
		 */
		boolean run(double time, double interval);
	}

	/**
	 * Add an action to the model animation loop until it removes itself.
	 * Thread-safe.
	 * 
	 * @param action
	 *            Action to be run
	 */
	void animate(IAnimationAction action);

	/**
	 * Run an action on model thread once. Thread-safe.
	 * 
	 * @param action
	 *            Action to be run
	 */
	void run(IAction action);

	/**
	 * Run an action on model thread once, with given delay.
	 * 
	 * @param delay
	 *            Delay before action is run, in seconds
	 * @param action
	 *            Action to be run
	 */
	void run(double delay, IAction action);
	
	/**
	 * Request repaint.
	 */
	void repaint();

	/**
	 * Returns true if caller calls from scene thread.
	 */
	boolean isSceneThread();

	/**
	 * Returns true if caller calls from render thread.
	 */
	boolean isRenderThread();
}
