/*
Copyright (c) 2013, ETH Zurich (Stefan Mueller Arisona, Eva Friedrich)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
 * Neither the name of ETH Zurich nor the names of its contributors may be 
  used to endorse or promote products derived from this software without
  specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ch.ethz.ether.gl;

import java.nio.Buffer;

import javax.media.opengl.GL;

import com.jogamp.common.nio.Buffers;

/**
 * Very simple texture wrapper.
 *
 * @author radar
 */
public class Texture {
    private int[] tex;

    public Texture() {
    }

    public void dispose(GL gl) {
        if (tex != null) {
            gl.glDeleteTextures(1, tex, 0);
            tex = null;
        }
    }

    public void load(GL gl, int width, int height, byte[] rgba) {
        load(gl, width, height, Buffers.newDirectByteBuffer(rgba), GL.GL_RGBA);
    }

    public void load(GL gl, int width, int height, Buffer buffer, int format) {
        if (tex == null) {
            tex = new int[1];
            gl.glGenTextures(1, tex, 0);
            gl.glBindTexture(GL.GL_TEXTURE_2D, tex[0]);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
        } else {
            gl.glBindTexture(GL.GL_TEXTURE_2D, tex[0]);
        }
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        buffer.rewind();
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, format, GL.GL_UNSIGNED_BYTE, buffer);
        gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    public void enable(GL gl) {
        if (tex != null)
            gl.glBindTexture(GL.GL_TEXTURE_2D, tex[0]);
    }

    public void disable(GL gl) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }
}
