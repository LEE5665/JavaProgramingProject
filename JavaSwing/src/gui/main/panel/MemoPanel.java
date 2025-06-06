package gui.main.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import api.model.Memo;
import api.model.MemoDAO;
import gui.main.MemoEditorFrame;

public class MemoPanel extends JPanel {
	private JPanel listPanel;
	private JScrollPane scrollPane;
	private int userId;

	private final MemoDAO memoDAO = new MemoDAO();

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
		try {
			MemoEditorFrame editor = new MemoEditorFrame(SwingUtilities.getWindowAncestor(this), "메모 추가", "",
					content -> {
						if (content != null && !content.isBlank()) {
							Memo memo = new Memo();
							memo.setUserId(userId);
							memo.setContent(content);
							memo.setSeq(listPanel.getComponentCount() + 1);
							memo.setFixFlag(false);
							memo.setUpdateAt(null);
							memo.setCreatedAt(null);
							int id = -1;
							try {
								id = memoDAO.insertMemo(memo);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							if (id > 0)
								reloadSingleMemoWithAnimation(id);
							else
								reloadMemos();
						}
					});
			editor.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void reloadMemos() {
		listPanel.removeAll();
		List<Memo> memos = null;
		try {
			memos = memoDAO.selectMemosByUser(userId);
		} catch (Exception ex) {
			ex.printStackTrace();
			memos = List.of();
		}
		for (Memo memo : memos) {
			MemoCardPanel[] cardRef = new MemoCardPanel[1];
			cardRef[0] = new MemoCardPanel(memo, () -> {
				try {
					cardRef[0].playDeleteAnimation(() -> {
						try {
							memoDAO.deleteMemo(memo.getId());
							reloadMemos();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					});
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}, () -> {
				try {
					new MemoEditorFrame(SwingUtilities.getWindowAncestor(this), "메모 수정", memo.getContent(), updated -> {
						try {
							if (updated != null) {
								memo.setContent(updated);
								memoDAO.updateMemo(memo);
								reloadMemos();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}).setVisible(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}, () -> {
				try {
					memo.setFixFlag(!memo.isFixFlag());
					memoDAO.updateMemo(memo);
					reloadMemos();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
			listPanel.add(cardRef[0]);
		}
		listPanel.revalidate();
		listPanel.repaint();
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

	/**
	 * 테마 업데이트를 위한 메서드. 이 메서드는 직접 호출해야 하며, updateUI()에서 호출하면 안 됨.
	 */
	public void updateTheme() {
		// 개별 컴포넌트에 대해 직접 테마 업데이트 수행
		// SwingUtilities.updateComponentTreeUI(this) 호출 제거

		// 대신 필요한 컴포넌트만 직접 업데이트
		for (Component comp : listPanel.getComponents()) {
			if (comp instanceof MemoCardPanel card) {
				card.updateThemeUI();
			}
		}
	}

	/**
	 * JPanel의 updateUI를 오버라이드. 상위 클래스의 updateUI를 호출한 후 필요한 추가 작업 수행.
	 */
	@Override
	public void updateUI() {
		// 먼저 상위 클래스의 updateUI 호출
		super.updateUI();

		// 필요한 추가 작업 수행 (updateTheme 호출 제거)
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
