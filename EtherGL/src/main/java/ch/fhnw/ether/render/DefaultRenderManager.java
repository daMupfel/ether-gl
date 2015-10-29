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

package ch.fhnw.ether.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLRunnable;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.render.forward.ForwardRenderer;
import ch.fhnw.ether.render.variable.builtin.LightUniformBlock;
import ch.fhnw.ether.scene.camera.Camera;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.camera.IViewCameraState;
import ch.fhnw.ether.scene.light.DirectionalLight;
import ch.fhnw.ether.scene.light.GenericLight;
import ch.fhnw.ether.scene.light.ILight;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.ui.UI;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.IView.ViewType;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

/*
SCENE SIDE
- map: views (camera, view-camera state)
- map: materials (shader, list of meshes)
- map: meshes (renderable)
- list: lights


RENDER SIDE
- list: views + view-camera state
- list: renderables
- list: lights (copy)
*/

/**
 * Default render manager.
 *
 * @author radar
 */
public class DefaultRenderManager implements IRenderManager {
	private static class SceneViewState {
		ICamera camera = new Camera();
		IViewCameraState viewCameraState;

		SceneViewState(IView view) {
			viewCameraState = new ViewCameraState(view, camera);
		}
	}

	private static final GenericLight DEFAULT_LIGHT = new DirectionalLight(Vec3.Z, RGB.BLACK, RGB.WHITE);

	private final IController controller;
	private final IRenderer renderer = new ForwardRenderer();
	private final IRenderProgram program = new DefaultRenderProgram();

	private final Map<IView, SceneViewState> sceneViews = new IdentityHashMap<>();
	private final Map<IMesh, Renderable> sceneRenderables = new IdentityHashMap<>();
	private final List<GenericLight> sceneLights = new ArrayList<>(Collections.singletonList(DEFAULT_LIGHT));

	public DefaultRenderManager(IController controller) {
		this.controller = controller;
	}

	@Override
	public void addView(IView view) {
		SceneViewState vcs = new SceneViewState(view);
		sceneViews.put(view, vcs);
		setCamera(view, vcs.camera);
	}

	@Override
	public void removeView(IView view) {
		sceneViews.remove(view);
		setCamera(view, null);
	}

	@Override
	public void addMesh(IMesh mesh) {
		Renderable renderable = new Renderable(mesh, program.getProviders());
		if (sceneRenderables.putIfAbsent(mesh, renderable) != null)
			throw new IllegalArgumentException("mesh already in renderer: " + mesh);
	}

	@Override
	public void removeMesh(IMesh mesh) {
		Renderable renderable = sceneRenderables.remove(mesh);
		if (renderable == null)
			throw new IllegalArgumentException("mesh not in renderer: " + mesh);
	}

	@Override
	public void addLight(ILight light) {
		if (!(light instanceof GenericLight)) {
			throw new IllegalArgumentException("can only handle GenericLight");
		}
		if (sceneLights.contains(light)) {
			throw new IllegalArgumentException("light already in renderer: " + light);
		}
		if (sceneLights.size() == LightUniformBlock.MAX_LIGHTS) {
			throw new IllegalStateException("too many lights in renderer: " + LightUniformBlock.MAX_LIGHTS);
		}
		if (sceneLights.get(0) == DEFAULT_LIGHT)
			sceneLights.remove(0);
		sceneLights.add((GenericLight) light);
	}

	@Override
	public void removeLight(ILight light) {
		if (!sceneLights.contains(light)) {
			throw new IllegalArgumentException("light not in renderer: " + light);
		}
		sceneLights.remove(light);
		if (sceneLights.isEmpty())
			sceneLights.add(DEFAULT_LIGHT);
	}

	@Override
	public ICamera getCamera(IView view) {
		return sceneViews.get(view).camera;
	}

	@Override
	public void setCamera(IView view, ICamera camera) {
		camera.updateRequest();
		sceneViews.get(view).camera = camera;
	}

	@Override
	public void lockCamera(IView view, Mat4 viewMatrix, Mat4 projMatrix) {
		sceneViews.get(view).viewCameraState = new ViewCameraState(view, viewMatrix, projMatrix);
	}

	@Override
	public IViewCameraState getViewCameraState(IView view) {
		return sceneViews.get(view).viewCameraState;
	}

	@Override
	public Runnable getRenderRunnable() {
		try {
			// 1. set new view matrices for each updated camera
			sceneViews.forEach((view, svs) -> {
				if (svs.camera.updateTest())
					svs.viewCameraState = new ViewCameraState(view, svs.camera);
			});
			sceneViews.forEach((view, svs) -> {
				svs.camera.updateClear();
			});

			// 2. create runnable
			return new RenderRunnable(controller, sceneViews, sceneRenderables, sceneLights, renderer, program);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This runnable is created on scene thread and then run on render thread.
	 */
	private static class RenderRunnable implements Runnable {
		static class RenderViewState {
			final IView view;
			final IViewCameraState viewCameraState;

			RenderViewState(IView view, IViewCameraState vcs) {
				this.view = view;
				this.viewCameraState = vcs;
			}
		}

		final IController controller;
		final IRenderer renderer;
		final IRenderProgram program;
		final List<RenderViewState> renderViews = new ArrayList<>();
		final List<GenericLight> renderLights = new ArrayList<>();
		final List<Renderable> renderRenderables = new ArrayList<>();

		RenderRunnable(IController controller, Map<IView, SceneViewState> views, Map<IMesh, Renderable> renderables, List<GenericLight> lights,
				IRenderer renderer, IRenderProgram program) {
			this.controller = controller;
			this.renderer = renderer;
			this.program = program;
			views.forEach((view, svs) -> {
				renderViews.add(new RenderViewState(view, svs.viewCameraState));
			});
			renderLights.addAll(lights);
			renderRenderables.addAll(renderables.values());
		}

		@Override
		public void run() {
			for (RenderViewState viewState : renderViews) {
				viewState.view.getWindow().display(new GLRunnable() {
					@Override
					public boolean run(GLAutoDrawable drawable) {
						try {
							render(drawable.getGL().getGL3(), viewState);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return true;
					}
				});
			}
		}

		void render(GL3 gl, RenderViewState viewState) {
			try {
				// XXX: make sure we only render on render thread (e.g. jogl
				// will do repaints on other threads when resizing windows...)
				if (!controller.getScheduler().isRenderThread()) {
					return;
				}

				GL3 gl3 = gl.getGL3();
				// gl3 = new TraceGL3(gl3, System.out);
				// gl3 = new DebugGL3(gl3);

				// FIXME: currently we clear in DefaultView.display() ... needs to move
				//gl3.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

				gl3.glEnable(GL.GL_MULTISAMPLE);

				if (!viewState.view.isEnabled())
					return;

				// repaint UI surface to texture if necessary
				// FIXME: should this be done on model or render thread?
				UI ui = controller.getUI();
				if (ui != null)
					ui.update();

				// render everything
				ViewType type = viewState.view.getConfig().getViewType();
				program.getViewInfo().update(gl, viewState.viewCameraState, type);
				program.getLightInfo().update(gl, viewState.viewCameraState, renderLights);
				program.setRenderables(renderRenderables);
				renderRenderables.forEach((r) -> r.update(gl));
				renderer.render(gl, program);

				int error = gl.glGetError();
				if (error != 0)
					System.err.println("renderer returned with exisiting GL error 0x" + Integer.toHexString(error));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
