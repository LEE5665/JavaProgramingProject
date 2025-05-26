package gui.main.panel;

import api.model.Todo;
import api.model.TodoDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TodoPanel extends JPanel {
    private int userId;

    // 좌측: 날짜 선택, 정렬 옵션, 필터
    private JList<LocalDate> dateList;
    private DefaultListModel<LocalDate> dateListModel;
    private JComboBox<String> sortCombo;

    // 우측: 할 일 목록, 추가 버튼
    private JPanel todoListPanel;
    private JScrollPane todoScrollPane;

    public TodoPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        // 1. 왼쪽: 날짜/정렬
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setPreferredSize(new Dimension(190, 999));
        leftPanel.setBorder(new EmptyBorder(18, 14, 18, 6));
        leftPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel dateLabel = new JLabel("계획 날짜");
        dateLabel.setFont(new Font("Dialog", Font.BOLD, 15));
        dateLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        leftPanel.add(dateLabel, BorderLayout.NORTH);

        dateListModel = new DefaultListModel<>();
        dateList = new JList<>(dateListModel);
        dateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dateList.setFont(new Font("Dialog", Font.PLAIN, 13));
        JScrollPane dateScroll = new JScrollPane(dateList);
        dateScroll.setPreferredSize(new Dimension(150, 220));
        leftPanel.add(dateScroll, BorderLayout.CENTER);

        sortCombo = new JComboBox<>(new String[]{"기간 오름차순", "기간 내림차순", "가장 빠른 일정", "미완료 먼저"});
        sortCombo.setFont(new Font("Dialog", Font.PLAIN, 13));
        sortCombo.setMaximumSize(new Dimension(150, 32));
        leftPanel.add(sortCombo, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // 2. 오른쪽: 할 일 + 추가
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBorder(new EmptyBorder(18, 4, 18, 16));
        rightPanel.setBackground(UIManager.getColor("Panel.background"));

        // 할 일 목록
        todoListPanel = new JPanel();
        todoListPanel.setLayout(new BoxLayout(todoListPanel, BoxLayout.Y_AXIS));
        todoListPanel.setOpaque(false);
        todoScrollPane = new JScrollPane(todoListPanel);
        todoScrollPane.setBorder(null);

        rightPanel.add(todoScrollPane, BorderLayout.CENTER);

        // 추가 버튼
        JButton addBtn = new JButton("+ 계획 추가");
        addBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        addBtn.setPreferredSize(new Dimension(120, 40));
        addBtn.addActionListener(e -> openTodoEditor(null));
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addPanel.setOpaque(false);
        addPanel.add(addBtn);
        rightPanel.add(addPanel, BorderLayout.NORTH);

        add(rightPanel, BorderLayout.CENTER);

        // 이벤트: 날짜/정렬 선택시 새로고침
        dateList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) reloadTodos(); });
        sortCombo.addActionListener(e -> reloadTodos());

        reloadDateList();
    }

    private void reloadDateList() {
        dateListModel.clear();
        List<LocalDate> dates = TodoDAO.listAllDates(userId);
        for (LocalDate d : dates) dateListModel.addElement(d);

        if (!dates.isEmpty()) dateList.setSelectedIndex(0);
        else reloadTodos();
    }

    private void reloadTodos() {
        todoListPanel.removeAll();
        LocalDate selDate = dateList.getSelectedValue();

        if (selDate == null) {
            todoListPanel.add(new JLabel("날짜를 선택하세요."));
        } else {
            List<Todo> todos = TodoDAO.listByDate(userId, selDate);

            // 정렬
            switch ((String) sortCombo.getSelectedItem()) {
                case "기간 내림차순":
                    todos = todos.stream().sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate())).collect(Collectors.toList());
                    break;
                case "가장 빠른 일정":
                    todos = todos.stream().sorted((a, b) -> a.getStartDate().compareTo(b.getStartDate())).collect(Collectors.toList());
                    break;
                case "미완료 먼저":
                    todos = todos.stream().sorted((a, b) -> Boolean.compare(a.isCompleted(), b.isCompleted())).collect(Collectors.toList());
                    break;
                default: // 오름차순
                    todos = todos.stream().sorted((a, b) -> a.getStartDate().compareTo(b.getStartDate())).collect(Collectors.toList());
                    break;
            }

            if (todos.isEmpty()) {
                todoListPanel.add(new JLabel("해당 날짜에 할 일이 없습니다."));
            } else {
                for (Todo todo : todos) {
                    TodoItemPanel panel = new TodoItemPanel(todo,
                        () -> { // 토글
                            TodoDAO.toggleCompleted(todo.getId(), !todo.isCompleted());
                            reloadTodos();
                        },
                        () -> openTodoEditor(todo),
                        () -> {
                            TodoDAO.delete(todo.getId());
                            reloadTodos();
                        }
                    );
                    todoListPanel.add(panel);
                    todoListPanel.add(Box.createVerticalStrut(8));
                }
            }
        }

        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    private void openTodoEditor(Todo todo) {
        TodoEditorFrame frame = new TodoEditorFrame(
            SwingUtilities.getWindowAncestor(this),
            todo == null ? "계획 추가" : "계획 수정",
            todo == null ? "" : todo.getTitle(),
            todo == null ? "" : todo.getNote(),
            todo == null ? null : todo.getStartDate(),
            todo == null ? null : todo.getEndDate(),
            (title, note, start, end) -> {
                if (todo == null) {
                    Todo newTodo = new Todo(userId, null, 0, title, note, start, end);
                    TodoDAO.insert(newTodo);
                } else {
                    todo.setTitle(title);
                    todo.setNote(note);
                    todo.setStartDate(start);
                    todo.setEndDate(end);
                    TodoDAO.update(todo);
                }
                reloadDateList();
            }
        );
        frame.setVisible(true);
    }
}
