package gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import api.model.Memo;

public class MemoEditorFrame extends JDialog {
	private final Memo memo;
	private final Consumer<String> onSave;
	private boolean editMode;
	private final boolean isNew;

	private JEditorPane editor;
	private JToolBar toolbar;
	private JButton saveButton, closeButton, editButton;
	private FileSystemImageHandler imgHandler;
	private JComboBox<String> fontBox;
	private JComboBox<Integer> sizeBox;
	private JButton boldBtn, italicBtn, underlineBtn;
	private Integer[] sizes = { 8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 32, 36, 48, 72 };
	private boolean updatingUI = false;

	public MemoEditorFrame(Window owner, Memo memo, boolean editMode, boolean isNew, Consumer<String> onSave) {
		super(owner, "", ModalityType.APPLICATION_MODAL);
		this.memo = memo;
		this.editMode = editMode;
		this.isNew = isNew;
		this.onSave = onSave;

		try {
			this.imgHandler = new FileSystemImageHandler();
		} catch (IOException e) {
			this.imgHandler = null;
		}

		setSize(600, 400);
		setLocationRelativeTo(owner);
		setLayout(new BorderLayout());

		editor = new JEditorPane();
		editor.setContentType("text/html");
		editor.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
		editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		setEditorText(memo.getContent());

		// HTML 스타일 시트 세팅
		HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();

		styleSheet.addRule("body {" + " font-family: 'Malgun Gothic', sans-serif;" + " font-size: 12px;"
				+ " margin: 10px;" + " white-space: pre-wrap;" + "}");
		styleSheet.addRule("p {" + " margin: 0 0 10px 0;" + " white-space: pre-wrap;" + "}");
		styleSheet.addRule("span { white-space: pre-wrap; }");
		styleSheet.addRule("font { white-space: pre-wrap; }");
		styleSheet.addRule("div { white-space: pre-wrap; }");
		styleSheet.addRule("img { display: block; margin: 5px 0; }");

		toolbar = createToolbar();

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		saveButton = createButton("저장", this::saveAndClose);
		closeButton = createButton("닫기", () -> {
			onSave.accept(null);
			dispose();
		});
		editButton = createButton("수정", () -> setMode(true));

		btnPanel.add(saveButton);
		btnPanel.add(editButton);
		btnPanel.add(closeButton);

		add(toolbar, BorderLayout.NORTH);
		add(new JScrollPane(editor), BorderLayout.CENTER);
		add(btnPanel, BorderLayout.SOUTH);

		editor.addCaretListener(e -> refreshStyleUI());

		setMode(editMode);
		refreshStyleUI();
	}

	public void setMode(boolean editMode) {
		this.editMode = editMode;
		updateModeUI();
		updateTitle();
		if (editMode)
			SwingUtilities.invokeLater(() -> editor.requestFocusInWindow());
	}

	private void updateTitle() {
		if (isNew)
			setTitle("메모 추가");
		else if (editMode)
			setTitle("메모 수정");
		else
			setTitle("메모 보기");
	}

	private void updateModeUI() {
		toolbar.setVisible(editMode);
		editor.setEditable(editMode);
		saveButton.setVisible(editMode);
		editButton.setVisible(!editMode && !isNew);
		if (editMode)
			SwingUtilities.invokeLater(() -> editor.requestFocusInWindow());
	}

	private void setEditorText(String html) {
		String processedHtml;
		if (html == null || html.isBlank()) {
			processedHtml = "<html><body></body></html>";
		} else if (!html.toLowerCase().contains("<html")) {
			// plain text: HTML 이스케이프
			String escaped = html.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
			processedHtml = "<html><body>" + escaped + "</body></html>";
		} else {
			// 이미 HTML 마크업 포함
			processedHtml = html;
		}
		editor.setText(processedHtml);
	}

	private JToolBar createToolbar() {
		JToolBar t = new JToolBar();
		t.setFloatable(false);
		boldBtn = createStyleButton("B", "굵게", this::toggleBold);
		italicBtn = createStyleButton("I", "기울임", this::toggleItalic);
		underlineBtn = createStyleButton("U", "밑줄", this::toggleUnderline);
		t.add(boldBtn);
		t.add(italicBtn);
		t.add(underlineBtn);
		t.addSeparator();
		String[] fam = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontBox = new JComboBox<>(fam);
		fontBox.setMaximumSize(new Dimension(160, 24));
		fontBox.setSelectedItem("Malgun Gothic");
		fontBox.addActionListener(e -> {
			if (!updatingUI)
				changeFontFamily((String) fontBox.getSelectedItem());
		});
		t.add(fontBox);
		sizeBox = new JComboBox<>(sizes);
		sizeBox.setMaximumSize(new Dimension(70, 24));
		sizeBox.setSelectedItem(12);
		sizeBox.addActionListener(e -> {
			if (!updatingUI)
				changeFontSize((Integer) sizeBox.getSelectedItem());
		});
		t.add(sizeBox);
		t.addSeparator();
		t.add(createButton("글자색", this::chooseColor));
		t.addSeparator();
		t.add(createButton("이미지 삽입", this::insertImage));
		return t;
	}

