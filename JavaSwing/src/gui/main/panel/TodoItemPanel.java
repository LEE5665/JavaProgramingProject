package gui.main.panel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import api.model.Todo;
import api.model.CheckItem;
import api.model.CheckItemDAO;
import api.model.TodoDAO; 

public class TodoItemPanel extends JPanel {
    private final Todo todo;
    private final Runnable onTodoChanged;

    private JPanel checkListPanel;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TodoItemPanel(Todo todo, Runnable onTodoChanged, Runnable onEdit, Runnable onDelete) {
        this.todo = todo;
        this.onTodoChanged = onTodoChanged;

        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel cardPanel = new JPanel(new BorderLayout(8, 8));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 200), 1, true),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(todo.getTitle());
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 15));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        String periodText = String.format("%s ~ %s",
        LocalDate.parse(todo.getStartDate()).format(DATE_FORMAT),
        	  LocalDate.parse(todo.getEndDate()).format(DATE_FORMAT));
        JLabel periodLabel = new JLabel(periodText);
        periodLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        periodLabel.setForeground(Color.GRAY);

        JPanel titlePeriodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePeriodPanel.setOpaque(false);
        titlePeriodPanel.add(titleLabel);
        titlePeriodPanel.add(periodLabel);

        topPanel.add(titlePeriodPanel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);

        JButton planEditBtn = new JButton("계획 수정");
        planEditBtn.setFont(new Font("Dialog", Font.PLAIN, 12));
        planEditBtn.setFocusable(false);
        planEditBtn.addActionListener(e -> onEdit.run());

        JButton planDeleteBtn = new JButton("계획 삭제");
        planDeleteBtn.setFont(new Font("Dialog", Font.PLAIN, 12));
        planDeleteBtn.setFocusable(false);
        planDeleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "이 계획을 삭제할까요?",
                "계획 삭제 확인",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                onDelete.run();
            }
        });

        buttonPanel.add(planEditBtn);
        buttonPanel.add(planDeleteBtn);

        topPanel.add(buttonPanel, BorderLayout.EAST);

        cardPanel.add(topPanel, BorderLayout.NORTH);

        checkListPanel = new JPanel();
        checkListPanel.setLayout(new BoxLayout(checkListPanel, BoxLayout.Y_AXIS));
        checkListPanel.setOpaque(false);
        cardPanel.add(checkListPanel, BorderLayout.CENTER);

        JButton addCheckBtn = new JButton("+ 체크박스 추가");
        JButton addTextBtn = new JButton("+ 텍스트 추가");
        addCheckBtn.setFont(new Font("Dialog", Font.PLAIN, 13));
        addTextBtn.setFont(new Font("Dialog", Font.PLAIN, 13));
        addCheckBtn.addActionListener(e -> onAddItem("checkbox"));
        addTextBtn.addActionListener(e -> onAddItem("text"));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(addCheckBtn);
        bottomPanel.add(addTextBtn);
        cardPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(cardPanel, BorderLayout.CENTER);

        reloadCheckList();
    }

    private void reloadCheckList() {
        checkListPanel.removeAll();
        List<CheckItem> items = CheckItemDAO.listByTodo(todo.getId());

        if (items == null || items.isEmpty()) {
            checkListPanel.add(new JLabel("항목이 없습니다."));
            checkListPanel.revalidate();
            checkListPanel.repaint();
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            CheckItem item = items.get(index);

            JPanel line = new JPanel();
            line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
            line.setOpaque(false);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);

            if ("checkbox".equals(item.getType())) {
                JCheckBox cb = new JCheckBox(item.getContent(), item.isChecked());
                cb.setFont(new Font("Dialog", Font.PLAIN, 13));
                cb.setOpaque(false);
                cb.setAlignmentY(Component.CENTER_ALIGNMENT);
                cb.addActionListener(e -> {
                    CheckItemDAO.toggleChecked(item.getId(), cb.isSelected());
                    if (onTodoChanged != null) onTodoChanged.run();
                });
                line.add(cb);
            } else {
                JLabel label = new JLabel(item.getContent());
                label.setFont(new Font("Dialog", Font.PLAIN, 13));
                label.setAlignmentY(Component.CENTER_ALIGNMENT);
                line.add(label);
            }

            JButton editBtn = new JButton("수정");
            editBtn.setFont(new Font("Dialog", Font.PLAIN, 11));
            editBtn.setFocusable(false);
            editBtn.setMargin(new Insets(2, 6, 2, 6));
            editBtn.setToolTipText("수정");
            editBtn.addActionListener(e -> {
                String newContent = JOptionPane.showInputDialog(this, "수정할 내용:", item.getContent());
                if (newContent != null && !newContent.isBlank()) {
                    item.setContent(newContent.trim());
                    CheckItemDAO.update(item);
                    reloadCheckList();
                    if (onTodoChanged != null) onTodoChanged.run();
                }
            });

            JButton delBtn = new JButton("삭제");
            delBtn.setFont(new Font("Dialog", Font.PLAIN, 11));
            delBtn.setFocusable(false);
            delBtn.setMargin(new Insets(2, 6, 2, 6));
            delBtn.setToolTipText("삭제");
            delBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(this, "삭제할까요?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    CheckItemDAO.delete(item.getId());
                    reloadCheckList();
                    if (onTodoChanged != null) onTodoChanged.run();
                }
            });

            JButton upBtn = new JButton("↑");
            upBtn.setFont(new Font("Dialog", Font.PLAIN, 11));
            upBtn.setFocusable(false);
            upBtn.setMargin(new Insets(2, 6, 2, 6));
            upBtn.setEnabled(index > 0);
            upBtn.addActionListener(e -> {
                if (index > 0) {
                    swapSeq(items.get(index), items.get(index - 1));
                }
            });

            JButton downBtn = new JButton("↓");
            downBtn.setFont(new Font("Dialog", Font.PLAIN, 11));
            downBtn.setFocusable(false);
            downBtn.setMargin(new Insets(2, 6, 2, 6));
            downBtn.setEnabled(index < items.size() - 1);
            downBtn.addActionListener(e -> {
                if (index < items.size() - 1) {
                    swapSeq(items.get(index), items.get(index + 1));
                }
            });

            line.add(Box.createHorizontalGlue());
            line.add(editBtn);
            line.add(delBtn);
            line.add(upBtn);
            line.add(downBtn);

            checkListPanel.add(line);
            checkListPanel.add(Box.createVerticalStrut(4));
        }
        checkListPanel.revalidate();
        checkListPanel.repaint();
    }

    private void swapSeq(CheckItem a, CheckItem b) {
        int tmp = a.getSeq();
        a.setSeq(b.getSeq());
        b.setSeq(tmp);
        CheckItemDAO.update(a);
        CheckItemDAO.update(b);
        reloadCheckList();
        if (onTodoChanged != null) onTodoChanged.run();
    }

    private void onAddItem(String type) {
        String content = JOptionPane.showInputDialog(this, (type.equals("checkbox") ? "새 체크박스 내용:" : "새 텍스트:"));
        if (content != null && !content.isBlank()) {
            List<CheckItem> items = CheckItemDAO.listByTodo(todo.getId());
            int nextSeq = items.size() + 1;
            CheckItem newItem = new CheckItem();
            newItem.setTodoId(todo.getId());
            newItem.setContent(content.trim());
            newItem.setChecked(false);
            newItem.setType(type);
            newItem.setSeq(nextSeq);
            CheckItemDAO.insert(newItem);
            reloadCheckList();
            if (onTodoChanged != null) onTodoChanged.run();
        }
    }
}
