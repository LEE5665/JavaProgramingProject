package gui.main;

import javax.swing.*;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.awt.event.*;
import gui.main.panel.MemoPanel;
import gui.main.panel.TodoPanel;
import net.miginfocom.swing.MigLayout;

public class MainFrame extends JFrame {
    private int userId;

    private JToggleButton todoButton;
    private JToggleButton memoButton;
    private ButtonGroup typeGroup;

    private JPanel contentPanel;          // 가운데 갈아끼우는 패널
    private MemoPanel memoPanel;          // 메모 전용 패널
    private TodoPanel todoPanel;          // 투두 전용 패널

    public MainFrame(int userId) {
        this.userId = userId;
        setTitle("일정 관리");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(new Color(20, 20, 20));

        // 유형 선택 버튼
        todoButton = createTypeToggle("To-do list");
        memoButton = createTypeToggle("메모장");
        typeGroup = new ButtonGroup();
        typeGroup.add(todoButton);
        typeGroup.add(memoButton);
        memoButton.setSelected(true);

        todoButton.addActionListener(e -> switchContent("todo"));
        memoButton.addActionListener(e -> switchContent("memo"));

        JPanel typePanel = new JPanel(new MigLayout("wrap 2, insets 0, gap 0", "[50%][50%]"));
        typePanel.setBackground(new Color(20, 20, 20));
        typePanel.add(todoButton, "growx");
        typePanel.add(memoButton, "growx");

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);

        JPanel modePanel = new JPanel(new BorderLayout());
        modePanel.setBackground(new Color(20, 20, 20));
        modePanel.add(typePanel, BorderLayout.CENTER);
        modePanel.add(separator, BorderLayout.SOUTH);

        // 중앙 컨텐츠 패널 (카드 레이아웃)
        contentPanel = new JPanel(new CardLayout());
        memoPanel = new MemoPanel(userId);
        todoPanel = new TodoPanel();
        contentPanel.add(memoPanel, "memo");
        contentPanel.add(todoPanel, "todo");

        add(modePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        switchContent("memo");
    }

    private JToggleButton createTypeToggle(String text) {
        JToggleButton btn = new JToggleButton("<html><div style='text-align:center;'>" + text + "<br><hr></div></html>");
        btn.setFocusPainted(false);
        btn.setFont(new Font("Dialog", Font.BOLD, 16));
        btn.setBackground(new Color(40, 40, 40));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.addItemListener(e -> btn.setForeground(btn.isSelected() ? Color.CYAN : Color.WHITE));
        return btn;
    }

    private void switchContent(String name) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
    }
}
