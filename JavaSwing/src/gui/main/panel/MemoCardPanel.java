package gui.main.panel;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.callback.TimelineCallback;

import com.formdev.flatlaf.FlatLaf;

import api.model.Memo;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

public class MemoCardPanel extends JPanel {

	private static final int WIDTH = 170, HEIGHT = 150;
	private float alpha = 1f;
	private boolean hover = false;
	private final Memo memo;
	private final JScrollPane contentScroll;
	private final JPanel buttonPanel;
	private final JButton editBtn, delBtn;
	private final JToggleButton pinBtn;
	private final Runnable onExpand;

	public MemoCardPanel(Memo memo, Runnable onDelete, Runnable onEdit, Runnable onPin, Runnable onExpand) {
		this.memo = memo;
		this.onExpand = onExpand;

		setLayout(new BorderLayout());
		setOpaque(false);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setBorder(new EmptyBorder(8, 10, 8, 10));
		setBackground(UIManager.getColor("Panel.background"));

		JEditorPane html = new JEditorPane("text/html", renderSnippet(memo.getContent()));
		html.setEditable(false);
		html.setOpaque(false);
		html.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		contentScroll = new JScrollPane(html, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		contentScroll.setBorder(BorderFactory.createEmptyBorder());
		contentScroll.setOpaque(false);
		contentScroll.getViewport().setOpaque(false);
		contentScroll.setPreferredSize(new Dimension(WIDTH - 24, HEIGHT - 50));

		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.setOpaque(false);

		pinBtn = createToggle(memo::isFixFlag, onPin);
		editBtn = createButton(onEdit);
		delBtn = createButton(onDelete);

		Stream.of(pinBtn, editBtn, delBtn).forEach(this::setupIcon);

		buttonPanel.add(pinBtn);
		buttonPanel.add(editBtn);
		buttonPanel.add(delBtn);

		add(buttonPanel, BorderLayout.NORTH);
		add(contentScroll, BorderLayout.CENTER);

		addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				hover = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				repaint();
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				hover = false;
				setCursor(Cursor.getDefaultCursor());
				repaint();
			}

			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				java.awt.Component c = SwingUtilities.getDeepestComponentAt(MemoCardPanel.this, e.getX(), e.getY());
				if (!SwingUtilities.isDescendingFrom(c, buttonPanel)) {
					onExpand.run();
				}
			}
		});

	}

	private JButton createButton(Runnable action) {
		JButton b = new JButton();
		b.setMargin(new Insets(0, 4, 0, 4));
		b.setBorderPainted(false);
		b.setContentAreaFilled(false);
		b.setFocusable(false);
		b.setRolloverEnabled(false);
		b.addActionListener(e -> action.run());
		return b;
	}

	private JToggleButton createToggle(BooleanSupplier state, Runnable action) {
		JToggleButton t = new JToggleButton();
		t.setSelected(state.getAsBoolean());
		t.setMargin(new Insets(0, 4, 0, 4));
		t.setBorderPainted(false);
		t.setContentAreaFilled(false);
		t.setFocusable(false);
		t.setRolloverEnabled(false);
		t.addActionListener(e -> action.run());
		return t;
	}

	private void setupIcon(AbstractButton btn) {
		boolean dark = FlatLaf.isLafDark();
		Color accent = dark ? new Color(0x8AB4F8) : new Color(0x0A84FF);
		FontAwesome fa = btn == pinBtn ? FontAwesome.THUMB_TACK
				: btn == editBtn ? FontAwesome.PENCIL : FontAwesome.TIMES;
		btn.setIcon(IconFontSwing.buildIcon(fa, 16, accent));
		if (btn instanceof JToggleButton t)
			t.setSelectedIcon(btn.getIcon());
	}

	private String renderSnippet(String html) {
		String t = html == null ? "" : html.replaceAll("<[^>]+>", "").trim();
		if (t.length() > 100)
			t = t.substring(0, 100) + "…";
		return "<html><body style='font-family:Malgun Gothic; font-size:12px;'>" + t + "</body></html>";
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		int arc = 12;
		g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
		g2.setColor(FlatLaf.isLafDark() ? new Color(0, 0, 0, 50) : new Color(0, 0, 0, 30));
		g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, arc, arc);
		g2.setColor(getBackground());
		g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);
		if (memo.isFixFlag()) {
			g2.setColor(new Color(255, 216, 80, 160));
			g2.fillRoundRect(0, 0, 8, getHeight() - 6, arc / 2, arc / 2);
		}
		if (hover) {
			g2.setColor(new Color(255, 255, 255, 60));
			g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);
		}
		Color border = UIManager.getColor("Component.borderColor");
		if (border == null)
			border = FlatLaf.isLafDark() ? new Color(70, 70, 70) : new Color(200, 180, 80);
		g2.setColor(border);
		g2.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, arc, arc);
		g2.dispose();
	}

	public void setAlpha(float a) {
		alpha = a;
		repaint();
	}

	public float getAlpha() {
		return alpha;
	}

	public Memo getMemo() {
		return memo;
	}

	public void playEntryAnimation() {
		setAlpha(0);
		Timeline t = new Timeline(this);
		t.addPropertyToInterpolate("alpha", 0f, 1f);
		t.setDuration(350);
		t.play();
	}

	public void playDeleteAnimation(Runnable fin) {
		Timeline t = new Timeline(this);
		t.addPropertyToInterpolate("alpha", 1f, 0f);
		t.setDuration(350);
		t.addCallback(new TimelineCallback() {
			public void onTimelineStateChanged(TimelineState o, TimelineState n, float f1, float f2) {
				if (n == TimelineState.DONE && fin != null)
					fin.run();
			}

			public void onTimelinePulse(float f1, float f2) {
			}
		});
		t.play();
	}

	public void updateUI() {
		super.updateUI();
		if (pinBtn != null) {
			Stream.of(pinBtn, editBtn, delBtn).forEach(this::setupIcon);
			repaint();
		}
	}

	// GhostPanel and WrapLayout reused
	public static class GhostPanel extends JComponent {
		private final java.awt.image.BufferedImage img;
		private float alpha = 1f;

		public GhostPanel(java.awt.image.BufferedImage img) {
			this.img = img;
		}

		public void setAlpha(float a) {
			alpha = a;
			repaint();
		}

		public float getAlpha() {
			return alpha;
		}

		public void setAnimY(int y) {
			setLocation(getX(), y);
		}

		public int getAnimY() {
			return getY();
		}

		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
			g2.drawImage(img, 0, 0, null);
			g2.dispose();
		}
	}

	public static class FullViewPanel extends JPanel {
		private float alpha = 0f;
		private final Memo memo;
		private final JToggleButton pinBtn;
		private final JButton editBtn;
		private final JButton closeBtn;
		private final JScrollPane scrollPane;
		private final JEditorPane html;

		public FullViewPanel(Memo memo, Runnable onPin, Runnable onEdit, Runnable onClose) {
			this.memo = memo;
			setLayout(new BorderLayout());
			setOpaque(true);

			JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
			bar.setOpaque(false);

			pinBtn = mkToggle(onPin);
			editBtn = mkButton(onEdit);
			closeBtn = mkButton(onClose);

			bar.add(pinBtn);
			bar.add(editBtn);
			bar.add(closeBtn);
			add(bar, BorderLayout.NORTH);

			html = new JEditorPane("text/html", memo.getContent());
			html.setEditable(false);
			html.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

			scrollPane = new JScrollPane(html);
			add(scrollPane, BorderLayout.CENTER);

			applyTheme();
		}

		private JButton mkButton(Runnable act) {
			JButton b = new JButton();
			b.setBorderPainted(false);
			b.setContentAreaFilled(false);
			b.setFocusable(false);
			b.setRolloverEnabled(false);
			b.addActionListener(e -> act.run());
			return b;
		}

		private JToggleButton mkToggle(Runnable act) {
			JToggleButton t = new JToggleButton();
			t.setSelected(memo.isFixFlag());
			t.setBorderPainted(false);
			t.setContentAreaFilled(false);
			t.setFocusable(false);
			t.setRolloverEnabled(false);
			t.addActionListener(e -> act.run());
			return t;
		}

		private void applyTheme() {
			boolean dark = UIManager.getLookAndFeel().getName().toLowerCase().contains("dark");
			Color bg = dark ? new Color(0x424140) : Color.WHITE;
			Color border = dark ? new Color(0x444444) : new Color(0xCCCCCC);
			html.setBackground(bg);
			setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.LineBorder(border, 2, true),
					new EmptyBorder(16, 16, 16, 16)));
			scrollPane.getViewport().setBackground(bg);
			Color accent = dark ? new Color(0x8AB4F8) : new Color(0x0A84FF);
			pinBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.THUMB_TACK, 18, accent));
			pinBtn.setSelectedIcon(pinBtn.getIcon());
			editBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.PENCIL, 18, accent));
			closeBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.TIMES, 18, accent));
		}

		public void updateUI() {
			super.updateUI();
			if (scrollPane != null)
				applyTheme();
		}

		public void refreshContent() {
			html.setText(memo.getContent());
		}

		public float getAlpha() {
			return alpha;
		}

		public void setAlpha(float a) {
			alpha = a;
			repaint();
		}
	}

	public static class WrapLayout extends FlowLayout {
		public WrapLayout(int align, int hg, int vg) {
			super(align, hg, vg);
		}

		public Dimension preferredLayoutSize(java.awt.Container t) {
			return layoutSize(t, true);
		}

		public Dimension minimumLayoutSize(java.awt.Container t) {
			return layoutSize(t, false);
		}

		private Dimension layoutSize(java.awt.Container t, boolean pref) {
			synchronized (t.getTreeLock()) {
				int maxW = (t.getWidth() > 0 ? t.getWidth() : Integer.MAX_VALUE)
						- (t.getInsets().left + t.getInsets().right + getHgap() * 2);
				int x = 0, y = getVgap(), rowH = 0;
				Dimension d = new Dimension(0, 0);
				for (java.awt.Component c : t.getComponents()) {
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
