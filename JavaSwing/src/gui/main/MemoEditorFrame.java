package gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
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
		editor.setFont(new Font("Malgun Gothic", Font.PLAIN, 12)); // 초기 글꼴 설정
		editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		setEditorText(memo.getContent());

		HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();
		// HTML 기본 스타일시트 업데이트: 기본 글꼴 및 크기 설정
		styleSheet.addRule("body { font-family: 'Malgun Gothic', sans-serif; font-size: 12px; margin: 10px; }");
		styleSheet.addRule("p { margin: 0 0 10px 0; }");
		styleSheet.addRule("img { display: block; margin: 5px 0; }");
		styleSheet.addRule("pre { margin: 0; white-space: pre-wrap; }");

		// Toolbar (수정 모드에서만 보임)
		toolbar = createToolbar();

		// Buttons (읽기/수정모드에 따라 보임)
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		saveButton = createButton("저장", this::saveAndClose);
		closeButton = createButton("닫기", () -> {
			onSave.accept(null); // 저장 콜백 없이 닫기
			dispose();
		});
		editButton = createButton("수정", () -> setMode(true));

		btnPanel.add(saveButton);
		btnPanel.add(editButton);
		btnPanel.add(closeButton);

		add(toolbar, BorderLayout.NORTH);
		add(new JScrollPane(editor), BorderLayout.CENTER);
		add(btnPanel, BorderLayout.SOUTH);

		setMode(editMode); // 초기 모드 설정
	}

	public void setMode(boolean editMode) {
		this.editMode = editMode;
		updateModeUI();
		updateTitle();

		// 수정 모드에서 창이 이미 떠 있을 때도 포커스
		if (editMode) {
			SwingUtilities.invokeLater(() -> editor.requestFocusInWindow());
		}
	}

	private void updateTitle() {
		if (isNew) {
			setTitle("메모 추가");
		} else if (editMode) {
			setTitle("메모 수정");
		} else {
			setTitle("메모 보기");
		}
	}

	private void updateModeUI() {
		toolbar.setVisible(editMode);
		editor.setEditable(editMode);
		saveButton.setVisible(editMode);
		editButton.setVisible(!editMode && !isNew);
		closeButton.setVisible(true); // 항상 닫기

		// 수정모드 시 바로 커서 포커스 (윈도우 오픈 후에도 한 번 더 보장)
		if (editMode) {
			SwingUtilities.invokeLater(() -> editor.requestFocusInWindow());
		}
	}

	// HTML 세팅 유틸
	private void setEditorText(String html) {
		String processedHtml = html;
		if (processedHtml != null && !processedHtml.isBlank()) {
			if (!processedHtml.toLowerCase().contains("<html")) {
				processedHtml = "<html><body><pre>" + processedHtml + "</pre></body></html>";
			}
		} else {
			processedHtml = "<html><body><pre></pre></body></html>";
		}
		editor.setText(processedHtml);
	}

	private JToolBar createToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(createStyleButton("B", "굵게", this::toggleBold));
		toolbar.add(createStyleButton("I", "기울임", this::toggleItalic));
		toolbar.add(createStyleButton("U", "밑줄", this::toggleUnderline));
		toolbar.addSeparator();
		// 글꼴 선택 콤보박스 추가
		toolbar.add(createFontFamilyComboBox());
		// 글꼴 크기 콤보박스 추가
		toolbar.add(createFontSizeComboBox());
		toolbar.add(createButton("글자색", this::chooseColor));
		toolbar.addSeparator();
		toolbar.add(createButton("이미지 삽입", this::insertImage));
		return toolbar;
	}

	private JComboBox<String> createFontFamilyComboBox() {
		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		JComboBox<String> fontFamilyBox = new JComboBox<>(fontNames);
		fontFamilyBox.setSelectedItem("Malgun Gothic"); // 기본 글꼴 설정
		fontFamilyBox.addActionListener(e -> {
			String selectedFont = (String) fontFamilyBox.getSelectedItem();
			setFontFamily(selectedFont);
		});
		return fontFamilyBox;
	}

	private JComboBox<Integer> createFontSizeComboBox() {
		Integer[] fontSizes = { 8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72 };
		JComboBox<Integer> fontSizeBox = new JComboBox<>(fontSizes);
		fontSizeBox.setSelectedItem(12); // 기본 글꼴 크기 설정
		fontSizeBox.addActionListener(e -> {
			Integer selectedSize = (Integer) fontSizeBox.getSelectedItem();
			setFontSize(selectedSize);
		});
		return fontSizeBox;
	}

	private JButton createButton(String text, Runnable onClick) {
		JButton button = new JButton(text);
		button.addActionListener(e -> onClick.run());
		return button;
	}

	private JButton createStyleButton(String text, String tooltip, Runnable onClick) {
		JButton button = new JButton(text);
		button.setToolTipText(tooltip);
		button.addActionListener(e -> onClick.run());
		return button;
	}

	// 스타일 기능들
	private void toggleBold() {
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setBold(attr, !StyleConstants.isBold(getCharacterAttributes()));
		setCharacterAttributes(attr, false);
	}

	private void toggleItalic() {
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setItalic(attr, !StyleConstants.isItalic(getCharacterAttributes()));
		setCharacterAttributes(attr, false);
	}

	private void toggleUnderline() {
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setUnderline(attr, !StyleConstants.isUnderline(getCharacterAttributes()));
		setCharacterAttributes(attr, false);
	}

	private void setFontFamily(String fontFamily) {
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attr, fontFamily);
		setCharacterAttributes(attr, false);
	}

	private void setFontSize(int fontSize) {
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontSize(attr, fontSize);
		setCharacterAttributes(attr, false);
	}

	private void chooseColor() {
		Color color = JColorChooser.showDialog(this, "글자색 선택", Color.BLACK);
		if (color != null) {
			MutableAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setForeground(attr, color);
			setCharacterAttributes(attr, false);
		}
	}

	private AttributeSet getCharacterAttributes() {
		StyledEditorKit kit = (StyledEditorKit) editor.getEditorKit();
		return kit.getInputAttributes();
	}

	private void setCharacterAttributes(AttributeSet attr, boolean replace) {
		int start = editor.getSelectionStart();
		int end = editor.getSelectionEnd();
		if (start != end) {
			HTMLDocument doc = (HTMLDocument) editor.getDocument();
			doc.setCharacterAttributes(start, end - start, attr, replace);
		} else {
			StyledEditorKit kit = (StyledEditorKit) editor.getEditorKit();
			MutableAttributeSet inputAttributes = kit.getInputAttributes();
			if (replace)
				inputAttributes.removeAttributes(inputAttributes);
			inputAttributes.addAttributes(attr);
		}
	}

	// 이미지 삽입
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

			String imgTag = String.format(
					"<img src=\"%s\" width=\"100\" height=\"100\" align=\"left\" hspace=\"0\" vspace=\"5\" />", uri);
			HTMLDocument doc = (HTMLDocument) editor.getDocument();
			HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
			kit.insertHTML(doc, editor.getCaretPosition(), "<br>" + imgTag, 0, 0, HTML.Tag.IMG);

			editor.revalidate();
			editor.repaint();
			editor.setText(editor.getText());
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "이미지를 삽입하는 중 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	// 저장 및 닫기
	private void saveAndClose() {
		String html = editor.getText();
		if (imgHandler != null) {
			imgHandler.deleteRemovedImages(memo.getContent(), html);
		}
		onSave.accept(html);
		dispose();
	}
}