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

package ch.fhnw.ether.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.fhnw.ether.media.AbstractRenderCommand;
import ch.fhnw.ether.media.Parameter;
import ch.fhnw.ether.media.RenderProgram;

public class ParameterWindow {
	public enum Flag {EXIT_ON_CLOSE}

	static final float S         = 1000f;
	static final int   NUM_TICKS = 5;

	private static NumberFormat FMT = new DecimalFormat("#.##");

	AtomicReference<JFrame> frame = new AtomicReference<>();

	static class ParamUI implements ChangeListener, ActionListener {
		JLabel                     label;
		JSlider                    slider;
		JComboBox<String>          combo;
		AbstractRenderCommand<?,?> cmd;
		Parameter                  p;
		float                      def;
		Timer                      t;

		ParamUI(AbstractRenderCommand<?,?> cmd, Parameter param) {
			Hashtable<Integer, JLabel> labels = new Hashtable<>();
			float d = param.getMax() - param.getMin();
			for(int i = 0; i < NUM_TICKS; i++) {
				float val = param.getMin() + ((i * d) / (NUM_TICKS-1));
				labels.put(Integer.valueOf((int)(val * S)), new JLabel(FMT.format(val))); 
			}
			this.cmd    = cmd;
			this.p      = param;
			this.def    = cmd.getVal(param);
			this.label  = new JLabel(param.getDescription());
			switch(p.getType()) {
			case RANGE:
				try {
					this.slider = new JSlider((int)(param.getMin() * S), (int)(param.getMax() * S), (int)(cmd.getVal(p) * S));
					this.slider.setPaintLabels(true);
					this.slider.setPaintTicks(true);
					this.slider.setLabelTable(labels);
					this.slider.addChangeListener(this);
					t = new Timer(40, this);
					t.start();
				} catch(Throwable t) {
					System.err.println(param);
					t.printStackTrace();
				}
				break;
			case ITEMS:
				this.combo = new JComboBox<>(param.getItems());
				this.combo.addActionListener(this);
				this.combo.setSelectedIndex((int)(cmd.getVal(p)));
				t = new Timer(40, this);
				t.start();
				break;
			}
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			cmd.setVal(p, slider.getValue() / S);
		}

		public void reset() {
			if(slider != null)
				slider.setValue((int)(def * S));
			if(combo != null)
				combo.setSelectedIndex((int) def);
		}

