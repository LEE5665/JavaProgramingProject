package gui.main.panel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
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

public class MemoCardPanel extends JPanel {

    private static final int WIDTH = 170;
    private static final int HEIGHT = 150;
    private float alpha = 1.0f;

    private final Memo memo;
    private Runnable onCardClick;

    private final JEditorPane contentPane;
    private final JPanel buttonPanel;
    private final JScrollPane contentScroll;
    private Color hoverBg = null;

    private final JToggleButton pinBtn;
    private final JButton editBtn;
    private final JButton delBtn;

    public MemoCardPanel(Memo memo, Runnable onDelete, Runnable onEdit, Runnable onPin) {
        this.memo = memo;
        IconFontSwing.register(FontAwesome.getIconFont());

        setOpaque(false);
        setBackground(UIManager.getColor("Panel.background"));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBorder(new EmptyBorder(8, 10, 8, 10));

        // 에디터
        contentPane = new JEditorPane();
        contentPane.setEditable(false);
        contentPane.setOpaque(false);
        contentPane.setContentType("text/html");
        contentPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        contentPane.setForeground(UIManager.getColor("Label.foreground"));
        contentPane.setFocusable(false);

        HTMLEditorKit kit = new HTMLEditorKit();
        contentPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: 'Malgun Gothic'; font-size: 12px; margin: 0; padding: 0; }");
        styleSheet.addRule("p { margin: 0; padding: 0; }");

        String content = memo.getContent();
        if (content == null || content.isBlank()) {
            content = "<html><body></body></html>";
        } else if (!content.toLowerCase().contains("<html")) {
            content = "<html><body>" + content.replace("\n", "<br>") + "</body></html>";
        }
        contentPane.setText(content);

        contentScroll = new JScrollPane(contentPane);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setOpaque(false);
        contentScroll.setPreferredSize(new Dimension(WIDTH - 24, HEIGHT - 50));

        // 버튼
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);

        pinBtn = new JToggleButton();
        styleIconButton(pinBtn);
        pinBtn.setSelected(memo.isFixFlag());
        pinBtn.setToolTipText("고정");
        pinBtn.addActionListener(e -> {
            onPin.run();
            updateIcons();
        });

        editBtn = new JButton();
        styleIconButton(editBtn);
        editBtn.setToolTipText("수정");
        editBtn.addActionListener(e -> onEdit.run());

        delBtn = new JButton();
        styleIconButton(delBtn);
        delBtn.setToolTipText("삭제");
        delBtn.addActionListener(e -> onDelete.run());

        buttonPanel.add(pinBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(delBtn);

        add(buttonPanel, BorderLayout.NORTH);
        add(contentScroll, BorderLayout.CENTER);
        updateIcons();

        setupUnifiedHoverAndClick();
    }

    private void styleIconButton(AbstractButton btn) {
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

    public void updateThemeUI() {
        SwingUtilities.updateComponentTreeUI(this);
        updateIcons();
        HTMLEditorKit kit = (HTMLEditorKit) contentPane.getEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { color: " + colorToHex(UIManager.getColor("Label.foreground")) + "; }");
        contentPane.revalidate();
        contentPane.repaint();
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void setupUnifiedHoverAndClick() {
        MouseAdapter adapter = new MouseAdapter() {
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
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isClickOnButtons(e.getPoint())) return;
                if (onCardClick != null) onCardClick.run();
            }
        };

        // 일괄 등록
        addMouseListener(adapter);
        contentPane.addMouseListener(adapter);
        contentScroll.addMouseListener(adapter);
        contentScroll.getViewport().addMouseListener(adapter);
        contentPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // JEditorPane 내부에서 발생한 마우스 클릭 위치
                Point pt = SwingUtilities.convertPoint(contentPane, e.getPoint(), MemoCardPanel.this);

                // 버튼 영역 아닌 경우에만 위임
                if (!isClickOnButtons(pt) && onCardClick != null) {
                    onCardClick.run();
                }
            }
        });
        for (Component c : buttonPanel.getComponents()) c.addMouseListener(adapter);
    }

    private boolean isClickOnButtons(Point point) {
        Point toBtnPanel = SwingUtilities.convertPoint(this, point, buttonPanel);
        if (buttonPanel.contains(toBtnPanel)) return true;
        for (Component c : buttonPanel.getComponents()) {
            Point toChild = SwingUtilities.convertPoint(this, point, c);
            if (c.contains(toChild)) return true;
        }
        return false;
    }

    public void setOnCardClick(Runnable onCardClick) {
        this.onCardClick = onCardClick;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    public float getAlpha() {
        return alpha;
    }

    public void playEntryAnimation() {
        setAlpha(0f);
        Timeline timeline = new Timeline(this);
        timeline.addPropertyToInterpolate("alpha", 0.0f, 1.0f);
        timeline.setDuration(350);
        timeline.play();
    }

    public void playDeleteAnimation(Runnable onFinished) {
        Timeline timeline = new Timeline(this);
        timeline.addPropertyToInterpolate("alpha", 1.0f, 0.0f);
        timeline.setDuration(350);
        timeline.addCallback(new TimelineCallback() {
            @Override
            public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float fraction, float pos) {
                if (newState == TimelineState.DONE && onFinished != null) onFinished.run();
            }

            @Override public void onTimelinePulse(float f, float g) {}
        });
        timeline.play();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int arc = 12;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(FlatLaf.isLafDark() ? new Color(0, 0, 0, 50) : new Color(0, 0, 0, 30));
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, arc, arc);
        g2.setColor(hoverBg != null ? hoverBg : getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, arc, arc);
        if (memo.isFixFlag()) {
            g2.setColor(new Color(255, 216, 80, 160));
            g2.fillRoundRect(0, 0, 8, getHeight() - 6, arc / 2, arc / 2);
        }
        Color border = UIManager.getColor("Component.borderColor") != null ?
            UIManager.getColor("Component.borderColor") :
            (FlatLaf.isLafDark() ? new Color(70, 70, 70) : new Color(200, 180, 80));
        g2.setColor(border);
        g2.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, arc, arc);
        g2.dispose();
    }

    private Color getLighterColor() {
        Color c = getBackground() != null ? getBackground() : UIManager.getColor("Panel.background");
        return new Color(
            Math.min(255, c.getRed() + 18),
            Math.min(255, c.getGreen() + 18),
            Math.min(255, c.getBlue() + 18)
        );
    }

    public Memo getMemo() {
        return memo;
    }
}
