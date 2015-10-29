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

package ch.fhnw.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;

public class BufferUtilities {
	public static final FloatBuffer EMPTY_FLOAT_BUFFER = Buffers.newDirectFloatBuffer(0);

	public static ByteBuffer createDirectByteBuffer(int size) {
		ByteBuffer result = ByteBuffer.allocateDirect(size);
		result.order(ByteOrder.nativeOrder());
		return result;
	}

	public static IntBuffer createDirectIntBuffer(int size) {
		return createDirectByteBuffer(4 * size).asIntBuffer();
	}

	public static FloatBuffer createDirectFloatBuffer(int size) {
		return createDirectByteBuffer(4 * size).asFloatBuffer();
	}

	public static void arraycopy(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int length) {
		if (src == dst) {
			src.clear();
			byte[] tmp = new byte[length];
			src.position(srcPos);
			src.get(tmp, 0, length);
			dst.position(dstPos);
			dst.put(tmp, 0, length);
		} else {
			src.position(srcPos);
			src.limit(srcPos + length);
			dst.position(dstPos);
			dst.put(src);
			src.limit(src.capacity());
		}
	}

	public static void fill(ByteBuffer buffer, int off, int len, byte val) {
		buffer.position(off);
		while (len-- >= 0)
			buffer.put(val);
	}

	public static byte[] toByteArray(ByteBuffer buffer) {
		return toByteArray(buffer, 0, buffer.capacity());
	}

	public static byte[] toByteArray(ByteBuffer buffer, int off, int len) {
		if (buffer.hasArray())
			if (off == 0 && len == buffer.capacity())
				return buffer.array();

		byte[] result = new byte[len];
		buffer.clear();
		buffer.position(off);
		buffer.get(result);
		return result;
	}
}
