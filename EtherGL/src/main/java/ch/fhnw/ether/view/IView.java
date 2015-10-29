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
 */package ch.fhnw.ether.view;

import java.util.EnumSet;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.util.Viewport;

/**
 * A 'view' here is a view with some control functionality, i.e. it handles the
 * rendering of the model and also the user input specific to the view.
 * 
 * @author radar
 */
public interface IView {
	enum ViewType {
		INTERACTIVE_VIEW, MAPPED_VIEW
	}

	enum ViewFlag {
		/** Grid visibility in navigation tool. */
		GRID,
		/** Enable line smoothing. */
		SMOOTH_LINES,
	}

	public static final class Config {
		private final ViewType viewType;
		private final int fsaaSamples;
		private final EnumSet<ViewFlag> flags;

		public Config(ViewType viewType, int fsaaSamples, ViewFlag... flags) {
			this.viewType = viewType;
			this.fsaaSamples = fsaaSamples;
			this.flags = EnumSet.noneOf(ViewFlag.class);
			for (ViewFlag flag : flags)
				this.flags.add(flag);
		}

		public ViewType getViewType() {
			return viewType;
		}

		public int getFSAASamples() {
			return fsaaSamples;
		}

		public boolean has(ViewFlag flag) {
			return flags.contains(flag);
		}
	}

	Config INTERACTIVE_VIEW = new Config(ViewType.INTERACTIVE_VIEW, 0, ViewFlag.GRID);
	Config MAPPED_VIEW = new Config(ViewType.MAPPED_VIEW, 0, ViewFlag.GRID);

	/**
	 * Dispose this view and release all associated resources
	 */
	void dispose();

	/**
	 * Check whether view is enabled for rendering.
	 * 
	 * @return true if view is enabled, false otherwise
	 */
	boolean isEnabled();

	/**
	 * Enable or disable view for rendering.
	 * 
	 * @param enabled
	 *            enables view if true, disables otherwise
	 */
	void setEnabled(boolean enabled);

	/**
	 * Get view configuration.
	 * 
	 * @return the view configuration
	 */
	Config getConfig();

	/**
	 * Get the controller this view belongs to.
	 * 
	 * @return the controller
	 */
	IController getController();

	/**
	 * Get viewport [x, y, w, h].
	 * 
	 * @return the viewport
	 */
	Viewport getViewport();

	/**
	 * Get the underlying window of this view.
	 * 
	 * @return the window
	 */
	IWindow getWindow();
}
