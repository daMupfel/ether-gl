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
package ch.ethz.ether.view;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import ch.ethz.ether.gl.Frame;
import ch.ethz.ether.scene.IScene;

/**
 * Abstract view class that implements some basic functionality. Use as base for
 * implementations.
 *
 * @author radar
 */
public abstract class AbstractView implements IView {
    private final Frame frame;
    private final IScene scene;

    private final ViewType viewType;

    private final Camera camera = new Camera(this);

    private final int[] viewport = new int[4];

    private boolean enabled = true;

    protected AbstractView(IScene scene, int x, int y, int w, int h, ViewType viewType, String title) {
        this.frame = new Frame(w, h, title);
        this.scene = scene;
        this.viewType = viewType;
        frame.setView(this);
        Point p = frame.getJFrame().getLocation();
        if (x != -1)
            p.x = x;
        if (y != -1)
            p.y = y;
        frame.getJFrame().setLocation(p);
    }

    @Override
    public final IScene getScene() {
        return scene;
    }

    @Override
    public final Camera getCamera() {
        return camera;
    }

    @Override
    public final int getWidth() {
        return viewport[2];
    }

    @Override
    public final int getHeight() {
        return viewport[3];
    }

    @Override
    public final int[] getViewport() {
        return viewport;
    }

    @Override
    public final ViewType getViewType() {
        return viewType;
    }

    @Override
    public final Frame getFrame() {
        return frame;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public final boolean isCurrent() {
        return getScene().getCurrentView() == this;
    }

    @Override
    public final void update() {
        getScene().getCurrentTool().viewChanged(this);
    }

    @Override
    public final void repaint() {
        frame.repaint();
    }


    // GLEventListener implementation

    @Override
    public final void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);

        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public final void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

        if (!isEnabled())
            return;

        // fetch viewport
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

        // repaint UI surface if necessary
        getScene().getUI().update();

        // render everything
        try {
            getScene().getRenderer().render(gl.getGL3(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();

        if (height == 0)
            height = 1; // prevent divide by zero
        viewport[2] = width;
        viewport[3] = height;
        gl.glViewport(0, 0, width, height);
        camera.update();
    }

    @Override
    public final void dispose(GLAutoDrawable drawable) {
        // TODO
    }

    // key listener

    @Override
    public void keyPressed(KeyEvent e) {
        scene.keyPressed(e, this);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        scene.keyReleased(e, this);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        scene.keyTyped(e, this);
    }

    // mouse listener

    @Override
    public void mouseEntered(MouseEvent e) {
        scene.mouseEntered(e, this);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        scene.mouseExited(e, this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        frame.requestFocus();
        scene.mousePressed(e, this);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        scene.mouseReleased(e, this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        scene.mouseClicked(e, this);
    }

    // mouse motion listener

    @Override
    public void mouseMoved(MouseEvent e) {
        scene.mouseMoved(e, this);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        scene.mouseDragged(e, this);
    }

    // mouse wheel listener

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scene.mouseWheelMoved(e, this);
    }
}
