package gui.main;

import javax.swing.*;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.util.function.Consumer;

public class MemoEditorFrame extends JFrame {
    public MemoEditorFrame(String initialText, Consumer<String> onSave) {
        setTitle("메모 입력");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new MigLayout("fill, insets 20, wrap 1", "[grow,fill]"));
        panel.setBackground(new Color(30, 30, 30));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        JLabel titleLabel = new JLabel("메모 내용 입력");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
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
