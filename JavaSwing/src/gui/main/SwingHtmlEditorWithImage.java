package gui.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class SwingHtmlEditorWithImage extends JDialog {
	private JEditorPane editor;
	private Consumer<String> onSave;
	private FileSystemImageHandler imgHandler;

	public SwingHtmlEditorWithImage(Window owner, String title, String initialHtml, Consumer<String> onSave)
			throws Exception {
		super(owner, title, ModalityType.APPLICATION_MODAL);
		this.onSave = onSave;
		this.imgHandler = new FileSystemImageHandler();
		setLayout(new BorderLayout());
		setSize(600, 400);
		setLocationRelativeTo(owner);

		editor = new JEditorPane();
		editor.setContentType("text/html");
		editor.setText(initialHtml != null ? initialHtml : "<html><body></body></html>");
		editor.setEditable(true);

		JToolBar toolbar = new JToolBar();
		JButton imgBtn = new JButton("이미지 삽입");
		imgBtn.addActionListener(e -> insertImage());
		toolbar.add(imgBtn);

		add(toolbar, BorderLayout.NORTH);
		add(new JScrollPane(editor), BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		JButton btnSave = new JButton("저장");
		btnSave.addActionListener(e -> saveAndClose());
		JButton btnCancel = new JButton("취소");
		btnCancel.addActionListener(e -> {
			onSave.accept(null);
			dispose();
		});
		btnPanel.add(btnSave);
		btnPanel.add(btnCancel);
		add(btnPanel, BorderLayout.SOUTH);
	}

	private void insertImage() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(
				new javax.swing.filechooser.FileNameExtensionFilter("이미지 파일", "png", "jpg", "jpeg", "gif", "bmp"));
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		File imgFile = chooser.getSelectedFile();
		ImageIcon icon = new ImageIcon(
				new ImageIcon(imgFile.getAbsolutePath()).getImage().getScaledInstance(150, -1, Image.SCALE_SMOOTH));
		int ok = JOptionPane.showConfirmDialog(this, new JLabel(icon), "이 이미지 삽입?", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (ok != JOptionPane.OK_OPTION)
			return;
		try {
			String filename = imgHandler.storeImage(imgFile);
			Path dest = imgHandler.getImagesDir().resolve(filename);
			if (!Files.exists(dest))
				throw new IOException("잘못된 경로: " + dest);
			String uri = dest.toUri().toString();
			BufferedImage stored = ImageIO.read(dest.toFile());
			int w = stored.getWidth();
			int h = stored.getHeight();
			HTMLDocument doc = (HTMLDocument) editor.getDocument();
			HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
			String imgTag = String.format("<img src=\"%s\" width=\"%d\" height=\"%d\"/>", uri, w, h);
			kit.insertHTML(doc, editor.getCaretPosition(), imgTag, 0, 0, HTML.Tag.IMG);
			editor.revalidate();
			editor.repaint();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "이미지를 삽입하는 중 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void saveAndClose() {
		String html = editor.getText();
		onSave.accept(html);
		dispose();
	}
}
