package gui.main.panel;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.callback.TimelineCallback;

import com.formdev.flatlaf.FlatLaf;

import api.model.Memo;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

/**
 * 한 장짜리 메모 카드.
 * <p>
 * 버튼 아이콘은 Font Awesome + IconFontSwing 사용. 라이트/다크 테마 변경 시
 * {@link #updateThemeUI()}를 호출해 아이콘 색을 갱신한다.
 */
public class MemoCardPanel extends JPanel {

	/* ---------- 크기 및 상태 ---------- */
	private static final int WIDTH = 170;
	private static final int HEIGHT = 150;
	private float alpha = 1.0f;

	/* ---------- 데이터 ---------- */
	private final Memo memo;

	/* ---------- 콜백 ---------- */
	private Runnable onCardClick;

	/* ---------- UI 컴포넌트 ---------- */
	private final JEditorPane contentPane;
	private final JPanel buttonPanel;
	private final JScrollPane contentScroll;
	private Color hoverBg = null;

	/* FontAwesome 버튼 */
	private final JToggleButton pinBtn;
	private final JButton editBtn;
	private final JButton delBtn;

	public MemoCardPanel(Memo memo, Runnable onDelete, Runnable onEdit, Runnable onPin) {
		this.memo = memo;

		/* Font Awesome 등록 (앱 전체에서 한 번만 호출돼도 문제 없음) */
		IconFontSwing.register(FontAwesome.getIconFont());

		/* ----- 패널 기본 설정 ----- */
		setOpaque(false);
		setBackground(UIManager.getColor("Panel.background"));
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(120, 60));
		setBorder(new EmptyBorder(8, 10, 8, 10));

		/* ----- 내용 영역 (HTML 에디터로 변경) ----- */
		contentPane = new JEditorPane();
		contentPane.setEditable(false);
		contentPane.setOpaque(false);
		contentPane.setContentType("text/html");

		// HTML 스타일 설정
		HTMLEditorKit kit = new HTMLEditorKit();
		contentPane.setEditorKit(kit);
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet
				.addRule("body { font-family: 'Malgun Gothic', sans-serif; font-size: 12px; margin: 0; padding: 0; }");
		styleSheet.addRule("p { margin: 0; padding: 0; }");

		// 내용 설정
		String content = memo.getContent();
		if (content == null || content.isBlank()) {
			content = "<html><body></body></html>";
		} else if (!content.toLowerCase().contains("<html")) {
			// 일반 텍스트인 경우 HTML로 변환
			content = "<html><body>" + content.replace("\n", "<br>") + "</body></html>";
		}
		contentPane.setText(content);

		// 배경색 투명하게 설정
		contentPane.setBackground(new Color(0, 0, 0, 0));
		contentPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		contentPane.setForeground(UIManager.getColor("Label.foreground"));
		contentPane.setBorder(null);
		contentPane.setFocusable(false);

		contentScroll = new JScrollPane(contentPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		contentScroll.setBorder(BorderFactory.createEmptyBorder());
		contentScroll.getViewport().setBorder(null);
		contentScroll.setOpaque(false);
		contentScroll.getViewport().setOpaque(false);
		contentScroll.setBackground(new Color(0, 0, 0, 0));
		contentScroll.getViewport().setBackground(new Color(0, 0, 0, 0));
		contentScroll.setPreferredSize(new Dimension(WIDTH - 24, HEIGHT - 50));

		/* ----- 버튼 영역 ----- */
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.setOpaque(false);
		buttonPanel.setBorder(null);

		/* Toggle : 고정 */
		pinBtn = new JToggleButton();
		styleIconButton(pinBtn);
		pinBtn.setSelected(memo.isFixFlag());
		pinBtn.setToolTipText("고정");
		pinBtn.addActionListener(e -> {
			onPin.run();
			updateIcons();
		});
		buttonPanel.add(pinBtn);

		/* 수정 */
		editBtn = new JButton();
		styleIconButton(editBtn);
		editBtn.setToolTipText("수정");
		editBtn.addActionListener(e -> onEdit.run());
		buttonPanel.add(editBtn);

		/* 삭제 */
		delBtn = new JButton();
		styleIconButton(delBtn);
		delBtn.setToolTipText("삭제");
		delBtn.addActionListener(e -> onDelete.run());
		buttonPanel.add(delBtn);

		add(buttonPanel, BorderLayout.NORTH);
		add(contentScroll, BorderLayout.CENTER);

		/* 첫 아이콘 세팅 */
		updateIcons();

		/* ----- Hover 효과 ----- */
		setupHoverEffect();

		/* ----- 클릭 이벤트 ----- */
		setupClickEvent();
	}

