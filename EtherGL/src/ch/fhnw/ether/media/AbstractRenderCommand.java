package ch.fhnw.ether.media;

import ch.fhnw.util.ClassUtilities;
import ch.fhnw.util.IObjectID;
import ch.fhnw.util.Log;

public abstract class AbstractRenderCommand<T extends IRenderTarget, S extends PerTargetState<T>> implements IObjectID {
	private static final Log log = Log.create();

	private final long id = ClassUtilities.createObjectID();

	protected final Parameter[]    parameters;
	private   final StateHandle<S> stateHandle;
	
	protected AbstractRenderCommand(Parameter ... parameters) {
		this.parameters  = new Parameter[parameters.length];
		this.stateHandle = new StateHandle<>(this);
		for(int i = 0; i < parameters.length; i++) {
			parameters[i].setIdx(i);
			this.parameters[i] = parameters[i].copy();
		}
	}
	
	public Parameter[] getParameters() {
		return parameters;
	}

	public String getName(Parameter p) {
		return parameters[p.getIdx()].getName();
	}

	public String getDescription(Parameter p) {
		return parameters[p.getIdx()].getDescription();
	}

	public float getMin(Parameter p) {
		return parameters[p.getIdx()].getMin();
	}

	public float getMax(Parameter p) {
		return parameters[p.getIdx()].getMax();
	}

	public float getVal(Parameter p) {
		return parameters[p.getIdx()].getVal();
	}

	public void setVal(Parameter p, float val) {
		parameters[p.getIdx()].setVal(val);
	}

	protected abstract void run(S state) throws RenderCommandException;
	
	@SuppressWarnings({"unused","unchecked" })
	protected S createState(T target) throws RenderCommandException {
		return (S)new Stateless<>(target);
	}

	@SuppressWarnings({"unchecked" })
	protected S getState(T target) {
		try {
			return (S)target.getState(this);
		} catch (RenderCommandException e) {
			log.severe(e);
			return null;
		}
	}

	@Override
	public final long getObjectID() {
		return id;
	}
	
	@Override
	public final int hashCode() {
		return (int) id;
	}
	
	@Override
	public final boolean equals(Object obj) {
		return obj instanceof AbstractRenderCommand && ((AbstractRenderCommand<?,?>)obj).id == id;
	}
	
	@SuppressWarnings("unchecked")
	final void runInternal(IRenderTarget target) throws RenderCommandException {
		run((S)target.getState(this));
	}
	
	@SuppressWarnings("unchecked")
	final <TS extends PerTargetState<?>> TS createStateInternal(IRenderTarget target) throws RenderCommandException {
		return (TS)createState((T) target);
	}
	
	@Override
	public String toString() {
		return getClass().getName();
	}
	
	public StateHandle<S> state() {
		return stateHandle; 
	}
}