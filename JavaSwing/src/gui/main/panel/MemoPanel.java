package gui.main.panel;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.pushingpixels.radiance.animation.api.Timeline;
import org.pushingpixels.radiance.animation.api.Timeline.TimelineState;
import org.pushingpixels.radiance.animation.api.callback.TimelineCallbackAdapter;

import api.model.Memo;
import api.model.MemoDAO;
import gui.main.SwingHtmlEditorWithImage;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

public class MemoPanel extends JPanel {

	/* ───────── 인스턴스 필드 ───────── */
	private final MemoDAO dao = new MemoDAO();
	private final int userId;
	private final JPanel header, listPanel;
	private final JScrollPane scroll;
	private boolean expanding = false;

	public MemoPanel(int userId) {
		this.userId = userId;
		setLayout(new BorderLayout());
		setBackground(UIManager.getColor("Panel.background"));

		/* 상단 ‘+ 메모 추가’ 버튼 */
		JButton add = new JButton("+ 메모 추가");
		add.addActionListener(e -> addMemo());

		header = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		header.setBackground(getBackground());
		header.add(add);
		add(header, BorderLayout.NORTH);

		/* 카드 목록 (WrapLayout) */
		listPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
		listPanel.setOpaque(false);

		scroll = new JScrollPane(listPanel);
		scroll.getViewport().setBackground(UIManager.getColor("Viewport.background"));
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		add(scroll, BorderLayout.CENTER);

		reloadMemos();
	}

