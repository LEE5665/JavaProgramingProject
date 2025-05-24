package gui.main.panel;

import javax.swing.*;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import api.DB;
import api.model.Memo;
import gui.main.MemoEditorFrame;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MemoPanel extends JPanel {
    private JPanel listPanel;
    private JPanel actionPanel;
    private Memo selectedMemo = null;
    private int userId;

    public MemoPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        JButton addButton = new JButton("+ 메모 추가");
        addButton.addActionListener(e -> {
            new MemoEditorFrame("", content -> {
                int orderIndex = listPanel.getComponentCount() + 1;
                DB.insertMemo(userId, content, orderIndex);
                loadMemos();
            }).setVisible(true);
        });

        JPanel addPanel = new JPanel(new MigLayout("insets 10 20 10 20", "[grow][100!]"));
        addPanel.setBackground(new Color(30, 30, 30));
        addPanel.add(Box.createGlue(), "growx");
        addPanel.add(addButton);

        listPanel = new JPanel(new MigLayout("insets 20, wrap 3, gap 20"));
        listPanel.setBackground(new Color(30, 30, 30));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getViewport().setBackground(new Color(20, 20, 20));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBackground(new Color(20, 20, 20));
        actionPanel.setVisible(false);

        add(addPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        loadMemos();
    }

    private void loadMemos() {
        listPanel.removeAll();
        List<Memo> memos = DB.loadMemo(userId);
        for (Memo memo : memos) {
            MemoItemPanel itemPanel = new MemoItemPanel(memo);
            listPanel.add(itemPanel);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    class MemoItemPanel extends JPanel {
        private static MemoItemPanel selectedPanel = null;
        private final Memo memo;
        private Point initialClick;

        public MemoItemPanel(Memo memo) {
            this.memo = memo;

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(350, 150));
            setMaximumSize(new Dimension(350, 200));
            setBackground(new Color(60, 63, 65));
            putClientProperty(FlatClientProperties.STYLE, "arc:20");
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextArea textArea = new JTextArea(memo.content);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setBackground(new Color(60, 63, 65));
            textArea.setForeground(Color.WHITE);
            textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
            textArea.setBorder(null);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(300, 100));
            scrollPane.setBorder(null);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getViewport().setBackground(new Color(60, 63, 65));

            add(scrollPane, BorderLayout.CENTER);

            MouseAdapter mouseAdapter = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    initialClick = e.getPoint();
                    if (selectedPanel != null)
                        selectedPanel.setBackground(new Color(60, 63, 65));
                    setBackground(new Color(50, 55, 60));
                    selectedPanel = MemoItemPanel.this;
                    selectedMemo = memo;
                    showActionPanel();
                }

                public void mouseReleased(MouseEvent e) {
                    reorderItems();
                }
            };

            MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point panelPoint = SwingUtilities.convertPoint(MemoItemPanel.this, e.getPoint(), listPanel);
                    Component comp = listPanel.getComponentAt(panelPoint);
                    if (comp != null && comp != MemoItemPanel.this && comp instanceof MemoItemPanel) {
                        int targetIndex = listPanel.getComponentZOrder(comp);
                        int currentIndex = listPanel.getComponentZOrder(MemoItemPanel.this);

                        if (targetIndex != currentIndex) {
                            listPanel.remove(MemoItemPanel.this);
                            listPanel.add(MemoItemPanel.this, targetIndex);
                            listPanel.revalidate();
                            listPanel.repaint();
                        }
                    }
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(motionAdapter);
            scrollPane.addMouseListener(mouseAdapter);
            scrollPane.addMouseMotionListener(motionAdapter);
            textArea.addMouseListener(mouseAdapter);
            textArea.addMouseMotionListener(motionAdapter);
        }
    }

    private void reorderItems() {
        Component[] components = listPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof MemoItemPanel) {
                MemoItemPanel panel = (MemoItemPanel) components[i];
                DB.updateOrder(panel.memo.id, i + 1);
            }
        }
    }

    private void showActionPanel() {
        actionPanel.removeAll();

        JLabel selectedLabel = new JLabel("선택한 메모:");
        JButton editButton = new JButton("수정");
        JButton deleteButton = new JButton("삭제");

        editButton.addActionListener(e -> {
            new MemoEditorFrame(selectedMemo.content, newContent -> {
                DB.updateMemoContent(selectedMemo.id, newContent);
                loadMemos();
            }).setVisible(true);
        });

        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DB.deleteMemo(selectedMemo.id);
                selectedMemo = null;
                actionPanel.setVisible(false);
                loadMemos();
            }
        });

        actionPanel.add(selectedLabel);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.setVisible(true);
        actionPanel.revalidate();
        actionPanel.repaint();
    }
}
