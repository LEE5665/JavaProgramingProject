package gui.main.panel;

import javax.swing.*;
import java.awt.*;
import api.model.Todo;
import api.model.TodoDAO;

public class TodoItemPanel extends JPanel {
    private Todo todo;
    private JCheckBox checkBox;

    public TodoItemPanel(Todo todo, Runnable onToggle, Runnable onEdit, Runnable onDelete) {
        this.todo = todo;
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);

        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(todo.isCompleted());
        checkBox.addActionListener(e -> {
            todo.setCompleted(checkBox.isSelected());
            if (onToggle != null) onToggle.run();
        });

        JLabel titleLabel = new JLabel(todo.getTitle());
        titleLabel.setFont(new Font("Dialog", todo.isCompleted() ? Font.PLAIN : Font.BOLD, 15));

        // 편집, 삭제 버튼
        JButton editBtn = new JButton("✎");
        editBtn.setMargin(new Insets(0, 4, 0, 4));
        editBtn.setFont(new Font("Dialog", Font.PLAIN, 13));
        editBtn.addActionListener(e -> {
            if (onEdit != null) onEdit.run();
        });

        JButton deleteBtn = new JButton("🗑");
        deleteBtn.setMargin(new Insets(0, 4, 0, 4));
        deleteBtn.setFont(new Font("Dialog", Font.PLAIN, 13));
        deleteBtn.addActionListener(e -> {
            if (onDelete != null) onDelete.run();
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(checkBox, BorderLayout.WEST);
        left.add(titleLabel, BorderLayout.CENTER);

        add(left, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.EAST);
    }
}
