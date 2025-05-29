package gui.main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MemoEditorFrame extends JDialog {
    private JTextArea textArea;

    public MemoEditorFrame(Window owner, String title, String initialContent, java.util.function.Consumer<String> onSave) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(0, 16));
        setSize(380, 230);
        setResizable(false);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UIManager.getColor("Panel.background"));

        textArea = new JTextArea(initialContent == null ? "" : initialContent, 7, 26);
        Font base = UIManager.getFont("ToggleButton.font");
        textArea.setFont(base.deriveFont( Font.PLAIN, 15));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textArea.setBackground(UIManager.getColor("TextField.background"));
        textArea.setForeground(UIManager.getColor("Label.foreground"));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 6));
        btnPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton btnSave = new JButton("저장");
        btnSave.setFont(base.deriveFont( Font.BOLD, 13));
        btnSave.setPreferredSize(new Dimension(80, 32));
        btnSave.addActionListener(e -> doSave(onSave));

        JButton btnCancel = new JButton("취소");
        btnCancel.setFont(base.deriveFont( Font.PLAIN, 13));
        btnCancel.setPreferredSize(new Dimension(80, 32));
        btnCancel.addActionListener(e -> {
            dispose();
        });

        textArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (e.isControlDown() || e.isMetaDown())) {
                    doSave(onSave);
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                    onSave.accept(null);
                }
            }
        });

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        JPanel padPanel = new JPanel(new BorderLayout());
        padPanel.setBackground(getBackground());
        padPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 18));
        padPanel.add(new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        add(padPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                textArea.requestFocus();
            }
        });
    }

    private void doSave(java.util.function.Consumer<String> onSave) {
        String content = textArea.getText();
        if (content == null) content = "";
        content = content.trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "내용을 입력하세요!", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        dispose();
        onSave.accept(content);
    }
}
