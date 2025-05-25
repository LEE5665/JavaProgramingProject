package gui.main.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import api.DB;
import api.model.Memo;
import gui.main.MemoEditorFrame;
import net.miginfocom.swing.MigLayout;

public class MemoPanel extends JPanel {
	private JPanel listPanel;
	private JScrollPane scrollPane;
	private int userId;

	public MemoPanel(int userId) {
		this.userId = userId;
		setLayout(new BorderLayout());
		setBackground(UIManager.getColor("Panel.background"));

<<<<<<< HEAD
		JButton addButton = new JButton("+ 메모 추가");
		addButton.addActionListener(e -> openEditorAndAdd());
=======
        JButton addButton = new JButton("+ 메모 추가");
        addButton.addActionListener(e -> {
            new MemoEditorFrame("", content -> {
                int orderIndex = listPanel.getComponentCount() + 1;
                DB.insertMemo(userId, content, orderIndex);
                loadMemos();
            }).setVisible(true);
        });
>>>>>>> fa4b591b9e0f29c240e2e2884b011e27c126e6f9

		JPanel addPanel = new JPanel(new MigLayout("insets 10 20 10 20", "[grow][100!]"));
		addPanel.setBackground(getBackground());
		addPanel.add(Box.createGlue(), "growx");
		addPanel.add(addButton);

		listPanel = new JPanel(new WrapLayout(WrapLayout.CENTER, 20, 20));
		listPanel.setBackground(getBackground());

		scrollPane = new JScrollPane(listPanel);
		scrollPane.getViewport().setBackground(UIManager.getColor("Viewport.background"));
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);

		add(addPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		reloadMemos();
	}

	private void openEditorAndAdd() {
		new MemoEditorFrame(SwingUtilities.getWindowAncestor(this), "", content -> {
			int id = DB.insertMemo(userId, content, listPanel.getComponentCount() + 1);
			if (id > 0)
				reloadSingleMemoWithAnimation(id);
			else
				reloadMemos();
		}).setVisible(true);
	}

	private void reloadMemos() {
		listPanel.removeAll();
		for (Memo memo : sortedMemos()) {
			listPanel.add(new AnimatedMemoItem(memo));
		}
		listPanel.revalidate();
		listPanel.repaint();
	}

	private void reloadSingleMemoWithAnimation(int targetId) {
		reloadMemos();
		SwingUtilities.invokeLater(() -> {
			for (Component comp : listPanel.getComponents()) {
				AnimatedMemoItem item = (AnimatedMemoItem) comp;
				if (item.memo.getId() == targetId) {
					item.playEntryAnimation();
					break;
				}
			}
		});
	}

	private List<Memo> sortedMemos() {
		List<Memo> memos = DB.loadMemo(userId);
		memos.sort((m1, m2) -> {
			if (m1.isFixFlag() && !m2.isFixFlag())
				return -1;
			if (!m1.isFixFlag() && m2.isFixFlag())
				return 1;
			if (m1.isFixFlag())
				return Integer.compare(m1.getOrderIndex(), m2.getOrderIndex());
			return m2.getUpdateAt().compareTo(m1.getUpdateAt());
		});
		return memos;
	}

	class AnimatedMemoItem extends JPanel {
		private static AnimatedMemoItem activeItem;
		private final Memo memo;
		private float scale = 1f;
		private int yOffset = HEIGHT;
		private static final int WIDTH = 350, HEIGHT = 150;
		private JTextArea textArea;
		private JPanel overlay;
		private Timer entryTimer, hoverTimer;
		private final MouseAdapter hover;

		@Override
		public void updateUI() {
			super.updateUI();
			if (textArea != null) {
				textArea.setForeground(getTextColor());
			}
		}

		public AnimatedMemoItem(Memo memo) {
			this.memo = memo;
			setLayout(null);
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			setOpaque(false);

			textArea = new JTextArea(memo.getContent());
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			textArea.setOpaque(false);
			textArea.setForeground(getTextColor());
			textArea.setFont(new Font("Dialog", Font.PLAIN, 14));

			JScrollPane sp = new JScrollPane(textArea);
			sp.setOpaque(false);
			sp.getViewport().setOpaque(false);
			sp.setBorder(new EmptyBorder(0, 0, 0, 0));
			sp.setBounds(0, 0, WIDTH, HEIGHT);
			sp.addMouseWheelListener(
					e -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue()
							+ e.getWheelRotation() * scrollPane.getVerticalScrollBar().getUnitIncrement()));
			add(sp);

