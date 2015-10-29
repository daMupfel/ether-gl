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

package ch.fhnw.ether.controller.hid.tuio;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.scene.camera.DefaultCameraControl;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.math.geometry.BoundingBox;
import ch.fhnw.util.net.osc.OSCHandler;
import ch.fhnw.util.net.osc.OSCServer;

// TODO: would be nice if we could use NEWT gestures here 
// TODO: add support for multiple interactive views
// TODO: check whether a cursor is actually within one of our views

/**
 * TUIO multitouch implementation. NOTE: totally untested.
 *
 * @author radar
 */
public class TUIO {
	public static final int DEFAULT_PORT = 3333;

	private static final long WAIT_TIME_MS = 50;

	private static final float SCALE_ROTATE = 360;
	private static final float SCALE_TRANSLATE = 5;
	private static final float SCALE_DISTANCE = 10;

	@SuppressWarnings("unused")
	private class Cursor {
		private int id;
		private float x;
		private float y;
		private float vx;
		private float vy;
		private float accel;
		private Cursor previous;

		public Cursor(int id, float x, float y, float vx, float vy, float accel, Cursor previous) {
			this.x = x;
			this.y = y;
			this.vx = vx;
			this.vy = vy;
			this.accel = accel;
			this.previous = previous;
			if (previous != null)
				previous.previous = null;
		}
	}

	private class Handler implements OSCHandler {
		private final List<Integer> alive = new ArrayList<>();

		@Override
		public Object[] handle(String[] address, int addrIdx, StringBuilder typeString, long timestamp, Object... args) {
			if (address.length < 3 || !address[1].equals("tuio") || !address[2].equals("2Dcur") || args.length == 0)
				return null;

			try {
				if (args[0].equals("set")) {
					if (cursors.isEmpty()) {
						System.out.print("set start ");
						startTime = System.currentTimeMillis();
					}

					Integer id = (Integer) args[1];
					Float x = (Float) args[2];
					Float y = (Float) args[3];
					Float vx = (Float) args[4];
					Float vy = (Float) args[5];
					Float accel = (Float) args[6];
					Cursor previous = cursors.get(id);
					cursors.put(id, new Cursor(id, x, y, vx, vy, accel, previous));
					System.out.print(x + " " + y + " ");
					detectGesture();
				} else if (args[0].equals("alive")) {
					// only detect gesture if something changes (since "alive"
					// is sent frequently)
					alive.clear();
					for (int i = 1; i < args.length; ++i)
						alive.add((Integer) args[i]);
					if (alive.isEmpty()) {
						if (!cursors.isEmpty()) {
							cursors.clear();
							detectGesture();
						}
					} else {
						if (cursors.keySet().retainAll(alive)) {
							detectGesture();
						}
					}
				}
			} catch (Exception ignored) {
			}
			return null;
		}
	}

	private IView view;

	private OSCServer server;

	private Map<Integer, Cursor> cursors = new HashMap<>();

	private long startTime;

	private BoundingBox bounds = new BoundingBox();

	public TUIO(IController controller, int port) throws IOException {
		for (IView view : controller.getViews()) {
			if (view.getConfig().getViewType() == IView.ViewType.INTERACTIVE_VIEW) {
				this.view = view;
				break;
			}
		}
		if (this.view == null) {
			throw new IllegalArgumentException("interactive view required for multitouch");
		}

		server = new OSCServer(port);
		server.installHandler("/", new Handler());

		// get screen information

		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice device : g.getScreenDevices()) {
			Rectangle rect = device.getDefaultConfiguration().getBounds();
			bounds.add(rect.getMinX(), rect.getMinY(), 0);
			bounds.add(rect.getMaxX(), rect.getMaxY(), 0);
		}
	}

	public TUIO(IController controller) throws IOException {
		this(controller, DEFAULT_PORT);
	}

	private void detectGesture() {
		// ignore everything that happens before a certain time
		if (System.currentTimeMillis() < startTime + WAIT_TIME_MS) {
			System.out.println("wait");
			return;
		}

		// handle gestures
		switch (cursors.size()) {
		case 0:
			// return to idle state if no touches
			System.out.println("idle");
			break;
		case 1:
			// ignore single finger touches (handled by mouse driver)
			System.out.println("ignore");
			break;
		case 2:
		// calculate swipe or pinch for 2 touches
		{
			Iterator<Cursor> i = cursors.values().iterator();
			Cursor c0 = i.next();
			Cursor c1 = i.next();
			System.out.println("swipe/pinch 2");
			if (c0.previous == null || c1.previous == null) {
				// do an extra round until all cursors have a previous position
				System.out.println("wait");
				return;
			}
			float swipeX = (c0.x - c0.previous.x + c1.x - c1.previous.x) / 2;
			float swipeY = (c0.y - c0.previous.y + c1.y - c1.previous.y) / 2;
			float pinch = (float) Math.sqrt((c1.x - c0.x) * (c1.x - c0.x) + (c1.y - c0.y) * (c1.y - c0.y));
			pinch -= (float) Math.sqrt((c1.previous.x - c0.previous.x) * (c1.previous.x - c0.previous.x) + (c1.previous.y - c0.previous.y)
					* (c1.previous.y - c0.previous.y));
			System.out.println("swipe " + cursors.size() + ": " + swipeX + " " + swipeY + " pinch 2: " + pinch);
			handleSwipeOrPinch2(swipeX, swipeY, pinch);
		}
			break;
		default:
		// calculate swipe for > 2 touches
		{
			float swipeX = 0;
			float swipeY = 0;
			for (Cursor c : cursors.values()) {
				if (c.previous == null) {
					// do an extra round until all cursors have a previous position
					System.out.println("wait");
					return;
				}
				swipeX += c.x - c.previous.x;
				swipeY += c.y - c.previous.y;
			}
			swipeX /= cursors.size();
			swipeY /= cursors.size();
			System.out.println("swipe " + cursors.size() + ": " + swipeX + " " + swipeY);
			handleSwipe3(swipeX, swipeY);
		}
		}
	}

	private void handleSwipeOrPinch2(float swipeX, float swipeY, float pinch) {
		DefaultCameraControl control = new DefaultCameraControl(view.getController().getCamera(view));
		// do we really need to discriminate between swipe and pinch, or just let both go at once?
		if (Math.abs(pinch) > 0.001) {
			control.addToDistance(-SCALE_DISTANCE * pinch);
		} else {
			control.addToAzimuth(SCALE_ROTATE * swipeX);
			control.addToElevation(SCALE_ROTATE * swipeY);
		}
	}

	private void handleSwipe3(float swipeX, float swipeY) {
		DefaultCameraControl control = new DefaultCameraControl(view.getController().getCamera(view));
		control.track(SCALE_TRANSLATE * swipeX, -SCALE_TRANSLATE * swipeY);
	}
}
