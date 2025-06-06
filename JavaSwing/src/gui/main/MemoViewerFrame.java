package gui.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import api.model.Memo;

/**
 * 메모 내용을 표시하는 뷰어 창.
 * <p>
 * 메모 카드 클릭 시 열리며, 메모 당 최대 1개만 열릴 수 있음. 수정 및 닫기 기능 제공.
 */
public class MemoViewerFrame extends JDialog {

	private final Memo memo;
	private final JEditorPane contentPane;
	private final Consumer<Memo> onEdit;
	private final Runnable onClose;

	/**
	 * 메모 뷰어 창 생성
	 * 
	 * @param owner   부모 윈도우
	 * @param memo    표시할 메모 객체
	 * @param onEdit  수정 버튼 클릭 시 호출될 콜백
	 * @param onClose 창이 닫힐 때 호출될 콜백
	 */
	public MemoViewerFrame(Window owner, Memo memo, Consumer<Memo> onEdit, Runnable onClose) {
		super(owner, "메모 보기", ModalityType.MODELESS);
		this.memo = memo;
		this.onEdit = onEdit;
		this.onClose = onClose;

		// 창 설정
		setSize(500, 400);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		// 내용 표시 영역
		contentPane = new JEditorPane();
		contentPane.setEditable(false);
		contentPane.setContentType("text/html");

		// HTML 스타일 설정
		HTMLEditorKit kit = new HTMLEditorKit();
		contentPane.setEditorKit(kit);
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("body { font-family: 'Malgun Gothic', sans-serif; font-size: 12px; margin: 10px; }");
		styleSheet.addRule("p { margin: 0 0 10px 0; }");

		// 내용 설정
		updateContent();

		// 스크롤 패널에 추가
		JScrollPane scrollPane = new JScrollPane(contentPane);
		add(scrollPane, BorderLayout.CENTER);

		// 버튼 패널
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

		JButton editButton = new JButton("수정");
		editButton.addActionListener(e -> {
			if (onEdit != null) {
				onEdit.accept(memo);
			}
		});

		JButton closeButton = new JButton("닫기");
		closeButton.addActionListener(e -> dispose());

		buttonPanel.add(editButton);
		buttonPanel.add(closeButton);
		add(buttonPanel, BorderLayout.SOUTH);

		// 창이 닫힐 때 콜백 호출
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (onClose != null) {
					onClose.run();
				}
			}
		});
	}

	/**
	 * 메모 내용 업데이트
	 */
	public void updateContent() {
		String content = memo.getContent();
		if (content == null || content.isBlank()) {
			content = "<html><body></body></html>";
		} else if (!content.toLowerCase().contains("<html")) {
			// 일반 텍스트인 경우 HTML로 변환
			content = "<html><body>" + content.replace("\n", "<br>") + "</body></html>";
		}
		contentPane.setText(content);
		contentPane.setCaretPosition(0); // 스크롤을 맨 위로
	}

	/**
	 * 메모 객체 반환
	 */
	public Memo getMemo() {
		return memo;
	}
}