			overlay = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
			overlay.setOpaque(false);
			overlay.setVisible(false);
			overlay.setSize(WIDTH, 40);
			overlay.setLocation(0, HEIGHT);
			overlay.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			add(overlay);
			setComponentZOrder(overlay, 0);

			Font iconFont = new Font("Dialog", Font.PLAIN, 20);

			JToggleButton pinBtn = new JToggleButton("📌");
			pinBtn.setFont(iconFont);
			pinBtn.setSelected(memo.isFixFlag());
			pinBtn.setBorderPainted(false);
			pinBtn.setContentAreaFilled(false);
			pinBtn.setFocusable(false);
			pinBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			pinBtn.addActionListener(e -> {
				DB.toggleFixFlag(memo.getId(), pinBtn.isSelected());
				reloadSingleMemoWithAnimation(memo.getId());
			});
			overlay.add(pinBtn);

			JButton editBtn = new JButton("🖊️");
			editBtn.setFont(iconFont);
			editBtn.setBorderPainted(false);
			editBtn.setContentAreaFilled(false);
			editBtn.setFocusable(false);
			editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			editBtn.addActionListener(e -> playEdit());
			overlay.add(editBtn);

			JButton delBtn = new JButton("🗑️");
			delBtn.setFont(iconFont);
			delBtn.setBorderPainted(false);
			delBtn.setContentAreaFilled(false);
			delBtn.setFocusable(false);
			delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			delBtn.addActionListener(e -> {
				if (JOptionPane.showConfirmDialog(this, "메모를 삭제하시겠습니까?", "삭제 확인",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					playDeleteAnimation(() -> {
						DB.deleteMemo(memo.getId());
						reloadSingleMemoWithAnimation(memo.getId());
					});
				}
			});

			overlay.add(delBtn);

<<<<<<< HEAD
			overlay.setSize(WIDTH, overlay.getPreferredSize().height);
=======
        JLabel selectedLabel = new JLabel("선택한 메모:");
        JButton editButton = new JButton("수정");
        JButton deleteButton = new JButton("삭제");
>>>>>>> fa4b591b9e0f29c240e2e2884b011e27c126e6f9

			hover = new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					if (activeItem != null && activeItem != AnimatedMemoItem.this) {
						activeItem.hideOverlay();
					}
					showOverlay();
					activeItem = AnimatedMemoItem.this;
				}

<<<<<<< HEAD
				@Override
				public void mouseExited(MouseEvent e) {
					Timer exitDelay = new Timer(100, ev -> {
						Point mouse = MouseInfo.getPointerInfo().getLocation();
						SwingUtilities.convertPointFromScreen(mouse, AnimatedMemoItem.this);
						if (!new Rectangle(0, 0, getWidth(), getHeight()).contains(mouse)) {
							hideOverlay();
							activeItem = null;
						}
					});
					exitDelay.setRepeats(false);
					exitDelay.start();
				}
			};

