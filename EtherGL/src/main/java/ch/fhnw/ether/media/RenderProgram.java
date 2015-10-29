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

package ch.fhnw.ether.media;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ch.fhnw.util.ArrayUtilities;
import ch.fhnw.util.CollectionUtilities;
import ch.fhnw.util.IDisposable;
import ch.fhnw.util.IdentityHashSet;
import ch.fhnw.util.Log;

public class RenderProgram<T extends IRenderTarget> extends AbstractRenderCommand<T,RenderProgram.State<T>> {
	private static Log log = Log.create();

	private List<IRenderTarget> targets = new LinkedList<>();

	static class Update {
		final AbstractRenderCommand<?,?> oldCmd;
		final AbstractRenderCommand<?,?> newCmd;
		final boolean                    first;

		Update(AbstractRenderCommand<?,?> oldCmd, AbstractRenderCommand<?,?> newCmd, boolean first) {
			this.oldCmd = oldCmd;
			this.newCmd = newCmd;
			this.first  = first;
		}

		boolean isAdd() {
			return newCmd != null && oldCmd == null;
		}

		boolean isReplace() {
			return newCmd != null && oldCmd != null;
		}

		boolean isRemove() {
			return newCmd == null && oldCmd != null;
		}

		void add(List<AbstractRenderCommand<?,?>> program) {
			if(first)
				program.add(0, newCmd);
			else
				program.add(newCmd);
		}

		void replace(List<AbstractRenderCommand<?,?>> program) {
			program.set(program.indexOf(oldCmd), newCmd);
		}

		void remove(List<AbstractRenderCommand<?,?>> program) {
			program.remove(oldCmd);
		}
	}

	private final AtomicReference<AbstractRenderCommand<T,?>[]> program   = new AtomicReference<>();
	private final List<IRenderProgramListener>                  listeners = new ArrayList<>();

	@SafeVarargs
	public RenderProgram(AbstractFrameSource<T,?> source, AbstractRenderCommand<T,?> ... commands) {
		program.set(ArrayUtilities.prepend(source, commands));
	}

	public Update createAddFirst(AbstractRenderCommand<T,?> cmd) {
		return new Update(null, cmd, true);
	}

	public Update createAddLast(AbstractRenderCommand<T,?> cmd) {
		return new Update(null, cmd, false);
	}

	public Update createRemove(AbstractRenderCommand<T,?> cmd) {
		return new Update(cmd, null, false);
	}

	public Update createReplace(AbstractRenderCommand<T,?> oldCmd, AbstractRenderCommand<T,?> newCmd) {
		return new Update(oldCmd, newCmd, false);
	}

	public void addFirst(AbstractRenderCommand<T,?> cmd) {
		update(createAddFirst(cmd));
	}

	public void addLast(AbstractRenderCommand<T,?> cmd) {
		update(createAddLast(cmd));
	}

	public void remove(AbstractRenderCommand<T,?> cmd) {
		update(createRemove(cmd));
	}

	public void replace(AbstractFrameSource<T, ?> source) {
		update(createReplace(program.get()[0], source)) ; 
	}

	public void replace(AbstractRenderCommand<T,?> oldCmd, AbstractRenderCommand<T,?> newCmd) {
		update(createReplace(oldCmd, newCmd)) ; 
	}

	public void update(Update ... updates) {
		List<AbstractRenderCommand<?,?>> tmp = new ArrayList<>(program.get().length + updates.length);
		CollectionUtilities.addAll(tmp, program.get());

		for(Update update : updates) {
			if(update.isAdd())
				update.add(tmp);
			if(update.isReplace())
				update.replace(tmp);
			if(update.isRemove())
				update.remove(tmp);
		}

		setProgram(tmp);
	}

	@SuppressWarnings("unchecked")
	private synchronized void setProgram(List<AbstractRenderCommand<?,?>> program) {
		AbstractRenderCommand<T,?>[] oldProgram = this.program.get(); 
		AbstractRenderCommand<T,?>[] newProgram = program.toArray(new AbstractRenderCommand[program.size()]);

		final IdentityHashSet<AbstractRenderCommand<T,?>> removed = new IdentityHashSet<>(oldProgram);		
		removed.removeAll(new IdentityHashSet<>(newProgram));

		new Thread(()->{
			try {
				 // Give all targets some time to switch to the changed program
				// before disposing any commands. Very hacky...
				Thread.sleep(500);
			} catch (Throwable t) {}
			for(AbstractRenderCommand<T,?> cmd : removed) {
				IRenderTarget[] ts = null;
				synchronized (targets) {ts = targets.toArray(new IRenderTarget[targets.size()]);}
				for(IRenderTarget target : ts) {
					try {
						PerTargetState<?> state = target.getState(cmd);
						if(state instanceof IDisposable)
							((IDisposable)state).dispose();
					} catch(Throwable t) {
						log.warning(t);
					}
				}
			}
		}, "disposing:" + removed).start();

		for(IRenderProgramListener l : listeners)
			l.programChanged(this, oldProgram, newProgram);
		this.program.set(newProgram);
	}

	@Override
	protected void run(State<T> state) throws RenderCommandException {
		final T target = state.getTarget();
		AbstractRenderCommand<T,?>[] commands = program.get(); 
		for(AbstractRenderCommand<T,?> command : commands)
			command.runInternal(target);
	}

	@Override
	protected State<T> createState(T target) {
		return new State<>(target);
	}

	static class State<T extends IRenderTarget> extends PerTargetState<T> {
		public State(T target) {
			super(target);
		}
	}

	public AbstractRenderCommand<T, ?>[] getProgram() {
		return program.get();
	}

	public synchronized void addListener(IRenderProgramListener listener) {
		CollectionUtilities.addIfUnique(listeners, listener);
	}

	public synchronized void removeListener(IRenderProgramListener listener) {
		CollectionUtilities.removeAll(listeners, listener);
	}

	public AbstractFrameSource<T, ?> getFrameSource() {
		AbstractRenderCommand<T,?>[] commands = program.get(); 
		for(AbstractRenderCommand<T,?> command : commands)
			if(command instanceof AbstractFrameSource)
				return (AbstractFrameSource<T, ?>) command;
		return null;
	}

	public void addTarget(IRenderTarget target) {
		synchronized (targets) {
			targets.add(target);
		}
	}
}
