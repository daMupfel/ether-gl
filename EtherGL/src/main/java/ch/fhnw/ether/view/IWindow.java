package ch.fhnw.ether.view;

import java.io.File;

import com.jogamp.opengl.GLRunnable;

public interface IWindow {
	/**
	 * Shows / hides this window.
	 */
	void setVisible(boolean visible);

	/**
	 * Set this window's position (in window units)
	 */
	void setPosition(int x, int y);

	/**
	 * Set this window's size.
	 */
	void setSize(int width, int height);

	/**
	 * Enable or disable this window as a fullscreen window.
	 */
	void setFullscreen(boolean enabled);

	/**
	 * Enable or disable mouse pointer for this window.
	 */
	void setPointerVisible(boolean visible);

	/**
	 * Confine or unconfine pointer for this window.
	 */
	void setPointerConfined(boolean confined);

	/**
	 * Set pointer icon for this window.
	 */
	void setPointerIcon(File pngImage, int hotspotX, int hotspotY);

	/**
	 * Warp pointer to x y (in pixel units), in right-handed window coordinates
	 * (origin bottom left).
	 */
	void warpPointer(int x, int y);

	/**
	 * Convert from pixel to window units.
	 */
	int convertFromPixelToWindowUnits(int value);
	
	/**
	 * Convert from window to pixel units.
	 */
	int convertFromWindowToPixelUnits(int value);
	
	
	/**
	 * Display (i.e. render) this view. Must be run from render thread.
	 */
	void display(GLRunnable runnable);
}
