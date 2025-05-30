package gui.main.panel;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
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
	private final Memo memo;
	private final JEditorPane htmlPane;
	private final JScrollPane contentScroll;
	private final JPanel buttonPanel;
	private final JButton zoomBtn, editBtn, delBtn;
	private final JToggleButton pinBtn;

	public MemoCardPanel(Memo memo, Runnable onDelete, Runnable onEdit, Runnable onPin) {
		this.memo = memo;
		setLayout(new BorderLayout());
		setOpaque(false);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setBorder(new EmptyBorder(8, 10, 8, 10));
		setBackground(UIManager.getColor("Panel.background"));

		htmlPane = new JEditorPane("text/html", renderSnippet(memo.getContent()));
		htmlPane.setEditable(false);
		htmlPane.setOpaque(false);
		htmlPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		contentScroll = new JScrollPane(htmlPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		contentScroll.setBorder(BorderFactory.createEmptyBorder());
		contentScroll.setOpaque(false);
		contentScroll.getViewport().setOpaque(false);
		contentScroll.setPreferredSize(new Dimension(WIDTH - 24, HEIGHT - 50));

		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.setOpaque(false);

		// 버튼 생성 & 설정
		zoomBtn = createButton("확대", () -> showFullMemo());
		pinBtn = createToggle("고정", memo::isFixFlag, onPin);
		editBtn = createButton("수정", onEdit);
		delBtn = createButton("삭제", onDelete);

		// 버튼들을 순회하며 아이콘 설정
		Stream.of(zoomBtn, pinBtn, editBtn, delBtn).forEach(this::setupIcon);

		buttonPanel.add(zoomBtn);
		buttonPanel.add(pinBtn);
		buttonPanel.add(editBtn);
		buttonPanel.add(delBtn);

		add(buttonPanel, BorderLayout.NORTH);
		add(contentScroll, BorderLayout.CENTER);

		// Hover 리스너
		MouseAdapter hover = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setHover(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setHover(false);
			}
		};
		Stream.of(this, htmlPane, contentScroll).forEach(c -> c.addMouseListener(hover));
	}

	private JButton createButton(String tip, Runnable action) {
		JButton btn = new JButton();
		btn.setToolTipText(tip);
		btn.setMargin(new Insets(0, 4, 0, 4));
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);
		btn.addActionListener(e -> action.run());
		return btn;
	}

	private JToggleButton createToggle(String tip, BooleanSupplier state, Runnable action) {
		JToggleButton btn = new JToggleButton();
		btn.setSelected(state.getAsBoolean());
		btn.setToolTipText(tip);
		btn.setMargin(new Insets(0, 4, 0, 4));
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);
		btn.addActionListener(e -> action.run());
		return btn;
	}

	private void setupIcon(AbstractButton btn) {
		Color c = FlatLaf.isLafDark() ? Color.WHITE : UIManager.getColor("Label.foreground");
		FontAwesome icon = switch (btn == zoomBtn ? FontAwesome.SEARCH_PLUS
				: btn == pinBtn ? FontAwesome.THUMB_TACK : btn == editBtn ? FontAwesome.PENCIL : FontAwesome.TIMES) {
		case SEARCH_PLUS -> FontAwesome.SEARCH_PLUS;
		case THUMB_TACK -> FontAwesome.THUMB_TACK;
		case PENCIL -> FontAwesome.PENCIL;
		default -> FontAwesome.TIMES;
		};
		btn.setIcon(IconFontSwing.buildIcon(icon, 16f, c));
		if (btn instanceof JToggleButton)
			((JToggleButton) btn).setSelectedIcon(IconFontSwing.buildIcon(icon, 16f, c));
	}

	private void setHover(boolean hover) {
		setCursor(hover ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
		repaint();
	}

	private String renderSnippet(String html) {
		String txt = html == null ? "" : html.replaceAll("<[^>]+>", "").trim();
		if (txt.length() > 100)
			txt = txt.substring(0, 100) + "…";
		return "<html><body style='font-family:Malgun Gothic; font-size:12px;'>" + txt + "</body></html>";
	}

	private void showFullMemo() {
		JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "메모 상세",
				Dialog.ModalityType.APPLICATION_MODAL);
		JEditorPane full = new JEditorPane("text/html", memo.getContent());
		full.setEditable(false);
		full.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		dlg.add(new JScrollPane(full));
		dlg.setSize(600, 400);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		var g2 = (Graphics2D) g.create();
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
		setAlpha(0f);
		Timeline t = new Timeline(this);
		t.addPropertyToInterpolate("alpha", 0f, 1f);
		t.setDuration(350);
		t.play();
	}

	public void playDeleteAnimation(Runnable onFinished) {
		Timeline t = new Timeline(this);
		t.addPropertyToInterpolate("alpha", 1f, 0f);
		t.setDuration(350);
		t.addCallback(new TimelineCallback() {
			@Override
			public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float f, float f2) {
				if (newState == TimelineState.DONE && onFinished != null) {
					onFinished.run();
				}
			}

			@Override
			public void onTimelinePulse(float f, float f2) {
			}
		});
		t.play();
	}

}
