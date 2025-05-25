package gui.main;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;

<<<<<<< HEAD
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
=======
public class MemoEditorFrame extends JFrame {
    public MemoEditorFrame(String initialText, Consumer<String> onSave) {
        setTitle("메모 입력");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
>>>>>>> fa4b591b9e0f29c240e2e2884b011e27c126e6f9

import com.formdev.flatlaf.FlatClientProperties;

<<<<<<< HEAD
import net.miginfocom.swing.MigLayout;
=======
        JLabel titleLabel = new JLabel("메모 내용 입력");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel);
>>>>>>> fa4b591b9e0f29c240e2e2884b011e27c126e6f9

public class MemoEditorFrame extends JDialog {
	public MemoEditorFrame(java.awt.Window owner, String initialText, Consumer<String> onSave) {
		super(owner, "메모 입력", ModalityType.APPLICATION_MODAL);
		setSize(500, 400);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

<<<<<<< HEAD
		JPanel panel = new JPanel(new MigLayout("fill, insets 20, wrap 1", "[grow,fill]"));
		panel.putClientProperty(FlatClientProperties.STYLE, "arc:20");
=======
        JButton saveButton = new JButton("저장");
        saveButton.addActionListener(e -> {
            String content = textArea.getText().trim();
            if (!content.isEmpty()) {
                onSave.accept(content);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "내용을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            }
        });
>>>>>>> fa4b591b9e0f29c240e2e2884b011e27c126e6f9

		JLabel titleLabel = new JLabel("메모 내용 입력");
		titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
		panel.add(titleLabel);

		JTextArea textArea = new JTextArea(initialText);
		textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(400, 250));
		panel.add(scrollPane, "grow, h 250!");

		JButton saveButton = new JButton("저장");
		saveButton.addActionListener(e -> {
			String content = textArea.getText().trim();
			if (!content.isEmpty()) {
				onSave.accept(content);
				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "내용을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setOpaque(false);
		buttonPanel.add(saveButton);

		panel.add(buttonPanel, "dock south");
		setContentPane(panel);
	}
}