	/*
	 * ======================================================================= UI 헬퍼
	 * =====================================================================
	 */
	private void styleIconButton(javax.swing.AbstractButton btn) {
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);
		btn.setFocusPainted(false);
		btn.setOpaque(false);
		btn.setPreferredSize(new Dimension(24, 24));
	}

	private void updateIcons() {
		Color iconColor = FlatLaf.isLafDark() ? Color.WHITE : UIManager.getColor("Label.foreground");
		int size = 14;

		pinBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.THUMB_TACK, size, iconColor));
		pinBtn.setSelectedIcon(IconFontSwing.buildIcon(FontAwesome.THUMB_TACK, size, iconColor));

		editBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.PENCIL, size, iconColor));
		delBtn.setIcon(IconFontSwing.buildIcon(FontAwesome.TRASH, size, iconColor));
	}

	/**
	 * 테마 전환 후 호출하여 아이콘 색상을 갱신한다.
	 */
	public void updateThemeUI() {
		SwingUtilities.updateComponentTreeUI(this);
		updateIcons();

		// HTML 에디터 스타일 업데이트
		HTMLEditorKit kit = (HTMLEditorKit) contentPane.getEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("body { color: " + colorToHex(UIManager.getColor("Label.foreground")) + "; }");
		contentPane.setForeground(UIManager.getColor("Label.foreground"));
		contentPane.revalidate();
		contentPane.repaint();
	}

	private String colorToHex(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	/* Hover 효과 및 이벤트 위임 ------------------------------------------ */
	private void setupHoverEffect() {
		MouseAdapter hoverAdapter = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hoverBg = getLighterColor();
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				PointerInfo pi = MouseInfo.getPointerInfo();
				if (pi != null) {
					Point pt = pi.getLocation();
					SwingUtilities.convertPointFromScreen(pt, MemoCardPanel.this);
					if (!MemoCardPanel.this.contains(pt)) {
						hoverBg = null;
						setCursor(Cursor.getDefaultCursor());
						repaint();
					}
				} else {
					hoverBg = null;
					setCursor(Cursor.getDefaultCursor());
					repaint();
				}
			}
		};
		addMouseListener(hoverAdapter);

		/* 내부 컴포넌트에도 동일 hover 위임 */
		MouseAdapter delegateHover = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				MemoCardPanel.this
						.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, MemoCardPanel.this));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				MemoCardPanel.this
						.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, MemoCardPanel.this));
			}
		};

		contentPane.addMouseListener(delegateHover);
		buttonPanel.addMouseListener(delegateHover);
		contentScroll.addMouseListener(delegateHover);
		contentScroll.getViewport().addMouseListener(delegateHover);
		contentScroll.getVerticalScrollBar().addMouseListener(delegateHover);
		contentScroll.getHorizontalScrollBar().addMouseListener(delegateHover);
		for (Component c : buttonPanel.getComponents())
			c.addMouseListener(delegateHover);
	}

	/* 클릭 이벤트 설정 */
	private void setupClickEvent() {
		MouseAdapter clickAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 버튼 영역 클릭은 무시 (버튼 자체의 이벤트 처리)
				if (isClickOnButtons(e.getPoint())) {
					return;
				}

				// 카드 클릭 이벤트 처리
				if (onCardClick != null) {
					onCardClick.run();
				}
			}
		};

		addMouseListener(clickAdapter);
		contentPane.addMouseListener(clickAdapter);
		contentScroll.addMouseListener(clickAdapter);
		contentScroll.getViewport().addMouseListener(clickAdapter);
	}

	/* 클릭 위치가 버튼 영역인지 확인 */
	private boolean isClickOnButtons(Point point) {
		// 버튼 패널 영역 내 클릭인지 확인
		Point buttonPanelPoint = SwingUtilities.convertPoint(this, point, buttonPanel);
		if (buttonPanel.contains(buttonPanelPoint)) {
			return true;
		}

		// 개별 버튼 영역 내 클릭인지 확인
		for (Component c : buttonPanel.getComponents()) {
			Point buttonPoint = SwingUtilities.convertPoint(this, point, c);
			if (c.contains(buttonPoint)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 카드 클릭 이벤트 설정
	 */
	public void setOnCardClick(Runnable onCardClick) {
		this.onCardClick = onCardClick;
	}

	/*
	 * ======================================================================= 애니메이션
	 * =====================================================================
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
		repaint();
	}

	public float getAlpha() {
		return alpha;
	}

	/** 카드 등장 애니메이션 */
	public void playEntryAnimation() {
		setAlpha(0f);
		Timeline timeline = new Timeline(this);
		timeline.addPropertyToInterpolate("alpha", 0.0f, 1.0f);
		timeline.setDuration(350);
		timeline.play();
	}

	/** 카드 삭제 전 사라지는 애니메이션 */
	public void playDeleteAnimation(Runnable onFinished) {
		Timeline timeline = new Timeline(this);
		timeline.addPropertyToInterpolate("alpha", 1.0f, 0.0f);
		timeline.setDuration(350);
		timeline.addCallback(new TimelineCallback() {
			@Override
			public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float durationFraction,
					float timelinePosition) {
				if (newState == TimelineState.DONE && onFinished != null)
					onFinished.run();
			}

			@Override
			public void onTimelinePulse(float f, float g) {
			}
		});
		timeline.play();
	}

	/*
	 * ======================================================================= 렌더링
	 * =====================================================================
	 */
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		int arc = 12;

		/* 투명도(애니메이션) */
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		/* 그림자 */
		g2.setColor(FlatLaf.isLafDark() ? new Color(0, 0, 0, 50) : new Color(0, 0, 0, 30));
		g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, arc, arc);

		/* 배경 (hover 시 밝기 ↑) */
		Color bg = hoverBg != null ? hoverBg : getBackground();
		g2.setColor(bg);
		g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);

		/* 고정 표시 */
		if (memo.isFixFlag()) {
			g2.setColor(new Color(255, 216, 80, 160));
			g2.fillRoundRect(0, 0, 8, getHeight() - 6, arc / 2, arc / 2);
		}

		/* 테두리 */
		Color border = UIManager.getColor("Component.borderColor") != null ? UIManager.getColor("Component.borderColor")
				: FlatLaf.isLafDark() ? new Color(70, 70, 70) : new Color(200, 180, 80);
		g2.setColor(border);
		g2.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, arc, arc);
		g2.dispose();
	}

	private Color getLighterColor() {
		Color c = getBackground() != null ? getBackground() : UIManager.getColor("Panel.background");
		int r = Math.min(255, c.getRed() + 18);
		int g = Math.min(255, c.getGreen() + 18);
		int b = Math.min(255, c.getBlue() + 18);
		return new Color(r, g, b);
	}

	/*
	 * =======================================================================
	 * Getter =====================================================================
	 */
	public Memo getMemo() {
		return memo;
	}
}
