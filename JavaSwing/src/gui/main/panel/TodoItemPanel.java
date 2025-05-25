package gui.main.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import api.model.Todo;
import api.model.TodoDAO;
import net.miginfocom.swing.MigLayout;

class TodoItemPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	static final DataFlavor FLAVOR = new DataFlavor(Todo.class, "Todo");
	private final Todo todo;
	private final Runnable refresh;
	private JLabel lbl;
	private JButton editBtn;
	private float strike = 0f;
	private Timer strikeTimer;

	TodoItemPanel(Todo todo, Runnable refresh) {
		this.todo = todo;
		this.refresh = refresh;
		this.strike = todo.isCompleted() ? 1f : 0f;
		buildUI();
		setTransferHandler(new TodoTransferHandler());
		addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				getTransferHandler().exportAsDrag(TodoItemPanel.this, e, TransferHandler.MOVE);
			}
		});
	}

	public Todo getTodo() {
		return todo;
	}

	private void buildUI() {
		setOpaque(false);
		setLayout(new MigLayout("insets 2, filly", "[][][grow][][]", "[]"));
		setBorder(new EmptyBorder(0, todo.getDepth() * 20, 0, 0));
		JButton sub = new JButton("+");
		sub.setMargin(new Insets(2, 4, 2, 4));
		sub.setVisible(todo.getDepth() == 0);
		sub.addActionListener(e -> {
			TodoDAO.insert(new Todo(todo.getUserId(), todo.getId(), todo.getDepth() + 1, "세부 작업", "", todo.getDate(),
					null, null));
			refresh.run();
		});
		add(sub);
		JCheckBox chk = new JCheckBox();
		chk.setSelected(todo.isCompleted());
		chk.addActionListener(e -> toggle());
		add(chk);
		lbl = new JLabel(todo.getTitle());
		lbl.setFont(lbl.getFont().deriveFont(todo.getDepth() == 0 ? 14f : 12f));
		add(lbl, "growx");
		editBtn = new JButton("🖊️");
		editBtn.setMargin(new Insets(2, 6, 2, 6));
		editBtn.addActionListener(e -> edit());
		add(editBtn);
		JButton del = new JButton("🗑");
		del.setMargin(new Insets(2, 6, 2, 6));
		del.setForeground(Color.RED.darker());
		del.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(this, "삭제할까요?", "확인",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				TodoDAO.delete(todo.getId());
				refresh.run();
			}
		});
		add(del);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2)
					edit();
			}
		});
	}

	private void toggle() {
		boolean next = !todo.isCompleted();
		TodoDAO.toggleCompleted(todo.getId(), next);
		todo.setCompleted(next);
		animateStrike(next);
	}

	private void animateStrike(boolean on) {
		if (strikeTimer != null && strikeTimer.isRunning())
			strikeTimer.stop();
		strikeTimer = new Timer(15, null);
		strikeTimer.addActionListener(e -> {
			strike += on ? 0.05f : -0.05f;
			repaint();
			if (strike < 0f || strike > 1f)
				strikeTimer.stop();
		});
		strikeTimer.start();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (strike <= 0)
			return;
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setStroke(new BasicStroke(2f));
		g2.setColor(Color.GRAY);
		Rectangle r = lbl.getBounds();
		int x1 = r.x;
		int x2 = editBtn.getX() - 5;
		int y = r.y + r.height / 2;
		int cur = (int) (x1 + (x2 - x1) * strike);
		g2.drawLine(x1, y, cur, y);
		g2.dispose();
	}

	private void edit() {
		String t = JOptionPane.showInputDialog(this, "할 일을 수정하세요", todo.getTitle());
		if (t != null && !t.trim().isEmpty()) {
			todo.setTitle(t.trim());
			TodoDAO.update(todo);
			refresh.run();
		}
	}

	private static class TodoTransferable implements Transferable {
		private final Todo todo;

		TodoTransferable(Todo t) {
			this.todo = t;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { FLAVOR };
		}

		public boolean isDataFlavorSupported(DataFlavor f) {
			return f.equals(FLAVOR);
		}

		public Object getTransferData(DataFlavor f) {
			return todo;
		}
	}

	private static class TodoTransferHandler extends TransferHandler {
		public int getSourceActions(javax.swing.JComponent c) {
			return MOVE;
		}

		protected Transferable createTransferable(javax.swing.JComponent c) {
			return new TodoTransferable(((TodoItemPanel) c).getTodo());
		}

		public boolean canImport(TransferSupport t) {
			t.setShowDropLocation(true);
			return t.isDataFlavorSupported(FLAVOR);
		}

		public boolean importData(TransferSupport ts) {
			try {
				Todo src = (Todo) ts.getTransferable().getTransferData(FLAVOR);
				TodoItemPanel tgtPanel = (TodoItemPanel) ts.getComponent();
				Todo tgt = tgtPanel.getTodo();
				if (src.getId() == tgt.getId())
					return false;

				boolean right = ts.getDropLocation().getDropPoint().x > tgtPanel.getWidth() / 2;
				boolean makeChild = right && tgt.getDepth() == 0 && src.getDepth() == 0;
				Integer newParent = makeChild ? Integer.valueOf(tgt.getId()) : tgt.getParentId();
				int newDepth = makeChild ? 1 : tgt.getDepth();

				int newSeq;
				int halfY = tgtPanel.getHeight() / 2;
				boolean after = ts.getDropLocation().getDropPoint().y > halfY;
				newSeq = after ? tgt.getSeq() + 1 : tgt.getSeq();

				if (Objects.equals(src.getParentId(), newParent) && src.getDepth() == newDepth
						&& src.getSeq() == newSeq)
					return false;

				TodoDAO.move(src.getId(), newParent, newDepth, newSeq);
				TodoPanel root = (TodoPanel) SwingUtilities.getAncestorOfClass(TodoPanel.class, tgtPanel);
				if (root != null)
					root.reloadCurrent();
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
	}
}
