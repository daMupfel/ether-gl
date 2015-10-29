package ch.fhnw.ether.media;

public class StateHandle<S extends PerTargetState<?>> {
	private final AbstractRenderCommand<?, S> cmd;
	
	public StateHandle(AbstractRenderCommand<?, S> cmd) {
		this.cmd = cmd;
	}
	
	@SuppressWarnings("unchecked")
	public S get(IRenderTarget target) throws RenderCommandException {
		return (S) target.getState(cmd);
	}
}
