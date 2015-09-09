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

public class PopupUtils {

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
