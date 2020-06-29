// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.prefuse.popups;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

// TODO: Auto-generated Javadoc
/**
 * The Class PopupUtils.
 */
public class PopupUtils {

	/**
	 * Adds the border and text to container.
	 *
	 * @param container the container
	 * @param border the border
	 * @param text the text
	 */
	public static void addBorderAndTextToContainer(Container container, Border border, String text) {
		JPanel component = new JPanel(new GridLayout(1, 1), false);

		JTextArea textArea = new JTextArea();
		Highlighter hilit = new DefaultHighlighter();
		textArea.setText(text);
		textArea.setHighlighter(hilit);
		textArea.setColumns(40);
		textArea.setLineWrap(true);
		textArea.setRows(10);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane jScrollPane1 = new JScrollPane(textArea);

		component.add(jScrollPane1);
		component.setBorder(border);
		container.add(Box.createRigidArea(new Dimension(0, 10)));
		container.add(component);
	}
}
