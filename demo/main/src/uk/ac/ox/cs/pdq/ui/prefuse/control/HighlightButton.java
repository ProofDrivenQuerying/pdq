package uk.ac.ox.cs.pdq.ui.prefuse.control;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;

import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;

public class HighlightButton extends JCheckBox{

	public HighlightButton(String title, AndPredicate filter, Predicate predicate) {
		super(title, false);
		this.setAction(new PropertiesListener(title, filter, predicate));
	}

	private class PropertiesListener extends AbstractAction {
		private AndPredicate filter;
		private Predicate predicate;

		public PropertiesListener(String title, AndPredicate filter, Predicate predicate) {
			super(title);
			this.filter = filter;
			this.predicate = predicate;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();
			if (cb.isSelected()) {
				this.filter.clear();

				if(this.predicate != null) {
					this.filter.add(this.predicate);
				}
			}
			else {
				if(this.predicate != null) {
					this.filter.remove(this.predicate);
				}
			}
				
		}
	}
}