	private void refreshStyleUI() {
		updatingUI = true;
		AttributeSet a = getCharacterAttributes();
		String fam = StyleConstants.getFontFamily(a);
		if (fam != null)
			fontBox.setSelectedItem(fam);
		int sz = StyleConstants.getFontSize(a);
		boolean found = false;
		for (int i = 0; i < sizeBox.getItemCount(); i++)
			if (sizeBox.getItemAt(i) == sz) {
				found = true;
				break;
			}
		if (!found)
			sizeBox.addItem(sz);
		sizeBox.setSelectedItem(sz);
		boldBtn.setForeground(StyleConstants.isBold(a) ? Color.BLUE : Color.BLACK);
		italicBtn.setForeground(StyleConstants.isItalic(a) ? Color.BLUE : Color.BLACK);
		underlineBtn.setForeground(StyleConstants.isUnderline(a) ? Color.BLUE : Color.BLACK);
		updatingUI = false;
	}

	private void applyCharAttr(AttributeSet attr, boolean replace) {
		int start = editor.getSelectionStart();
		int end = editor.getSelectionEnd();

		if (start == end) {
			StyledEditorKit kit = (StyledEditorKit) editor.getEditorKit();
			MutableAttributeSet ia = kit.getInputAttributes();
			if (replace)
				ia.removeAttributes(ia);
			ia.addAttributes(attr);
			return;
		}

		HTMLDocument doc = (HTMLDocument) editor.getDocument();
		try {
			String txt = doc.getText(start, end - start);
			int run = -1;
			for (int i = 0; i < txt.length(); i++) {
				char c = txt.charAt(i);
				if (c == '\n' || c == '\r') {
					if (run != -1) {
						doc.setCharacterAttributes(start + run, i - run, attr, false);
						run = -1;
					}
				} else if (run == -1)
					run = i;
			}
			if (run != -1)
				doc.setCharacterAttributes(start + run, txt.length() - run, attr, false);
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}

	private void changeFontFamily(String f) {
		MutableAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setFontFamily(a, f);
		applyCharAttr(a, false);
		refreshStyleUI();
	}

	private void changeFontSize(int s) {
		MutableAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setFontSize(a, s);
		applyCharAttr(a, false);
		refreshStyleUI();
	}

	private JButton createButton(String text, Runnable r) {
		JButton b = new JButton(text);
		b.addActionListener(e -> r.run());
		return b;
	}

	private JButton createStyleButton(String text, String tip, Runnable r) {
		JButton b = new JButton(text);
		b.setToolTipText(tip);
		b.addActionListener(e -> r.run());
		return b;
	}

	private void toggleBold() {
		MutableAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setBold(a, !StyleConstants.isBold(getCharacterAttributes()));
		applyCharAttr(a, false);
		refreshStyleUI();
	}

	private void toggleItalic() {
		MutableAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setItalic(a, !StyleConstants.isItalic(getCharacterAttributes()));
		applyCharAttr(a, false);
		refreshStyleUI();
	}

	private void toggleUnderline() {
		MutableAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setUnderline(a, !StyleConstants.isUnderline(getCharacterAttributes()));
		applyCharAttr(a, false);
		refreshStyleUI();
	}

	private void setAlignment(int align) {
		MutableAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setAlignment(a, align);
		setParagraphAttributes(a, false);
	}

	private void chooseColor() {
		Color c = JColorChooser.showDialog(this, "글자색 선택", Color.BLACK);
		if (c != null) {
			MutableAttributeSet a = new SimpleAttributeSet();
			StyleConstants.setForeground(a, c);
			applyCharAttr(a, false);
			refreshStyleUI();
		}
	}

	private AttributeSet getCharacterAttributes() {
		StyledEditorKit kit = (StyledEditorKit) editor.getEditorKit();
		return kit.getInputAttributes();
	}

	private void setParagraphAttributes(AttributeSet attr, boolean replace) {
		int start = editor.getSelectionStart();
		int end = editor.getSelectionEnd();
		HTMLDocument doc = (HTMLDocument) editor.getDocument();
		doc.setParagraphAttributes(start, end - start, attr, replace);
	}

	private void insertImage() {
		JFileChooser ch = new JFileChooser();
		ch.setFileFilter(
				new javax.swing.filechooser.FileNameExtensionFilter("이미지 파일", "png", "jpg", "jpeg", "gif", "bmp"));
		if (ch.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		File imgFile = ch.getSelectedFile();
		ImageIcon prev = new ImageIcon(
				new ImageIcon(imgFile.getAbsolutePath()).getImage().getScaledInstance(150, -1, Image.SCALE_SMOOTH));
		if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(this, new JLabel(prev), "이 이미지 삽입?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE))
			return;
		try {
			String filename = imgHandler.storeImage(imgFile);
			Path dest = imgHandler.getImagesDir().resolve(filename);
			String uri = dest.toUri().toString();
			BufferedImage stored = ImageIO.read(dest.toFile());
			String imgTag = String.format(
					"<img src=\"%s\" width=\"100\" height=\"100\" align=\"left\" hspace=\"0\" vspace=\"5\" />", uri);
			HTMLDocument doc = (HTMLDocument) editor.getDocument();
			HTMLEditorKit kit2 = (HTMLEditorKit) editor.getEditorKit();
			kit2.insertHTML(doc, editor.getCaretPosition(), "<br>" + imgTag, 0, 0, HTML.Tag.IMG);
			editor.revalidate();
			editor.repaint();
			editor.setText(editor.getText());
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "이미지를 삽입하는 중 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void saveAndClose() {
		String html = editor.getText();
		if (imgHandler != null)
			imgHandler.deleteRemovedImages(memo.getContent(), html);
		onSave.accept(html);
		dispose();
	}
}
