package gui.main.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.function.Consumer;

public class TodoEditorFrame extends JDialog {
    private JTextField titleField;
    private JTextArea noteArea;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;

    public TodoEditorFrame(Window owner, String title,
                          String initialTitle, String initialNote,
                          LocalDate startDate, LocalDate endDate,
                          QuadConsumer<String, String, LocalDate, LocalDate> onSave) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setSize(420, 310);
        setResizable(false);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 16));
        getContentPane().setBackground(UIManager.getColor("Panel.background"));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));

        // 제목
        form.add(new JLabel("제목"));
        titleField = new JTextField(initialTitle == null ? "" : initialTitle, 28);
        titleField.setFont(new Font("Dialog", Font.BOLD, 15));
        form.add(titleField);
        form.add(Box.createVerticalStrut(8));

        // 시작일
        form.add(new JLabel("시작일"));
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setValue(java.sql.Date.valueOf(startDate != null ? startDate : LocalDate.now()));
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        form.add(startDateSpinner);
        form.add(Box.createVerticalStrut(8));

        // 마감일
        form.add(new JLabel("마감일"));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setValue(java.sql.Date.valueOf(endDate != null ? endDate : LocalDate.now()));
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endEditor);
        form.add(endDateSpinner);
        form.add(Box.createVerticalStrut(8));

        // 내용
        form.add(new JLabel("내용"));
        noteArea = new JTextArea(initialNote == null ? "" : initialNote, 5, 28);
        noteArea.setFont(new Font("Dialog", Font.PLAIN, 13));
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

        form.add(new JScrollPane(noteArea));

        // 버튼
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton saveBtn = new JButton("저장");
        saveBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        saveBtn.addActionListener(e -> doSave(onSave));

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setFont(new Font("Dialog", Font.PLAIN, 13));
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // 단축키
        noteArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (e.isControlDown() || e.isMetaDown())) doSave(onSave);
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
            }
        });
    }

    private void doSave(QuadConsumer<String, String, LocalDate, LocalDate> onSave) {
        String title = titleField.getText().trim();
        String note = noteArea.getText().trim();

        // ↓ 안전하게 변환
        java.util.Date startUtilDate = (java.util.Date) startDateSpinner.getValue();
        java.util.Date endUtilDate = (java.util.Date) endDateSpinner.getValue();
        LocalDate startDate = startUtilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endUtilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        if (onSave != null && !title.isBlank())
            onSave.accept(title, note, startDate, endDate);
        dispose();
    }

    @FunctionalInterface
    public interface QuadConsumer<A, B, C, D> {
        void accept(A a, B b, C c, D d);
    }
}