	/* ───────── CRUD ───────── */
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
							int id = dao.insertMemo(m);
							if (id > 0)
								reloadSingle(id);
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
								dao.updateMemo(memo);
							} catch (Exception e) {
								e.printStackTrace();
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
		/* 카드 스냅샷 → GhostPanel */
		JComponent glass = (JComponent) SwingUtilities.getRootPane(this).getGlassPane();
		glass.setLayout(null);
		Rectangle start = SwingUtilities.convertRectangle(card.getParent(), card.getBounds(), glass);

		BufferedImage img = new BufferedImage(card.getWidth(), card.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		card.paint(g2);
		g2.dispose();

		GhostPanel ghost = new GhostPanel(img);
		ghost.setBounds(start);
		glass.add(ghost);
		glass.setVisible(true);

		listPanel.remove(card);
		listPanel.revalidate();
		listPanel.repaint();

		Timeline.builder(ghost).addPropertyToInterpolate("alpha", 1f, 0f)
				.addPropertyToInterpolate("y", start.y, start.y + 20).setDuration(350)
				.addCallback(new TimelineCallbackAdapter() {
					@Override
					public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float v1,
							float v2) {
						if (newState == TimelineState.DONE) {
							glass.remove(ghost);
							if (glass.getComponentCount() == 0)
								glass.setVisible(false);
							try {
								dao.deleteMemo(memo.getId());
							} catch (Exception e) {
								e.printStackTrace();
							}
							reloadMemos();
						}
					}
				}).build().play();
	}

	/* ───────── 목록 로딩 ───────── */
	private void reloadMemos() {
		listPanel.removeAll();
		List<Memo> memos;
		try {
			memos = dao.selectMemosByUser(userId);
		} catch (Exception ex) {
			ex.printStackTrace();
			memos = List.of();
		}

		for (Memo m : memos) {
			MemoCardPanel[] ref = new MemoCardPanel[1];
			ref[0] = new MemoCardPanel(m, () -> deleteMemo(m, ref[0]), () -> editMemo(m), () -> {
				m.setFixFlag(!m.isFixFlag());
				try {
					dao.updateMemo(m);
				} catch (Exception e) {
					e.printStackTrace();
				}
				reloadMemos();
			}, () -> expandMemo(ref[0]));
			listPanel.add(ref[0]);
		}
		listPanel.revalidate();
		listPanel.repaint();
	}

	private void reloadSingle(int id) {
		reloadMemos();
		SwingUtilities.invokeLater(() -> {
			for (Component c : listPanel.getComponents()) {
				MemoCardPanel mc = (MemoCardPanel) c;
				if (mc.getMemo().getId() == id) {
					mc.playEntryAnimation();
					break;
				}
			}
		});
	}

	/* ───────── 카드 확대 ───────── */
	public void expandMemo(MemoCardPanel card) {
		if (expanding)
			return;
		expanding = true;

		listPanel.setVisible(false);

		JComponent glass = (JComponent) SwingUtilities.getRootPane(this).getGlassPane();
		glass.setLayout(null);
		glass.setVisible(true);

		Dimension gp = glass.getSize();
		Rectangle start = SwingUtilities.convertRectangle(card.getParent(), card.getBounds(), glass);

		Rectangle hdr = SwingUtilities.convertRectangle(header.getParent(), header.getBounds(), glass);
		int safeTop = hdr.y + hdr.height + 8;
		Rectangle end = new Rectangle(80, safeTop, gp.width - 160, gp.height - safeTop - 60);

		final FullViewPanel[] holder = new FullViewPanel[1];
		Memo m = card.getMemo();

		holder[0] = new FullViewPanel(m, () -> {
			m.setFixFlag(!m.isFixFlag());
			try {
				dao.updateMemo(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
			reloadMemos();
		}, () -> editMemo(m), () -> {
			Timeline.builder(holder[0]).addPropertyToInterpolate("alpha", 1f, 0f)
					.addPropertyToInterpolate("animBounds", end, start).setDuration(300)
					.addCallback(new TimelineCallbackAdapter() {
						@Override
						public void onTimelineStateChanged(TimelineState o, TimelineState n, float a, float b) {
							if (n == TimelineState.DONE) {
								glass.remove(holder[0]);
								glass.setVisible(false);
								listPanel.setVisible(true);
								expanding = false;
							}
						}
					}).build().play();
		});

		holder[0].setAnimBounds(start);
		glass.add(holder[0]);

		Timeline.builder(holder[0]).addPropertyToInterpolate("alpha", 0f, 1f)
				.addPropertyToInterpolate("animBounds", start, end).setDuration(350).build().play();
	}

	/* ───────── GhostPanel ───────── */
	private static class GhostPanel extends JComponent {
		private final BufferedImage img;
		private float alpha = 1f;

		public GhostPanel(BufferedImage img) {
			this.img = img;
		}

		public void setAlpha(float a) {
			alpha = a;
			repaint();
		}

		public float getAlpha() {
			return alpha;
		}

		public void setY(int y) {
			setLocation(getX(), y);
		}

		public int getY() {
			return super.getY();
		}

		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
			g2.drawImage(img, 0, 0, null);
			g2.dispose();
		}
	}

	/* ───────── FullViewPanel ───────── */

	public static class FullViewPanel extends JPanel {
		private float alpha = 0f;

		/* 툴바 버튼들을 필드로 보관 → 테마 바뀔 때 아이콘 교체 */
		private final JToggleButton pinBtn;
		private final JButton editBtn;
		private final JButton closeBtn;
		private final JScrollPane scrollPane;
		private final JEditorPane html;

		public FullViewPanel(Memo memo, Runnable onPin, Runnable onEdit, Runnable onClose) {

			setLayout(new BorderLayout());
			setOpaque(true);

			/* ───── 툴바 ───── */
			JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
			bar.setOpaque(false);

			pinBtn = mkToggle(FontAwesome.THUMB_TACK, memo.isFixFlag(), onPin);
			editBtn = mkButton(FontAwesome.PENCIL, onEdit);
			closeBtn = mkButton(FontAwesome.TIMES, onClose);

			bar.add(pinBtn);
			bar.add(editBtn);
			bar.add(closeBtn);
			add(bar, BorderLayout.NORTH);

			/* ───── 본문 ───── */
			html = new JEditorPane("text/html", memo.getContent());
			html.setEditable(false);
			html.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

			scrollPane = new JScrollPane(html);
			add(scrollPane, BorderLayout.CENTER);

			/* 첫 테마 적용 */
			applyTheme();
		}

		private JButton mkButton(FontAwesome ico, Runnable act) {
			JButton b = new JButton();
			b.setBorderPainted(false);
			b.setContentAreaFilled(false);
			b.addActionListener(e -> act.run());
			return b;
		}

		private JToggleButton mkToggle(FontAwesome ico, boolean sel, Runnable act) {
			JToggleButton t = new JToggleButton();
			t.setSelected(sel);
			t.setBorderPainted(false);
			t.setContentAreaFilled(false);
			t.addActionListener(e -> act.run());
			return t;
		}

		private void applyTheme() {
			boolean dark = UIManager.getLookAndFeel().getName().toLowerCase().contains("dark");

			/* 배경 / 보더 */
			Color bg = dark ? new Color(0x424140) : Color.WHITE;
			Color border = dark ? new Color(0x444444) : new Color(0xCCCCCC);

			html.setBackground(bg);
			setBorder(new CompoundBorder(new LineBorder(border, 2, true), new EmptyBorder(16, 16, 16, 16)));

			/* 스크롤 뷰포트 배경도 동일하게 */
			scrollPane.getViewport().setBackground(bg);

			/* 아이콘(강조색) */
			Color accent = dark ? new Color(0x8AB4F8) : new Color(0x0A84FF);

			pinBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.THUMB_TACK, 18, accent));
			pinBtn.setSelectedIcon(pinBtn.getIcon());

			editBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.PENCIL, 18, accent));
			closeBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.TIMES, 18, accent));
		}

		@Override
		public void updateUI() {
			super.updateUI();
			if (scrollPane != null) {
				applyTheme();
			}
		}

		public Rectangle getAnimBounds() {
			return getBounds();
		}

		public void setAnimBounds(Rectangle r) {
			setBounds(r);
			revalidate();
			repaint();
		}

		public float getAlpha() {
			return alpha;
		}

		public void setAlpha(float a) {
			alpha = a;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
			super.paintComponent(g2);
			g2.dispose();
		}

	}/* ───────── WrapLayout (변동 없음) ───────── */

	public static class WrapLayout extends FlowLayout {
		public WrapLayout(int align, int hg, int vg) {
			super(align, hg, vg);
		}

		public Dimension preferredLayoutSize(Container t) {
			return layoutSize(t, true);
		}

		public Dimension minimumLayoutSize(Container t) {
			return layoutSize(t, false);
		}

		private Dimension layoutSize(Container t, boolean pref) {
			synchronized (t.getTreeLock()) {
				int maxW = (t.getWidth() > 0 ? t.getWidth() : Integer.MAX_VALUE)
						- (t.getInsets().left + t.getInsets().right + getHgap() * 2);
				int x = 0, y = getVgap(), rowH = 0;
				Dimension d = new Dimension(0, 0);
				for (Component c : t.getComponents()) {
					if (!c.isVisible())
						continue;
					Dimension cd = pref ? c.getPreferredSize() : c.getMinimumSize();
					if (x == 0 || x + cd.width <= maxW) {
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
