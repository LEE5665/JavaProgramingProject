package gui.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

public class MemoEditorFrame extends JDialog {
	private final JEditorPane editor;
	private final Consumer<String> onSave;
	private final FileSystemImageHandler imgHandler;

	public MemoEditorFrame(Window owner, String title, String initialHtml, Consumer<String> onSave) throws IOException {
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
		toolbar.add(mkButton("이미지 삽입", this::insertImage));
		add(toolbar, BorderLayout.NORTH);

		add(new JScrollPane(editor), BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		Stream.of(mkButton("저장", this::saveAndClose), mkButton("취소", () -> {
			onSave.accept(null);
			dispose();
		})).forEach(btnPanel::add);
		add(btnPanel, BorderLayout.SOUTH);
	}

	private JButton mkButton(String text, Runnable onClick) {
		JButton b = new JButton(text);
		b.addActionListener(e -> onClick.run());
		return b;
	}

	private void insertImage() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(
				new javax.swing.filechooser.FileNameExtensionFilter("이미지 파일", "png", "jpg", "jpeg", "gif", "bmp"));
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;

		File imgFile = chooser.getSelectedFile();
		ImageIcon preview = new ImageIcon(
				new ImageIcon(imgFile.getAbsolutePath()).getImage().getScaledInstance(150, -1, Image.SCALE_SMOOTH));
		if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(this, new JLabel(preview), "이 이미지 삽입?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
			return;

		try {
			String filename = imgHandler.storeImage(imgFile);
			Path dest = imgHandler.getImagesDir().resolve(filename);
			String uri = dest.toUri().toString();

			BufferedImage stored = ImageIO.read(dest.toFile());
			int w = stored.getWidth(), h = stored.getHeight();

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
		onSave.accept(editor.getText());
		dispose();
	}
}
