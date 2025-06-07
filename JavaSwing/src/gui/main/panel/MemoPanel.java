package gui.main.panel;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import api.model.Memo;
import api.model.MemoDAO;
import gui.main.MemoEditorFrame;
import gui.main.FileSystemImageHandler;

public class MemoPanel extends JPanel {
    private JPanel listPanel;
    private JScrollPane scrollPane;
    private int userId;
    private final Map<Integer, MemoEditorFrame> openEditors = new HashMap<>();

    public MemoPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        JButton addButton = new JButton("+ 메모 추가");
        addButton.addActionListener(e -> addMemo());

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addPanel.setBackground(getBackground());
        addPanel.add(addButton);

        listPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(12, 24, 12, 24));

        scrollPane = new JScrollPane(listPanel);
        scrollPane.getViewport().setBackground(UIManager.getColor("Viewport.background"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        add(addPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        reloadMemos();
    }

    private void addMemo() {
        Memo memo = new Memo();
        memo.setUserId(userId);
        memo.setContent("");
        memo.setFixFlag(false);
        memo.setUpdateAt(null);
        memo.setCreatedAt(null);

        MemoEditorFrame editor = new MemoEditorFrame(
            SwingUtilities.getWindowAncestor(this), memo, true, true,
            content -> {
                if (content != null && !content.isBlank()) {
                    memo.setContent(content);
                    int id = -1;
                    try {
                        id = MemoDAO.insertMemo(memo);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (id > 0)
                        reloadSingleMemoWithAnimation(id);
                    else
                        reloadMemos();
                }
            }
        );
        editor.setVisible(true);
    }

    private void reloadMemos() {
        listPanel.removeAll();
        List<Memo> memos = null;
        try {
            memos = MemoDAO.selectMemosByUser(userId);
        } catch (Exception ex) {
            ex.printStackTrace();
            memos = List.of();
        }
        for (Memo memo : memos) {
            MemoCardPanel[] cardRef = new MemoCardPanel[1];
            cardRef[0] = new MemoCardPanel(memo,
                // 삭제 콜백
                () -> {
                    try {
                        cardRef[0].playDeleteAnimation(() -> {
                            try {
                                closeEditor(memo.getId());
                                try {
                                    FileSystemImageHandler imgHandler = new FileSystemImageHandler();
                                    imgHandler.deleteImagesInMarkdown(memo.getContent());
                                } catch (Exception imgEx) {
                                    imgEx.printStackTrace();
                                }
                                MemoDAO.deleteMemo(memo.getId());
                                reloadMemos();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                },
                // 수정 콜백
                () -> openMemoEditor(memo, true),
                // 고정 콜백
                () -> {
                    try {
                        memo.setFixFlag(!memo.isFixFlag());
                        MemoDAO.updateMemo(memo);
                        reloadMemos();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

            cardRef[0].setOnCardClick(() -> openMemoEditor(memo, false));
            listPanel.add(cardRef[0]);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void openMemoEditor(Memo memo, boolean editMode) {
        int memoId = memo.getId();
        if (memoId != 0 && openEditors.containsKey(memoId)) {
            MemoEditorFrame frame = openEditors.get(memoId);
            frame.setMode(editMode);
            frame.toFront();
            frame.requestFocus();
            return;
        }
        MemoEditorFrame frame = new MemoEditorFrame(
            SwingUtilities.getWindowAncestor(this), memo, editMode, false,
            updated -> {
                try {
                    if (updated != null) {
                        memo.setContent(updated);
                        MemoDAO.updateMemo(memo);
                        reloadMemos();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        );
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                openEditors.remove(memoId);
            }
        });
        if (memoId != 0)
            openEditors.put(memoId, frame);
        frame.setVisible(true);
    }

    private void closeEditor(int memoId) {
        if (openEditors.containsKey(memoId)) {
            MemoEditorFrame frame = openEditors.get(memoId);
            frame.dispose();
            openEditors.remove(memoId);
        }
    }

    public void closeAllEditors() {
        for (MemoEditorFrame frame : openEditors.values()) {
            frame.dispose();
        }
        openEditors.clear();
    }

    private void reloadSingleMemoWithAnimation(int targetId) {
        reloadMemos();
        SwingUtilities.invokeLater(() -> {
            for (Component comp : listPanel.getComponents()) {
                MemoCardPanel card = (MemoCardPanel) comp;
                if (card.getMemo().getId() == targetId) {
                    card.playEntryAnimation();
                    break;
                }
            }
        });
    }

    public void updateTheme() {
        for (Component comp : listPanel.getComponents()) {
            if (comp instanceof MemoCardPanel card) {
                card.updateThemeUI();
            }
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (listPanel != null) {
            for (Component comp : listPanel.getComponents()) {
                if (comp instanceof MemoCardPanel card) {
                    card.updateThemeUI();
                }
            }
        }
    }

    public static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }
        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth() > 0 ? target.getWidth() : Integer.MAX_VALUE;
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + getHgap() * 2);
                int x = 0, y = insets.top + getVgap(), rowH = 0;
                Dimension d = new Dimension(0, 0);
                for (Component c : target.getComponents()) {
                    if (!c.isVisible())
                        continue;
                    Dimension cd = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x == 0 || x + cd.width <= maxWidth) {
                        if (x > 0)
                            x += getHgap();
                        x += cd.width;
                        rowH = Math.max(rowH, cd.height);
                    } else {
                        x = cd.width;
                        y += getVgap() + rowH;
                        rowH = cd.height;
                    }
                    d.width = Math.max(d.width, x);
                }
                d.height = y + rowH + getVgap();
                return d;
            }
        }
    }
}
