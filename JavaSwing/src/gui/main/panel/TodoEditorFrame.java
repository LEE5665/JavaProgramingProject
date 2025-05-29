package gui.main.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;

public class TodoEditorFrame extends JDialog {
    private JTextField titleField;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;

    public TodoEditorFrame(Window owner, String title,
                          String initialTitle,
                          LocalDate startDate, LocalDate endDate,
                          TriConsumer<String, LocalDate, LocalDate> onSave) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setSize(420, 300); 
        setResizable(false);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 16));
        getContentPane().setBackground(UIManager.getColor("Panel.background"));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24)); 

        
        Font base = UIManager.getFont("ToggleButton.font");
        form.add(new JLabel("제목"));
        titleField = new JTextField(initialTitle == null ? "" : initialTitle, 28);
        titleField.setFont(base.deriveFont(Font.BOLD, 15));
        form.add(titleField);
        form.add(Box.createVerticalStrut(12)); 

        
        form.add(new JLabel("시작일"));
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setValue(java.sql.Date.valueOf(startDate != null ? startDate : LocalDate.now()));
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        startDateSpinner.setPreferredSize(new Dimension(150, 25));
        form.add(startDateSpinner);
        form.add(Box.createVerticalStrut(12));

        
        form.add(new JLabel("마감일"));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setValue(java.sql.Date.valueOf(endDate != null ? endDate : LocalDate.now()));
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endEditor);
        endDateSpinner.setPreferredSize(new Dimension(150, 25));
        form.add(endDateSpinner);
        form.add(Box.createVerticalStrut(12));

        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton saveBtn = new JButton("저장");
        saveBtn.setFont(base.deriveFont(Font.PLAIN, 13));
        saveBtn.addActionListener(e -> doSave(onSave));

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setFont(base.deriveFont(Font.PLAIN, 13));
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        
        titleField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (e.isControlDown() || e.isMetaDown()))
                    doSave(onSave);
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    dispose();
            }
        });
    }

    private void doSave(TriConsumer<String, LocalDate, LocalDate> onSave) {
        String title = titleField.getText().trim();

        java.util.Date startUtilDate = (java.util.Date) startDateSpinner.getValue();
        java.util.Date endUtilDate = (java.util.Date) endDateSpinner.getValue();
        LocalDate startDate = startUtilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endUtilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        if (onSave != null && !title.isBlank())
            onSave.accept(title, startDate, endDate);
        dispose();
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}
