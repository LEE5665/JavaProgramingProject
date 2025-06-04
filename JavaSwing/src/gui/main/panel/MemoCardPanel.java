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
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
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
	private final Memo memo;
	private final JScrollPane contentScroll;
	private final JPanel buttonPanel;
	private final JButton zoomBtn, editBtn, delBtn;
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

		zoomBtn = createButton(() -> onExpand.run());
		pinBtn = createToggle(memo::isFixFlag, onPin);
		editBtn = createButton(onEdit);
		delBtn = createButton(onDelete);

		Stream.of(zoomBtn, pinBtn, editBtn, delBtn).forEach(this::setupIcon);

		buttonPanel.add(zoomBtn);
		buttonPanel.add(pinBtn);
		buttonPanel.add(editBtn);
		buttonPanel.add(delBtn);

		add(buttonPanel, BorderLayout.NORTH);
		add(contentScroll, BorderLayout.CENTER);

		addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent e) {
				setHover(true);
			}

			public void mouseExited(java.awt.event.MouseEvent e) {
				setHover(false);
			}
		});
	}

	private JButton createButton(Runnable action) {
		JButton b = new JButton();
		b.setMargin(new Insets(0, 4, 0, 4));
		b.setBorderPainted(false);
		b.setContentAreaFilled(false);
		b.addActionListener(e -> action.run());
		return b;
	}

	private JToggleButton createToggle(BooleanSupplier state, Runnable action) {
		JToggleButton t = new JToggleButton();
		t.setSelected(state.getAsBoolean());
		t.setMargin(new Insets(0, 4, 0, 4));
		t.setBorderPainted(false);
		t.setContentAreaFilled(false);
		t.addActionListener(e -> action.run());
		return t;
	}

	private void setupIcon(AbstractButton btn) {
		boolean dark = FlatLaf.isLafDark();
		Color accent = dark ? new Color(0x8AB4F8) // 다크 모드용
				: new Color(0x0A84FF); // 라이트 모드용

		FontAwesome fa = switch (btn) {
		case JButton b when b == zoomBtn -> FontAwesome.SEARCH_PLUS;
		case JToggleButton t when t == pinBtn -> FontAwesome.THUMB_TACK;
		case JButton b when b == editBtn -> FontAwesome.PENCIL;
		default -> FontAwesome.TIMES;
		};

		btn.setIcon(IconFontSwing.buildIcon(fa, 16, accent));
		if (btn instanceof JToggleButton t)
			t.setSelectedIcon(IconFontSwing.buildIcon(fa, 16, accent));
	}

	private void setHover(boolean h) {
		setCursor(h ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
		repaint();
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

	@Override
	public void updateUI() {
		super.updateUI();
		if (zoomBtn != null) {
			Stream.of(zoomBtn, pinBtn, editBtn, delBtn).forEach(this::setupIcon);
			repaint();
		}
	}

}
