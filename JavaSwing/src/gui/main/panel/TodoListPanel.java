package gui.main.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import api.model.Todos;

public class TodoListPanel extends JPanel {
	
    private Point initialClick;
    private Todos todo;
    
//	public TodoListPanel(Todos todo) {
//		this.todo = todo;
//        setLayout(new BorderLayout());
//        setBorder(BorderFactory.createLineBorder(Color.GRAY));
//        setMaximumSize(new Dimension(550, 60));
//        setBackground(new Color(255, 255, 200));
//
//        JLabel label = new JLabel(todo.content);
//        label.setFont(new Font("Dialog", Font.PLAIN, 16));
//        add(label, BorderLayout.CENTER);
//
//        addMouseListener(new MouseAdapter() {
//            public void mousePressed(MouseEvent e) {
//                initialClick = e.getPoint();
//            }
//            public void mouseReleased(MouseEvent e) {
//                reorderItems();
//            }
//        });
//
//        addMouseMotionListener(new MouseMotionAdapter() {
//            public void mouseDragged(MouseEvent e) {
//                int thisIndex = listPanel.getComponentZOrder(TodoListPanel.this);
//                Component comp = listPanel.getComponentAt(e.getLocationOnScreen());
//                if (comp != null && comp != TodoListPanel.this && comp instanceof TodoListPanel) {
//                    int targetIndex = listPanel.getComponentZOrder(comp);
//                    listPanel.remove(TodoListPanel.this);
//                    listPanel.add(TodoListPanel.this, targetIndex);
//                    listPanel.revalidate();
//                    listPanel.repaint();
//                }
//            }
//        });
//    }
}