			attachHoverRecursively(this, hover);
		}

		private void attachHoverRecursively(Component c, MouseAdapter hover) {
			c.addMouseListener(hover);
			if (c instanceof Container cont) {
				for (Component child : cont.getComponents()) {
					attachHoverRecursively(child, hover);
				}
			}
		}

		private void showOverlay() {
			if (overlay.isVisible())
				return;
			if (hoverTimer != null && hoverTimer.isRunning())
				hoverTimer.stop();

			yOffset = HEIGHT;
			overlay.setLocation(0, yOffset);
			overlay.setVisible(true);

			hoverTimer = new Timer(15, e -> {
				yOffset = Math.max(HEIGHT - overlay.getHeight() - 5, yOffset - 4);
				overlay.setLocation(0, yOffset);
				if (yOffset <= HEIGHT - overlay.getHeight() - 5)
					hoverTimer.stop();
			});
			hoverTimer.start();
		}

		private void hideOverlay() {
			if (hoverTimer != null && hoverTimer.isRunning())
				hoverTimer.stop();
			hoverTimer = new Timer(15, e -> {
				yOffset = Math.min(HEIGHT, yOffset + 4);
				overlay.setLocation(0, yOffset);
				if (yOffset >= HEIGHT) {
					hoverTimer.stop();
					overlay.setVisible(false);
				}
			});
			hoverTimer.start();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			int w = getWidth(), h = getHeight();
			g2.translate(w / 2, h / 2 + Math.round((1 - scale) * (HEIGHT / 2)));
			g2.scale(scale, scale);
			g2.translate(-w / 2, -h / 2);

			boolean dark = UIManager.getLookAndFeel().getClass().getName().contains("Darcula");
			Color paper = dark ? new Color(50, 50, 50) : new Color(255, 248, 220);
			Color border = dark ? new Color(100, 100, 100) : new Color(160, 140, 100);
			Color shadow = dark ? new Color(0, 0, 0, 100) : new Color(0, 0, 0, 50);
			Color stripe = dark ? new Color(200, 80, 80) : new Color(255, 64, 64);
			int arc = 16;
			g2.setColor(shadow);
			g2.fillRoundRect(4, 4, w, h, arc, arc);
			g2.setColor(paper);
			g2.fillRoundRect(0, 0, w, h, arc, arc);
			if (memo.isFixFlag()) {
				g2.setColor(stripe);
				g2.fillRoundRect(0, 0, 8, h, arc / 2, arc / 2);
			}
			g2.setColor(border);
			g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
			super.paintComponent(g2);
			g2.dispose();
		}

		public void playEntryAnimation() {
			scale = 0.1f;
			textArea.setVisible(false);
			entryTimer = new Timer(15, null);
			entryTimer.addActionListener(e -> {
				scale += (1f - scale) * 0.13f;
				revalidate();
				repaint();
				if (scale >= 0.995f) {
					scale = 1f;
					entryTimer.stop();
					textArea.setVisible(true);
				}
			});
			entryTimer.start();
		}

		private void detachHoverRecursively(Component c, MouseAdapter h) {
			c.removeMouseListener(h);
			if (c instanceof Container cont)
				for (Component child : cont.getComponents())
					detachHoverRecursively(child, h);
		}

		private void playDeleteAnimation(Runnable after) {
			textArea.setVisible(false);
			overlay.setVisible(false);
			activeItem = null;
			detachHoverRecursively(this, hover);
			Timer del = new Timer(15, null);
			del.addActionListener(e -> {
				scale += (0f - scale) * 0.1f;
				if (scale <= 0.05f) {
					del.stop();
					after.run();
				}
				revalidate();
				repaint();
			});
			del.start();
		}

		private Color getTextColor() {
			boolean dark = UIManager.getLookAndFeel().getClass().getName().contains("Darcula");
			return dark ? Color.WHITE : Color.DARK_GRAY;
		}

		private void playEdit() {
			overlay.setVisible(false);
			new MemoEditorFrame(SwingUtilities.getWindowAncestor(this), textArea.getText(), updated -> {
				DB.updateMemoContent(memo.getId(), updated);
				textArea.setText(updated);
				reloadSingleMemoWithAnimation(memo.getId());
			}).setVisible(true);
		}

		private void shrinkBack() {
			Timer shrink = new Timer(10, null);
			shrink.addActionListener(e -> {
				scale += (1f - scale) * 0.3f;
				if (Math.abs(scale - 1f) < 0.02f) {
					scale = 1f;
					overlay.setVisible(false);
					shrink.stop();
					repaint();
				}
				repaint();
			});
			shrink.start();
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
			Dimension m = layoutSize(target, false);
			m.width -= (getHgap() + 1);
			return m;
		}

		private Dimension layoutSize(Container target, boolean pref) {
			synchronized (target.getTreeLock()) {
				int targetWidth = target.getWidth();
				if (targetWidth == 0)
					targetWidth = Integer.MAX_VALUE;
				Insets insets = target.getInsets();
				int maxWidth = targetWidth - (insets.left + insets.right + getHgap() * 2);
				int x = 0, y = insets.top + getVgap(), rowH = 0;
				Dimension d = new Dimension(0, 0);
				for (Component c : target.getComponents()) {
					if (!c.isVisible())
						continue;
					Dimension cd = pref ? c.getPreferredSize() : c.getMinimumSize();
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
=======
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DB.deleteMemo(selectedMemo.id);
                selectedMemo = null;
                actionPanel.setVisible(false);
                loadMemos();
            }
        });
>>>>>>> fa4b591b9e0f29c240e2e2884b011e27c126e6f9

}
