package gui.main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class FileSystemImageHandler {
	private final Path assetsDir;
	private final Path imagesDir;

	// 이미지 경로를 추출하기 위한 정규식 패턴 개선
	// file:/// 형식과 상대 경로 형식 모두 매칭
	private final Pattern imgPattern = Pattern.compile(
			"<img[^>]+src=[\"'](?:(?:file:(?:/+)?.*?/assets/images/)|(?:assets/images/)|(?:.*?/assets/images/))([^\"'?#]+)[\"']");

	public FileSystemImageHandler() throws IOException {
		try {
			Path base = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
			assetsDir = base.resolve("assets");
			imagesDir = assetsDir.resolve("images");
			if (!Files.exists(imagesDir))
				Files.createDirectories(imagesDir);

//			System.out.println("이미지 디렉토리 경로: " + imagesDir.toAbsolutePath());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	/**
	 * 이전 HTML과 새 HTML을 비교하여 제거된 이미지 파일을 삭제합니다.
	 * 
	 * @param oldHtml 이전 HTML 내용
	 * @param newHtml 새 HTML 내용
	 */
	public void deleteRemovedImages(String oldHtml, String newHtml) {
		if (oldHtml == null || oldHtml.isBlank()) {
//            System.out.println("이전 HTML이 비어있어 이미지 삭제를 건너뜁니다.");
			return;
		}

//        System.out.println("이미지 삭제 기능 실행 - 이전 HTML과 새 HTML 비교");

		// 이전 HTML에서 이미지 파일명 추출
		List<String> oldImages = extractImageFilenames(oldHtml);
//        System.out.println("이전 HTML에서 발견된 이미지: " + oldImages);

		// 새 HTML에서 이미지 파일명 추출
		List<String> newImages = new ArrayList<>();
		if (newHtml != null && !newHtml.isBlank()) {
			newImages = extractImageFilenames(newHtml);
		}
//        System.out.println("새 HTML에서 발견된 이미지: " + newImages);

		// 이전 HTML에는 있지만 새 HTML에는 없는 이미지 파일 삭제
		for (String filename : oldImages) {
			if (!newImages.contains(filename)) {
				Path imgFile = imagesDir.resolve(filename);
				try {
					boolean deleted = Files.deleteIfExists(imgFile);
//                    System.out.println("이미지 파일 삭제 " + (deleted ? "성공" : "실패") + ": " + filename);
				} catch (IOException e) {
//                    System.err.println("이미지 파일 삭제 중 오류 발생: " + filename + " - " + e.getMessage());
				}
			}
		}
	}

	/**
	 * HTML 내용에서 이미지 파일명을 추출합니다.
	 * 
	 * @param html HTML 내용
	 * @return 추출된 이미지 파일명 목록
	 */
	private List<String> extractImageFilenames(String html) {
		List<String> filenames = new ArrayList<>();
		Matcher matcher = imgPattern.matcher(html);

		while (matcher.find()) {
			String filename = matcher.group(1);
			filenames.add(filename);
		}

		return filenames;
	}

	public String storeImage(File imageFile) throws IOException {
		if (imageFile == null || !imageFile.exists())
			throw new IOException("파일 없음");
		BufferedImage src = ImageIO.read(imageFile);
		int origW = src.getWidth();
		int origH = src.getHeight();
		double scale = 150.0 / Math.max(origW, origH);
		int newW = (int) Math.round(origW * scale);
		int newH = (int) Math.round(origH * scale);
		BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(src, 0, 0, newW, newH, null);
		g.dispose();
		String filename = UUID.randomUUID() + ".png";
		Path dest = imagesDir.resolve(filename);
		ImageIO.write(resized, "png", dest.toFile());
//		System.out.println("이미지 저장 완료: " + filename + " 경로: " + dest.toAbsolutePath());
		return filename;
	}

	public void deleteImagesInMarkdown(String html) {
		if (html == null || html.isBlank()) {
//			System.out.println("HTML이 비어있어 이미지 삭제를 건너뜁니다.");
			return;
		}

//		System.out.println("메모 삭제 시 이미지 파일 삭제 기능 실행");

		List<String> images = extractImageFilenames(html);
		System.out.println("HTML에서 발견된 이미지: " + images);

		for (String filename : images) {
			Path imgFile = imagesDir.resolve(filename);
			try {
				boolean deleted = Files.deleteIfExists(imgFile);
//				System.out.println("이미지 파일 삭제 " + (deleted ? "성공" : "실패") + ": " + filename);
			} catch (IOException e) {
//				System.err.println("이미지 파일 삭제 중 오류 발생: " + filename + " - " + e.getMessage());
			}
		}
	}

	public Path getImagesDir() {
		return imagesDir;
	}
}
