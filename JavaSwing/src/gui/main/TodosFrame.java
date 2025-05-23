package gui.main;

import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import api.DB;
import api.model.Todos;
import net.miginfocom.swing.MigLayout;

public class TodosFrame extends JFrame {
    private JPanel listPanel;
    private int userId;

    public TodosFrame(int userId) {
        this.userId = userId;
        setTitle("일정 관리");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(new Color(20, 20, 20));

        JButton addButton = new JButton("+ 메모 추가");
        addButton.addActionListener(e -> addNewTodo());

        listPanel = new JPanel(new MigLayout("insets 20, wrap 3, gap 20")); 
        listPanel.setBackground(new Color(30, 30, 30));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getViewport().setBackground(new Color(20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(new Color(20, 20, 20));
        topPanel.add(addButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadTodos();
    }

    private void loadTodos() {
        listPanel.removeAll();
        List<Todos> todos = DB.loadTodos(userId);

        for (Todos todo : todos) {
            TodoItemPanel itemPanel = new TodoItemPanel(todo);
            listPanel.add(itemPanel);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private void addNewTodo() {
        String content = JOptionPane.showInputDialog(this, "메모 내용 입력");
        if (content != null && !content.trim().isEmpty()) {
            int orderIndex = listPanel.getComponentCount() + 1;
            DB.insertTodo(userId, content, orderIndex);
            loadTodos();
        }
    }

    class TodoItemPanel extends JPanel {
        private static TodoItemPanel selectedPanel = null;
        private final Todos todo;

        public TodoItemPanel(Todos todo) {
            this.todo = todo;

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(250, 120));
            setBackground(new Color(245, 245, 250)); // 카드 배경

            // FlatLaf 스타일 적용 (arc만 사용)
            putClientProperty(FlatClientProperties.STYLE, "arc:20");
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


            JLabel label = new JLabel("<html>" + todo.content + "</html>");
            label.setFont(new Font("Dialog", Font.PLAIN, 14));
            label.setForeground(new Color(30, 30, 30));
            label.setVerticalAlignment(SwingConstants.TOP);
            add(label, BorderLayout.CENTER);

            // 선택 시 강조
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (selectedPanel != null) {
                        selectedPanel.setBackground(new Color(245, 245, 250));
                    }
                    setBackground(new Color(210, 230, 255));
                    selectedPanel = TodoItemPanel.this;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    reorderItems();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    Point panelPoint = SwingUtilities.convertPoint(
                        TodoItemPanel.this, e.getPoint(), listPanel
                    );
                    Component comp = listPanel.getComponentAt(panelPoint);

                    if (comp != null && comp != TodoItemPanel.this && comp instanceof TodoItemPanel) {
                        int targetIndex = listPanel.getComponentZOrder(comp);
                        int currentIndex = listPanel.getComponentZOrder(TodoItemPanel.this);

                        if (targetIndex != currentIndex) {
                            listPanel.remove(TodoItemPanel.this);
                            listPanel.add(TodoItemPanel.this, targetIndex);
                            listPanel.revalidate();
                            listPanel.repaint();
                        }
                    }
                }
            });
        }
    }


    private void reorderItems() {
        Component[] components = listPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TodoItemPanel) {
                TodoItemPanel panel = (TodoItemPanel) components[i];
                DB.updateOrder(panel.todo.id, i + 1);
            }
        }
    }
}