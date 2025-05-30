package gui.main.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import api.model.Memo;
import api.model.MemoDAO;
import gui.main.SwingHtmlEditorWithImage;

public class MemoPanel extends JPanel {
	private final MemoDAO memoDAO = new MemoDAO();
	private final int userId;
	private final JPanel listPanel;
	private final JScrollPane scrollPane;

	public MemoPanel(int userId) {
		this.userId = userId;
		setLayout(new BorderLayout());
		setBackground(UIManager.getColor("Panel.background"));

		JButton addButton = new JButton("+ 메모 추가");
		addButton.addActionListener(e -> addMemo());
		JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addPanel.setBackground(getBackground());
		addPanel.add(addButton);
		add(addPanel, BorderLayout.NORTH);

		listPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
		listPanel.setOpaque(false);
		scrollPane = new JScrollPane(listPanel);
		scrollPane.getViewport().setBackground(UIManager.getColor("Viewport.background"));
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		add(scrollPane, BorderLayout.CENTER);

		reloadMemos();
	}

	private void addMemo() {
		try {
			SwingHtmlEditorWithImage dlg = new SwingHtmlEditorWithImage(SwingUtilities.getWindowAncestor(this), "메모 추가",
					"", html -> {
						if (html != null && !html.isBlank()) {
							Memo m = new Memo();
							m.setUserId(userId);
							m.setContent(html);
							m.setSeq(listPanel.getComponentCount() + 1);
							m.setFixFlag(false);
							int id = memoDAO.insertMemo(m);
							if (id > 0)
								reloadSingleMemoWithAnimation(id);
							else
								reloadMemos();
						}
					});
			dlg.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void editMemo(Memo memo) {
		try {
			SwingHtmlEditorWithImage dlg = new SwingHtmlEditorWithImage(SwingUtilities.getWindowAncestor(this), "메모 수정",
					memo.getContent(), html -> {
						if (html != null) {
							memo.setContent(html);
							try {
								memoDAO.updateMemo(memo);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							reloadMemos();
						}
					});
			dlg.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void deleteMemo(Memo memo, MemoCardPanel card) {
		try {
			card.playDeleteAnimation(() -> {
				try {
					memoDAO.deleteMemo(memo.getId());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				reloadMemos();
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void reloadMemos() {
		listPanel.removeAll();
		List<Memo> memos;
		try {
			memos = memoDAO.selectMemosByUser(userId);
		} catch (Exception ex) {
			ex.printStackTrace();
			memos = List.of();
		}
		for (Memo memo : memos) {
			MemoCardPanel[] ref = new MemoCardPanel[1];
			ref[0] = new MemoCardPanel(memo, () -> deleteMemo(memo, ref[0]), () -> editMemo(memo), () -> {
				memo.setFixFlag(!memo.isFixFlag());
				try {
					memoDAO.updateMemo(memo);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				reloadMemos();
			});
			listPanel.add(ref[0]);
		}
		listPanel.revalidate();
		listPanel.repaint();
	}

	private void reloadSingleMemoWithAnimation(int targetId) {
		reloadMemos();
		SwingUtilities.invokeLater(() -> {
			for (Component c : listPanel.getComponents()) {
				MemoCardPanel card = (MemoCardPanel) c;
				if (card.getMemo().getId() == targetId) {
					card.playEntryAnimation();
					break;
				}
			}
		});
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
				int maxWidth = (target.getWidth() > 0 ? target.getWidth() : Integer.MAX_VALUE)
						- (target.getInsets().left + target.getInsets().right + getHgap() * 2);
				int x = 0, y = getVgap(), rowH = 0;
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