		public void zero() {
			if(slider != null)
				slider.setValue(0);
			if(combo != null)
				combo.setSelectedIndex(0);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == t) {
				if(this.slider != null) {
					float val =  slider.getValue() / S;
					if(cmd.getVal(p) != val) {
						slider.setValue((int) (cmd.getVal(p) * S));
						cmd.setVal(p, slider.getValue() / S);
					}
				}
				if(this.combo != null) {
					float val =  combo.getSelectedIndex();
					if(cmd.getVal(p) != val) {
						combo.setSelectedIndex((int) cmd.getVal(p));
						cmd.setVal(p, combo.getSelectedIndex());
					}
				}
			} else
				cmd.setVal(p, combo.getSelectedIndex());
		}
	}

	public ParameterWindow(final AbstractRenderCommand<?,?> src, Flag ... flags) {
		this(null, src, flags);
	}

	private boolean hasFlag(Flag flag, Flag[] flags) {
		for(Flag f : flags)
			if(f == flag)
				return true;
		return false;
	}

	public ParameterWindow(final JComponent addOn, final AbstractRenderCommand<?,?> src, Flag ... flags) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame f= new JFrame("Parameters");
				if(hasFlag(Flag.EXIT_ON_CLOSE, flags))
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setLayout(new BorderLayout());
				if(addOn != null)
					f.add(addOn, BorderLayout.NORTH);
				f.add(new JScrollPane(createUIRecr(src), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
				f.pack();
				f.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width - f.getSize().width, f.getLocation().y);
				f.setVisible(true);
				frame.set(f);
			}

			private void addMenuItem(JPopupMenu menu, JMenuItem item, ActionListener listener) {
				item.addActionListener(listener);
				menu.add(item);
			}

			JComponent createUI(AbstractRenderCommand<?,?> cmd) {
				if(cmd.getClass().getName().equals(cmd.toString()) && cmd.getParameters().length == 0) {
					JLabel result = new JLabel(cmd.toString());
					result.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
					return result;
				}

				JPopupMenu menu   = new JPopupMenu();
				JPanel     result = new JPanel();
				result.setComponentPopupMenu(menu);
				result.setBorder(new TitledBorder(cmd.getClass().getName()));
				Parameter[] params = cmd.getParameters();
				result.setLayout(new GridBagLayout());
				if(params.length > 0) {
					final ParamUI[] uis = new ParamUI[params.length];
					for(int i = 0; i < uis.length; i++) {
						uis[i] = new ParamUI(cmd, params[i]);
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridy = i;
						gbc.gridx = 0;
						result.add(uis[i].label, gbc);
						gbc = new GridBagConstraints();
						gbc.gridy = i;
						gbc.gridx = 1;
						if(uis[i].slider != null)
							result.add(uis[i].slider, gbc);
						if(uis[i].combo != null)
							result.add(uis[i].combo, gbc);
					}
					addMenuItem(menu, new JCheckBoxMenuItem("Enabled", cmd.isEnabled()), (ActionEvent e)->{
						cmd.setEnable(((JCheckBoxMenuItem)e.getSource()).isSelected());
						setEnablded(result, cmd.isEnabled());
					});
					addMenuItem(menu, new JMenuItem("Reset"), (ActionEvent e)->{for(ParamUI p : uis) p.reset();});
					addMenuItem(menu, new JMenuItem("Zero"), (ActionEvent e)->{for(ParamUI p : uis) p.zero();});
				} else {
					JTextArea text = new JTextArea(cmd.toString(), 1, 30);
					text.setWrapStyleWord(false);
					text.setEditable(false);
					text.setBackground(result.getBackground());
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.gridwidth = 2;
					result.add(text, new GridBagConstraints());
				}
				return result;
			}


			private void setEnablded(JComponent cmp, boolean state) {
				cmp.setEnabled(state);
				for(Component c : cmp.getComponents()) {
					if(c instanceof JComponent)
						setEnablded((JComponent)c, state);
				}
			}

			JPanel createUIRecr(AbstractRenderCommand<?,?> src) {
				Insets insets = new Insets(0, 0, 0, 0);
				JPanel result = new JPanel();
				result.setLayout(new GridBagLayout());
				JComponent cmp = createUI(src);
				int w = 1;
				if(src instanceof RenderProgram<?>) {
					RenderProgram<?> program = (RenderProgram<?>)src;
					program.addListener((RenderProgram<?> prog,
							AbstractRenderCommand<?, ?>[] oldProgram,
							AbstractRenderCommand<?, ?>[] newProgram)->{
								result.removeAll();
								int y = 0;
								for(AbstractRenderCommand<?, ?> cmd : newProgram)
									result.add(createUIRecr(cmd), new GridBagConstraints(1, y++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
								Component c = SwingUtilities.getRoot(result);
								if(c instanceof JFrame)
									((JFrame)c).pack();
								else
									c.validate();;
							});
					int y = 0;
					for(AbstractRenderCommand<?, ?> cmd : program.getProgram())
						result.add(createUIRecr(cmd), new GridBagConstraints(1, y++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
				} else {
					result.add(cmp, new GridBagConstraints(0, 0, w, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
				}
				return result;
			}
		});

	}

	public boolean isVisible() {
		while(frame.get() == null)
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		return frame.get().isVisible();
	}

	public void exitOnClose() {
		// TODO Auto-generated method stub

	}
}