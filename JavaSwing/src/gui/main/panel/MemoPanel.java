package gui.main.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.pushingpixels.radiance.animation.api.Timeline;
import org.pushingpixels.radiance.animation.api.Timeline.TimelineState;
import org.pushingpixels.radiance.animation.api.callback.TimelineCallbackAdapter;
import org.pushingpixels.radiance.animation.api.ease.Spline;

import api.model.Memo;
import api.model.MemoDAO;
import gui.main.SwingHtmlEditorWithImage;

public class MemoPanel extends JPanel {

	private final MemoDAO dao = new MemoDAO();
	private final int userId;
	private final JPanel header, listPanel;
	private final JScrollPane scroll;
	private final Map<Integer, JDialog> viewers = new HashMap<>();

	public MemoPanel(int userId) {
		this.userId = userId;
		setLayout(new BorderLayout());
		setBackground(UIManager.getColor("Panel.background"));

		JButton add = new JButton("+ 메모 추가");
		add.addActionListener(e -> addMemo());

		header = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		header.setBackground(getBackground());
		header.add(add);
		add(header, BorderLayout.NORTH);

		listPanel = new JPanel(new MemoCardPanel.WrapLayout(java.awt.FlowLayout.LEFT, 16, 16));
		listPanel.setOpaque(false);

		scroll = new JScrollPane(listPanel);
		scroll.getViewport().setBackground(UIManager.getColor("Viewport.background"));
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		add(scroll, BorderLayout.CENTER);

		reloadMemos();
	}

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
		JComponent glass = (JComponent) SwingUtilities.getRootPane(this).getGlassPane();
		glass.setLayout(null);
		Rectangle start = SwingUtilities.convertRectangle(card.getParent(), card.getBounds(), glass);

		BufferedImage img = new BufferedImage(card.getWidth(), card.getHeight(), BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g2 = img.createGraphics();
		card.paint(g2);
		g2.dispose();

		MemoCardPanel.GhostPanel ghost = new MemoCardPanel.GhostPanel(img);
		ghost.setBounds(start);
		glass.add(ghost);
		glass.setVisible(true);

		listPanel.remove(card);
		listPanel.revalidate();
		listPanel.repaint();

		Timeline.builder(ghost).addPropertyToInterpolate("alpha", 1f, 0f)
				.addPropertyToInterpolate("animY", start.y, start.y + 20).setDuration(350)
				.setEase(new Spline(0.4f, 0f, 0.2f, 1f)).addCallback(new TimelineCallbackAdapter() {
					public void onTimelineStateChanged(TimelineState o, TimelineState n, float f1, float f2) {
						if (n == TimelineState.DONE) {
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
			for (java.awt.Component c : listPanel.getComponents()) {
				MemoCardPanel mc = (MemoCardPanel) c;
				if (mc.getMemo().getId() == id) {
					mc.playEntryAnimation();
					break;
				}
			}
		});
	}

	public void expandMemo(MemoCardPanel card) {
		Memo memo = card.getMemo();
		if (viewers.containsKey(memo.getId())) {
			JDialog v = viewers.get(memo.getId());
			v.setVisible(true);
			v.toFront();
			return;
		}

		JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this));
		dlg.setTitle("메모");
		dlg.setModal(false);
		dlg.setMinimumSize(new Dimension(500, 400));

		final MemoCardPanel.FullViewPanel[] holder = new MemoCardPanel.FullViewPanel[1];
		holder[0] = new MemoCardPanel.FullViewPanel(memo, () -> {
			memo.setFixFlag(!memo.isFixFlag());
			try {
				dao.updateMemo(memo);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			reloadMemos();
		}, () -> {
			editMemo(memo);
			holder[0].refreshContent();
		}, dlg::dispose);

		dlg.setContentPane(holder[0]);
		dlg.pack();
		Dimension s = dlg.getSize();
		if (s.width < 500 || s.height < 400)
			dlg.setSize(Math.max(500, s.width), Math.max(400, s.height));

		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);

		dlg.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosed(java.awt.event.WindowEvent e) {
				viewers.remove(memo.getId());
			}
		});

		viewers.put(memo.getId(), dlg);
	}

}
