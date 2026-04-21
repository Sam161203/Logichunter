package ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Minimal UI tab retained after removal of proprietary analyzers.
 */
public class LogicHunterTab extends JPanel {

    public LogicHunterTab() {
        setLayout(new BorderLayout());

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        info.setText(
                "LogicHunter tab loaded.\n\n"
                + "High-risk proprietary analyzer components have been removed from this build."
        );

        add(new JScrollPane(info), BorderLayout.CENTER);
    }
}