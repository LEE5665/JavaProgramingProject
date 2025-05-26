package gui.main.panel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import api.model.Memo;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.callback.TimelineCallback;
import org.pushingpixels.trident.Timeline.TimelineState;
import com.formdev.flatlaf.FlatLaf;

public class MemoCardPanel extends JPanel {
    private static final int WIDTH = 170, HEIGHT = 150;
    private float alpha = 1.0f;
    private Memo memo;
    private JTextArea textArea;
    private JPanel buttonPanel;
    private JScrollPane contentScroll;
    private Color hoverBg = null;

    public MemoCardPanel(
        Memo memo,
        Runnable onDelete,
        Runnable onEdit,
        Runnable onPin
    ) {
        this.memo = memo;

        setBackground(UIManager.getColor("Panel.background"));
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMaximumSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(120, 60));
        setBorder(new EmptyBorder(8, 10, 8, 10));

        
        textArea = new JTextArea(memo.getContent() == null ? "" : memo.getContent());
        textArea.setEditable(false);
        textArea.setOpaque(false); 
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        textArea.setForeground(UIManager.getColor("Label.foreground"));
        textArea.setBorder(null);
        textArea.setFocusable(false);

        contentScroll = new JScrollPane(
            textArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.getViewport().setBorder(null);
        
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setOpaque(false);
        contentScroll.setBackground(new Color(0,0,0,0));
        contentScroll.getViewport().setBackground(new Color(0,0,0,0));
        contentScroll.setPreferredSize(new Dimension(WIDTH - 24, HEIGHT - 50));

        
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(null);

        JToggleButton pinBtn = new JToggleButton("📌");
        pinBtn.setSelected(memo.isFixFlag());
        pinBtn.setMargin(new Insets(0, 0, 0, 0));
        pinBtn.setBorderPainted(false);
        pinBtn.setContentAreaFilled(false);
        pinBtn.setFocusPainted(false);
        pinBtn.setFont(new Font("Dialog", Font.PLAIN, 13));
        pinBtn.setToolTipText("고정");
        pinBtn.addActionListener(e -> onPin.run());
        buttonPanel.add(pinBtn);

        JButton editBtn = new JButton("✎");
        editBtn.setMargin(new Insets(0, 2, 0, 2));
        editBtn.setBorderPainted(false);
        editBtn.setContentAreaFilled(false);
        editBtn.setFocusPainted(false);
        editBtn.setFont(new Font("Dialog", Font.PLAIN, 13));
        editBtn.setToolTipText("수정");
        editBtn.addActionListener(e -> onEdit.run());
        buttonPanel.add(editBtn);

        JButton delBtn = new JButton("✕");
        delBtn.setMargin(new Insets(0, 2, 0, 2));
        delBtn.setBorderPainted(false);
        delBtn.setContentAreaFilled(false);
        delBtn.setFocusPainted(false);
        delBtn.setFont(new Font("Dialog", Font.PLAIN, 13));
        delBtn.setToolTipText("삭제");
        delBtn.addActionListener(e -> onDelete.run());
        buttonPanel.add(delBtn);

        add(buttonPanel, BorderLayout.NORTH);
        add(contentScroll, BorderLayout.CENTER);

        
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
        
        MouseAdapter delegateHover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                MemoCardPanel.this.dispatchEvent(
                    SwingUtilities.convertMouseEvent(e.getComponent(), e, MemoCardPanel.this)
                );
            }
            @Override
            public void mouseExited(MouseEvent e) {
                MemoCardPanel.this.dispatchEvent(
                    SwingUtilities.convertMouseEvent(e.getComponent(), e, MemoCardPanel.this)
                );
            }
        };
        textArea.addMouseListener(delegateHover);
        buttonPanel.addMouseListener(delegateHover);
        contentScroll.addMouseListener(delegateHover);
        contentScroll.getViewport().addMouseListener(delegateHover);
        contentScroll.getVerticalScrollBar().addMouseListener(delegateHover);
        contentScroll.getHorizontalScrollBar().addMouseListener(delegateHover);
        for (Component btn : buttonPanel.getComponents()) {
            btn.addMouseListener(delegateHover);
        }
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }
    public float getAlpha() {
        return this.alpha;
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
            public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float durationFraction, float timelinePosition) {
                if (newState == TimelineState.DONE) {
                    if (onFinished != null) onFinished.run();
                }
            }
            @Override
            public void onTimelinePulse(float durationFraction, float timelinePosition) {}
        });

        timeline.play();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int arc = 12;

        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        
        g2.setColor(FlatLaf.isLafDark() ? new Color(0,0,0,50) : new Color(0,0,0,30));
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, arc, arc);

        
        Color bg = hoverBg != null ? hoverBg : getBackground();
        g2.setColor(bg);
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
        int r = Math.min(255, c.getRed() + 18);
        int g = Math.min(255, c.getGreen() + 18);
        int b = Math.min(255, c.getBlue() + 18);
        return new Color(r, g, b);
    }

    public Memo getMemo() {
        return memo;
    }
}
