package gui.main.panel;

import api.model.Todo;
import api.model.TodoDAO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TodoPanel extends JPanel {
    private int userId;

    private JTextField dateInputField;
    private JButton btnPrevDay, btnNextDay, btnSearchDate;

    private JTextField searchField;
    private JButton btnSearchTitle;

    private JComboBox<String> sortCombo;

    private JPanel todoListPanel;
    private JScrollPane todoScrollPane;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TodoDAO dao = new TodoDAO();

    public TodoPanel(int userId) {
        this.userId = userId;
        setLayout(new MigLayout("insets 0, fill", "[240!][grow,fill]", "[grow,fill]"));

        Font base = UIManager.getFont("ToggleButton.font");
        /* ------------------ 왼쪽 패널 ------------------ */
        JPanel leftPanel = new JPanel(new MigLayout(
                "insets 18 14 18 6, wrap 1",
                "[grow,fill]", ""));

        JLabel dateLabel = new JLabel("날짜 선택");
        dateLabel.setFont(base.deriveFont(Font.BOLD, 15));
        leftPanel.add(dateLabel, "gapbottom 6");

        JPanel datePanel = new JPanel(new MigLayout(
                "insets 0, wrap 2",
                "[30!]2[grow,fill]8[30!]",
                "[]4[]"));
        btnPrevDay = new JButton("<");
        dateInputField = new JTextField(DATE_FORMAT.format(LocalDate.now()), 10);
        btnNextDay = new JButton(">");

        datePanel.add(btnPrevDay, "cell 0 0");
        datePanel.add(dateInputField, "cell 1 0, growx");
        datePanel.add(btnNextDay, "cell 2 0");
        btnSearchDate = new JButton("검색");
        datePanel.add(btnSearchDate, "cell 0 1 3 1, growx");

        leftPanel.add(datePanel, "growx, gapbottom 10");

        leftPanel.add(new JSeparator(), "growx, gapy 8 8");

        JLabel searchLabel = new JLabel("제목 검색");
        searchLabel.setFont(base.deriveFont(Font.BOLD, 15));
        leftPanel.add(searchLabel, "gapbottom 6");

        JPanel searchPanel = new JPanel(new MigLayout("insets 0", "[grow,fill][]", ""));
        searchField    = new JTextField();
        btnSearchTitle = new JButton("검색");
        searchPanel.add(searchField,    "growx");
        searchPanel.add(btnSearchTitle, "");
        leftPanel.add(searchPanel, "growx, gapbottom 10");

        leftPanel.add(new JSeparator(), "growx, gapy 8 8");

        JLabel sortLabel = new JLabel("정렬");
        sortLabel.setFont(base.deriveFont(Font.BOLD, 15));
        leftPanel.add(sortLabel, "gapbottom 6");

        sortCombo = new JComboBox<>(new String[]{"기간 오름차순", "기간 내림차순"});
        sortCombo.setFont(base.deriveFont(Font.PLAIN, 13));
        leftPanel.add(sortCombo, "growx");

        leftPanel.add(new JLabel(), "grow, pushy");

        /* ------------------ 오른쪽 패널 ------------------ */
        JPanel rightPanel = new JPanel(new MigLayout("insets 18 4 18 16, wrap 1, fill", "[grow,fill]", "[]8[grow,fill]"));

        JButton addBtn = new JButton("+ 계획 추가");
        addBtn.setFont(base.deriveFont(Font.BOLD, 14));
        rightPanel.add(addBtn, "alignx right, gapbottom 8");

        todoListPanel = new JPanel(new MigLayout("insets 0, wrap 1, fillx", "[grow,fill]", ""));
        todoListPanel.setOpaque(false);

        todoScrollPane = new JScrollPane(todoListPanel);
        todoScrollPane.setBorder(null);
        rightPanel.add(todoScrollPane, "grow, pushy");

        add(leftPanel,  "cell 0 0, growy");
        add(rightPanel, "cell 1 0, grow, push");

        /* -------- 이벤트 연결 -------- */
        btnPrevDay.addActionListener(e -> changeDateBy(-1));
        btnNextDay.addActionListener(e -> changeDateBy(1));
        btnSearchDate.addActionListener(e -> reloadTodos());
        sortCombo.addActionListener(e     -> reloadTodos());
        dateInputField.addActionListener(e-> reloadTodos());

        btnSearchTitle.addActionListener(e -> reloadTodosByTitle());
        searchField.addActionListener(e    -> reloadTodosByTitle());

        addBtn.addActionListener(e -> openTodoEditor(null));

        reloadTodos();
    }

    private void changeDateBy(int days) {
        try {
            LocalDate currentDate = LocalDate.parse(dateInputField.getText(), DATE_FORMAT);
            LocalDate newDate = currentDate.plusDays(days);
            setDate(newDate);
        } catch (Exception ex) {
            setDate(LocalDate.now());
        }
    }

    private void setDate(LocalDate date) {
        dateInputField.setText(DATE_FORMAT.format(date));
        reloadTodos();
    }

    private void reloadTodos() {
        todoListPanel.removeAll();

        LocalDate selDate;
        try {
            selDate = LocalDate.parse(dateInputField.getText(), DATE_FORMAT);
        } catch (Exception ex) {
            todoListPanel.add(new JLabel("유효한 날짜를 입력하세요 (yyyy-MM-dd)."));
            refreshPanel();
            return;
        }

        List<Todo> todos;
        try {
            todos = dao.listByDate(userId, selDate);
        } catch (Exception e) {
            todos = List.of();
            todoListPanel.add(new JLabel("DB 오류: " + e.getMessage()));
            refreshPanel();
            return;
        }

        String sort = (String) sortCombo.getSelectedItem();
        if ("기간 내림차순".equals(sort)) {
            todos = todos.stream()
                    .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
                    .collect(Collectors.toList());
        } else {
            todos = todos.stream()
                    .sorted((a, b) -> a.getStartDate().compareTo(b.getStartDate()))
                    .collect(Collectors.toList());
        }

        if (todos.isEmpty()) {
            todoListPanel.add(new JLabel("해당 날짜에 할 일이 없습니다."));
        } else {
            for (Todo todo : todos) {
                TodoItemPanel panel = new TodoItemPanel(todo,
                        () -> {
                            try {
                                dao.toggleCompleted(todo.getId(), todo.getCompleted() == 0);
                                reloadTodos();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        },
                        () -> openTodoEditor(todo),
                        () -> {
                            try {
                                dao.deleteTodo(todo.getId());
                                reloadTodos();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                todoListPanel.add(panel);
                todoListPanel.add(Box.createVerticalStrut(8));
            }
        }
        refreshPanel();
    }

    private void reloadTodosByTitle() {
        todoListPanel.removeAll();

        String keyword = searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            reloadTodos();
            return;
        }

        List<Todo> todos;
        try {
            todos = dao.listAll(userId).stream()
                    .filter(todo -> todo.getTitle().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            todos = List.of();
            todoListPanel.add(new JLabel("DB 오류: " + e.getMessage()));
            refreshPanel();
            return;
        }

        String sort = (String) sortCombo.getSelectedItem();
        if ("기간 내림차순".equals(sort)) {
            todos = todos.stream()
                    .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
                    .collect(Collectors.toList());
        } else {
            todos = todos.stream()
                    .sorted((a, b) -> a.getStartDate().compareTo(b.getStartDate()))
                    .collect(Collectors.toList());
        }

        if (todos.isEmpty()) {
            todoListPanel.add(new JLabel("검색 결과가 없습니다."));
        } else {
            for (Todo todo : todos) {
                TodoItemPanel panel = new TodoItemPanel(todo,
                        () -> {
                            try {
                                dao.toggleCompleted(todo.getId(), todo.getCompleted() == 0);
                                reloadTodosByTitle();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        },
                        () -> openTodoEditor(todo),
                        () -> {
                            try {
                                dao.deleteTodo(todo.getId());
                                reloadTodosByTitle();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                todoListPanel.add(panel);
                todoListPanel.add(Box.createVerticalStrut(8));
            }
        }
        refreshPanel();
    }

    private void refreshPanel() {
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    private void openTodoEditor(Todo todo) {
        TodoEditorFrame.TriConsumer<String, LocalDate, LocalDate> onSave = (title, start, end) -> {
            try {
                if (todo == null) {
                    Todo newTodo = new Todo();
                    newTodo.setUserId(userId);
                    newTodo.setTitle(title);
                    newTodo.setStartDate(start);
                    newTodo.setEndDate(end);
                    newTodo.setCompleted(0);
                    
                    dao.insertTodo(newTodo);
                } else {
                    todo.setTitle(title);
                    todo.setStartDate(start);
                    todo.setEndDate(end);
                    dao.updateTodo(todo);
                }
                reloadTodos();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        TodoEditorFrame frame = new TodoEditorFrame(
        	    SwingUtilities.getWindowAncestor(this),
        	    todo == null ? "계획 추가" : "계획 수정",
        	    todo == null ? "" : todo.getTitle(),
        	    todo == null ? LocalDate.now() : LocalDate.parse(todo.getStartDate()),
        	    todo == null ? LocalDate.now() : LocalDate.parse(todo.getEndDate()),
        	    onSave
        	);

        frame.setVisible(true);
    }
}
