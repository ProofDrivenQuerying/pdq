// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.control;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;

import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;

// TODO: Auto-generated Javadoc
/**
 * The Class HighlightButton.
 */
public class HighlightButton extends JCheckBox{

	/**
	 * Instantiates a new highlight button.
	 *
	 * @param title the title
	 * @param filter the filter
	 * @param predicate the predicate
	 */
	public HighlightButton(String title, AndPredicate filter, Predicate predicate) {
		super(title, false);
		this.setAction(new PropertiesListener(title, filter, predicate));
	}

	/**
	 * The listener interface for receiving properties events.
	 * The class that is interested in processing a properties
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addPropertiesListener<code> method. When
	 * the properties event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see PropertiesEvent
	 */
	private class PropertiesListener extends AbstractAction {
		
		/** The filter. */
		private AndPredicate filter;
		
		/** The predicate. */
		private Predicate predicate;

		/**
		 * Instantiates a new properties listener.
		 *
		 * @param title the title
		 * @param filter the filter
		 * @param predicate the predicate
		 */
		public PropertiesListener(String title, AndPredicate filter, Predicate predicate) {
			super(title);
			this.filter = filter;
			this.predicate = predicate;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
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
