package gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class MemoEditorFrame extends JDialog {
	private final JEditorPane editor;
	private final Consumer<String> onSave;
	private final FileSystemImageHandler imgHandler;

	public MemoEditorFrame(Window owner, String title, String initialHtml, Consumer<String> onSave) {
		super(owner, title, ModalityType.APPLICATION_MODAL);
		this.onSave = onSave;

		try {
			this.imgHandler = new FileSystemImageHandler();
		} catch (IOException e) {
			throw new RuntimeException("이미지 핸들러 초기화 실패", e);
		}

		setLayout(new BorderLayout());
		setSize(600, 400);
		setLocationRelativeTo(owner);

		// HTML 에디터 설정
		editor = new JEditorPane();
		editor.setContentType("text/html");
		editor.setText(initialHtml != null && !initialHtml.isBlank() ? initialHtml : "<html><body></body></html>");
		editor.setEditable(true);
		editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		editor.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
		configureHtmlEditor();
		// 툴바 생성
		JToolBar toolbar = createToolbar();
		add(toolbar, BorderLayout.NORTH);

		// 에디터 스크롤 패널
		add(new JScrollPane(editor), BorderLayout.CENTER);

		// 버튼 패널
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		Stream.of(createButton("저장", this::saveAndClose), createButton("취소", () -> {
			onSave.accept(null);
			dispose();
		})).forEach(btnPanel::add);
		add(btnPanel, BorderLayout.SOUTH);
	}

	private JToolBar createToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		// 서식 관련 버튼들
		toolbar.add(createStyleButton("B", "굵게", this::toggleBold));
		toolbar.add(createStyleButton("I", "기울임", this::toggleItalic));
		toolbar.add(createStyleButton("U", "밑줄", this::toggleUnderline));
		toolbar.addSeparator();

		// 정렬 버튼들
		toolbar.add(createStyleButton("←", "왼쪽 정렬", () -> setAlignment(StyleConstants.ALIGN_LEFT)));
		toolbar.add(createStyleButton("↔", "가운데 정렬", () -> setAlignment(StyleConstants.ALIGN_CENTER)));
		toolbar.add(createStyleButton("→", "오른쪽 정렬", () -> setAlignment(StyleConstants.ALIGN_RIGHT)));
		toolbar.addSeparator();

		// 글자색 버튼
		JButton colorBtn = createButton("글자색", this::chooseColor);
		toolbar.add(colorBtn);
		toolbar.addSeparator();

		// 이미지 삽입 버튼
		toolbar.add(createButton("이미지 삽입", this::insertImage));

		return toolbar;
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

	private void toggleBold() {
		HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setBold(attr, !StyleConstants.isBold(getCharacterAttributes()));
		setCharacterAttributes(attr, false);
	}

	private void toggleItalic() {
		HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setItalic(attr, !StyleConstants.isItalic(getCharacterAttributes()));
		setCharacterAttributes(attr, false);
	}

	private void toggleUnderline() {
		HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setUnderline(attr, !StyleConstants.isUnderline(getCharacterAttributes()));
		setCharacterAttributes(attr, false);
	}

	private void setAlignment(int alignment) {
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setAlignment(attr, alignment);
		setParagraphAttributes(attr, false);
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
			if (replace) {
				inputAttributes.removeAttributes(inputAttributes);
			}
			inputAttributes.addAttributes(attr);
		}
	}

	private void setParagraphAttributes(AttributeSet attr, boolean replace) {
		int start = editor.getSelectionStart();
		int end = editor.getSelectionEnd();
		HTMLDocument doc = (HTMLDocument) editor.getDocument();
		doc.setParagraphAttributes(start, end - start, attr, replace);
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
			// ① 파일 저장
			String filename = imgHandler.storeImage(imgFile);

			// ② 에디터-루트 기준 상대 경로 생성 (예: assets/images/abcd1234.png)
			String relativeSrc = Paths.get("assets", "images", filename).toString().replace("\\", "/");

			// ③ <img> 태그 삽입 – 폭/높이 속성 제거, CSS로 처리
			HTMLDocument doc = (HTMLDocument) editor.getDocument();
			HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
			String imgTag = String.format("<img src=\"%s\"/>", relativeSrc);
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

	private void configureHtmlEditor() {
		// HTML 에디터 키트 설정
		HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
		HTMLDocument doc = (HTMLDocument) editor.getDocument();

		// 스타일시트 설정
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("body { font-family: 'Malgun Gothic', sans-serif; font-size: 12px; margin: 10px; }");
		styleSheet.addRule("p { margin: 0 0 10px 0; }");
		styleSheet.addRule("br { line-height: 150%; }");
		// ▶️ 이미지가 자동 줄바꿈 + 여백을 갖도록
		styleSheet.addRule("img { display:block; margin:6px 0; max-width:100%; height:auto; }");

		// ▶️ 에디터-기준 base URL 설정 → <img src="assets/images/…"> 같은 상대경로 사용 가능
		try {
			doc.setBase(new File(".").toURI().toURL());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
