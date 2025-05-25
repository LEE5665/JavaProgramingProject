package gui.main.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

import api.model.Todo;
import api.model.TodoDAO;
import net.miginfocom.swing.MigLayout;

public class TodoPanel extends JPanel {
	private final int userId;
	private LocalDate currentDate;
	private final JLabel dayLabel = new JLabel();
	private final JButton calBtn = new JButton("📅");
	private final JButton todayBtn = new JButton("오늘");
	private final JButton addBtn = new JButton("＋ 할 일 추가");
	private final JButton prevBtn = new JButton("◀");
	private final JButton nextBtn = new JButton("▶");
	private final JLayeredPane layer = new JLayeredPane();
	private final DatePicker datePicker;
	private final JButton arrowBtn;
	private JScrollPane currentScroll;

	public TodoPanel(int userId) {
		this.userId = userId;
		setLayout(new BorderLayout());
		DatePickerSettings set = new DatePickerSettings(Locale.KOREAN);
		set.setAllowEmptyDates(false);
		set.setVisibleTodayButton(false);
		set.setVisibleClearButton(false);
		datePicker = new DatePicker(set);
		datePicker.setPreferredSize(new Dimension(1, 1));
		datePicker.getComponentDateTextField().setVisible(false);
		arrowBtn = datePicker.getComponentToggleCalendarButton();
		arrowBtn.setMargin(new Insets(0, 0, 0, 0));
		arrowBtn.setText("");
		arrowBtn.setFocusable(false);
		arrowBtn.setOpaque(false);
		arrowBtn.setContentAreaFilled(false);
		arrowBtn.setBorderPainted(false);
		arrowBtn.setPreferredSize(new Dimension(1, 1));
		datePicker.addDateChangeListener((DateChangeEvent e) -> {
			LocalDate d = e.getNewDate();
			if (d != null)
				loadPage(d, d.isAfter(currentDate), true);
		});
		buildHeader();
		buildArrows();
		add(layer, BorderLayout.CENTER);
		loadPage(LocalDate.now(), false, false);
		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				if (currentScroll != null) {
					currentScroll.setBounds(0, 0, layer.getWidth(), layer.getHeight());
					layer.revalidate();
				}
			}
		});
		UIManager.addPropertyChangeListener(evt -> {
			if ("lookAndFeel".equals(evt.getPropertyName())) {
				applyTheme(TodoPanel.this);
				if (currentScroll != null)
					applyTheme(currentScroll);
				applyTheme(datePicker);
				revalidate();
				repaint();
			}
		});
		applyTheme(this);
	}

	private void applyTheme(Component comp) {
		Color bg = UIManager.getColor("Panel.background");
		Color fg = UIManager.getColor("Label.foreground");
		if (comp instanceof JScrollPane sp) {
			sp.getViewport().getView().setBackground(bg);
			sp.setBackground(bg);
			applyTheme(sp.getViewport().getView());
		} else if (comp instanceof JPanel p)
			p.setBackground(bg);
		if (comp instanceof JLabel l)
			l.setForeground(fg);
		if (comp instanceof AbstractButton b) {
			b.setBackground(bg);
			b.setForeground(fg);
		}
		if (comp instanceof DatePicker dp) {
			dp.getComponentDateTextField().setBackground(bg);
			dp.getComponentDateTextField().setForeground(fg);
			arrowBtn.setBackground(bg);
		}
		if (comp instanceof Container c)
			for (Component ch : c.getComponents())
				applyTheme(ch);
	}

	private void buildHeader() {
		dayLabel.setFont(getFont().deriveFont(Font.BOLD, 16f));
		dayLabel.setBorder(new EmptyBorder(0, 8, 0, 8));
		calBtn.addActionListener(e -> {
			datePicker.setDate(currentDate);
			datePicker.openPopup();
			datePicker.setSize(400, 300);
		});
		todayBtn.addActionListener(e -> {
			LocalDate today = LocalDate.now();
			boolean animate = !today.equals(currentDate);
			boolean slideLeft = today.isAfter(currentDate);
			loadPage(today, slideLeft, animate);
		});
		addBtn.addActionListener(e -> {
			TodoDAO.insert(new Todo(userId, null, 0, "새로운 할 일", "", currentDate, null, null));
			reloadCurrent();
		});
		JPanel head = new JPanel(new MigLayout("insets 4", "[][5!][][grow]push[]", "[]"));
		head.add(dayLabel);
		head.add(calBtn);
		head.add(todayBtn);
		head.add(datePicker, "w 1!, h 1!, growx");
		head.add(addBtn);
		add(head, BorderLayout.NORTH);
	}

	private void buildArrows() {
		Dimension d = new Dimension(45, 45);
		prevBtn.setPreferredSize(d);
		nextBtn.setPreferredSize(d);
		for (JButton b : List.of(prevBtn, nextBtn)) {
			b.setFocusPainted(false);
			b.setOpaque(false);
			b.setContentAreaFilled(false);
			b.setBorderPainted(false);
			b.setFont(b.getFont().deriveFont(Font.BOLD, 18f));
		}
		prevBtn.addActionListener(e -> loadPage(currentDate.minusDays(1), false, true));
		nextBtn.addActionListener(e -> loadPage(currentDate.plusDays(1), true, true));
		add(prevBtn, BorderLayout.WEST);
		add(nextBtn, BorderLayout.EAST);
	}

	private void loadPage(LocalDate target, boolean slideLeft, boolean animate) {
		JScrollPane next = createPage(target);
		applyTheme(next);
		JScrollPane cur = currentScroll;
		int w = layer.getWidth();
		if (!animate || cur == null) {
			layer.removeAll();
			next.setBounds(0, 0, w, layer.getHeight());
			layer.add(next, JLayeredPane.DEFAULT_LAYER, 0);
		} else {
			int startX = slideLeft ? w : -w;
			int dir = slideLeft ? -1 : 1;
			next.setBounds(startX, 0, w, layer.getHeight());
			layer.add(next, JLayeredPane.DEFAULT_LAYER, 0);
			animatePageTurn(cur, next, dir);
		}
		currentScroll = next;
		currentDate = target;
		dayLabel.setText(String.format("%s (%s)", target,
				target.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, Locale.KOREAN)));
	}

	private void animatePageTurn(JScrollPane cur, JScrollPane next, int dir) {
		int w = layer.getWidth();
		int duration = 450;
		int fps = 60;
		long start = System.currentTimeMillis();
		Timer timer = new Timer(1000 / fps, null);
		timer.addActionListener(e -> {
			float t = (System.currentTimeMillis() - start) / (float) duration;
			if (t > 1f)
				t = 1f;
			float ease = 1 - (float) Math.pow(1 - t, 3);
			int curX = Math.round(ease * w * dir);
			int nextX = Math.round((ease - 1) * w * dir);
			cur.setLocation(curX, 0);
			next.setLocation(nextX, 0);
			if (t >= 1f) {
				next.setLocation(0, 0);
				layer.remove(cur);
				layer.repaint();
				((Timer) e.getSource()).stop();
			}
		});
		timer.start();
	}

	void reloadCurrent() {
		loadPage(currentDate, false, false);
	}

	private List<Todo> sortHierarchy(List<Todo> list) {
		Map<Integer, List<Todo>> child = new LinkedHashMap<>();
		List<Todo> roots = new ArrayList<>();
		for (Todo t : list)
			if (t.getParentId() == null)
				roots.add(t);
			else
				child.computeIfAbsent(t.getParentId(), k -> new ArrayList<>()).add(t);
		List<Todo> out = new ArrayList<>();
		for (Todo r : roots)
			addWithChildren(out, r, child);
		return out;
	}

	private void addWithChildren(List<Todo> out, Todo p, Map<Integer, List<Todo>> child) {
		out.add(p);
		if (child.containsKey(p.getId()))
			for (Todo c : child.get(p.getId()))
				addWithChildren(out, c, child);
	}

	private JScrollPane createPage(LocalDate date) {
		JPanel body = new JPanel(new MigLayout("insets 10 20 10 20, gapy 8", "[grow]", ""));
		List<Todo> list = sortHierarchy(TodoDAO.listByDate(userId, date));
		list.forEach(t -> body.add(new TodoItemPanel(t, this::reloadCurrent), "growx, wrap"));
		JScrollPane sp = new JScrollPane(body, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setBorder(null);
		sp.getVerticalScrollBar().setUnitIncrement(16);
		sp.setBounds(0, 0, layer.getWidth(), layer.getHeight());
		return sp;
	}
}
