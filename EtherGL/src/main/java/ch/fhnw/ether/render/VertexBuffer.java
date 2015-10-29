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

import java.nio.FloatBuffer;
import java.util.List;

import ch.fhnw.ether.render.gl.FloatArrayBuffer;
import ch.fhnw.ether.render.gl.IArrayBuffer;
import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.variable.IShaderArray;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.IGeometryAttribute;
import ch.fhnw.util.BufferUtilities;
import ch.fhnw.util.math.Mat3;
import ch.fhnw.util.math.Mat4;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

// TODO: deal with max vbo size & multiple vbos, memory optimization, handle non-float arrays

public final class VertexBuffer implements IVertexBuffer {
	private static final ThreadLocal<FloatBuffer> TARGET = 
			ThreadLocal.withInitial(() -> BufferUtilities.createDirectFloatBuffer(1024 * 1024));

	private final FloatArrayBuffer buffer = new FloatArrayBuffer();

	private final int stride;
	private final int[] sizes;
	private final int[] offsets;
	private final int[] attributeIndices;

	public VertexBuffer(IShader shader, IMesh mesh) {
		List<IShaderArray<?>> arrays = shader.getArrays();

		sizes = new int[arrays.size()];
		offsets = new int[arrays.size()];
		attributeIndices = new int[arrays.size()];

		int[] stride = new int[1];
		mesh.getGeometry().inspect((attributes, data) -> {
			int bufferIndex = 0;
			for (IShaderArray<?> array : arrays) {
				int attributeIndex = 0;
				for (IGeometryAttribute attribute : attributes) {
					if (array.id().equals(attribute.id())) {
						int size = attribute.getNumComponents();
						sizes[bufferIndex] = size;
						offsets[bufferIndex] = stride[0];
						attributeIndices[bufferIndex] = attributeIndex;
						array.setBufferIndex(bufferIndex);
						bufferIndex++;
						stride[0] += size;
						break;
					}
					attributeIndex++;
				}
				if (attributeIndex == attributes.length)
					throw new IllegalArgumentException("shader " + shader + " requires attribute " + array.id());
			}
		});
		this.stride = stride[0];
	}

	@Override
	public int getNumVertices() {
		return buffer.size() / stride;
	}

	@Override
	public void load(GL3 gl, IShader shader, IMesh mesh) {
		Mat4 modelMatrix = Mat4.multiply(Mat4.translate(mesh.getPosition()), mesh.getTransform());
		Mat3 normalMatrix = new Mat3(modelMatrix).inverse().transpose();

		List<IShaderArray<?>> arrays = shader.getArrays();
		float[][] sources = new float[arrays.size()][];
		mesh.getGeometry().inspect((attributes, data) -> {
			int bufferIndex = 0;
			int size = 0;
			for (IShaderArray<?> array : arrays) {
				float[] source = data[attributeIndices[bufferIndex]];
				if (array.id().equals(IGeometry.POSITION_ARRAY.id()))
					sources[bufferIndex] = modelMatrix.transform(source);
				else if (array.id().equals(IGeometry.NORMAL_ARRAY.id()))
					sources[bufferIndex] = normalMatrix.transform(source);
				else
					sources[bufferIndex] = source;
				bufferIndex++;
				size += source.length;
			}
			FloatBuffer buffer = TARGET.get();
			if (buffer.capacity() < size) {
				buffer = BufferUtilities.createDirectFloatBuffer(2 * size);
				TARGET.set(buffer);
			}
			buffer.clear();
			buffer.limit(size);
			interleave(buffer, sources, sizes);
		});
		buffer.load(gl, TARGET.get());
	}

	@Override
	public void bind(GL3 gl) {
		buffer.bind(gl);
	}

	@Override
	public void unbind(GL3 gl) {
		IArrayBuffer.unbind(gl);
	}

	@Override
	public void enableAttribute(GL3 gl, int bufferIndex, int shaderIndex) {
		if (!buffer.isEmpty()) {
			gl.glEnableVertexAttribArray(shaderIndex);
			gl.glVertexAttribPointer(shaderIndex, sizes[bufferIndex], GL.GL_FLOAT, false, stride * 4,
					offsets[bufferIndex] * 4);
		}
	}

	@Override
	public void disableAttribute(GL3 gl, int bufferIndex, int shaderIndex) {
		if (!buffer.isEmpty()) {
			gl.glDisableVertexAttribArray(shaderIndex);
		}
	}

	@Override
	public String toString() {
		return buffer.size() + " " + stride;
	}

	private static void interleave(FloatBuffer target, float[][] data, int[] sizes) {
		for (int i = 0; i < data[0].length / sizes[0]; ++i) {
			for (int j = 0; j < data.length; ++j) {
				int k = (i * sizes[j]) % data[j].length;
				target.put(data[j], k, sizes[j]);
			}
		}
	}
}
