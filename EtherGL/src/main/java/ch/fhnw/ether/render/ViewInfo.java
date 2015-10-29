package ch.fhnw.ether.render;

import com.jogamp.opengl.GL3;

import ch.fhnw.ether.render.gl.FloatUniformBuffer;
import ch.fhnw.ether.render.variable.builtin.ViewUniformBlock;
import ch.fhnw.ether.scene.attribute.IAttributeProvider;
import ch.fhnw.ether.scene.camera.IViewCameraState;
import ch.fhnw.ether.view.IView.ViewType;

public final class ViewInfo {
	private final FloatUniformBuffer uniforms = new FloatUniformBuffer(ViewUniformBlock.BLOCK_SIZE, 3);
	private IViewCameraState vcs;
	private ViewType type;

	public void update(GL3 gl, IViewCameraState vcs, ViewType type) {
		ViewUniformBlock.loadUniforms(gl, uniforms, vcs);
		this.vcs = vcs;
		this.type = type;
	}
	
	public IViewCameraState getViewCameraState() {
		return vcs;
	}
	
	public ViewType getType() {
		return type;
	}

	public void setCameraSpace(GL3 gl) {
		uniforms.bind(gl, 0);
	}

	public void setOrthoDeviceSpace(GL3 gl) {
		uniforms.bind(gl, 1);
	}

	public void setOrthoScreenSpace(GL3 gl) {
		uniforms.bind(gl, 2);
	}

	public IAttributeProvider getAttributeProvider() {
		return new IAttributeProvider() {
			@Override
			public void getAttributes(IAttributes attributes) {
				attributes.provide(ViewUniformBlock.ATTRIBUTE, () -> uniforms.getBindingPoint());
			}
		};
	}
}
