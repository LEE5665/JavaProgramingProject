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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class FileSystemImageHandler {
	private final Path assetsDir;
	private final Path imagesDir;
	private final Pattern imgPattern = Pattern
			.compile("<img[^>]+src=[\"'](?:(?:file:.*?/assets/images/)|(?:assets/images/))([^\"']+)[\"']");

	/**
	 * 이전 HTML과 새 HTML을 비교하여 제거된 이미지 파일을 삭제합니다.
	 * 
	 * @param oldHtml 이전 HTML 내용
	 * @param newHtml 새 HTML 내용
	 */
	public void deleteRemovedImages(String oldHtml, String newHtml) {
		if (oldHtml == null || oldHtml.isBlank())
			return;
			
		// 이전 HTML에서 이미지 파일명 추출
		Matcher oldMatcher = imgPattern.matcher(oldHtml);
		while (oldMatcher.find()) {
			String filename = oldMatcher.group(1);
			
			// 새 HTML에 해당 이미지가 없으면 파일 삭제
			if (newHtml == null || !newHtml.contains(filename)) {
				Path imgFile = imagesDir.resolve(filename);
				try {
					Files.deleteIfExists(imgFile);
					System.out.println("이미지 파일 삭제: " + filename);
				} catch (IOException ignored) {
					System.err.println("이미지 파일 삭제 실패: " + filename);
				}
			}
		}
	}

	public FileSystemImageHandler() throws IOException {
		try {
			Path base = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
			assetsDir = base.resolve("assets");
			imagesDir = assetsDir.resolve("images");
			if (!Files.exists(imagesDir))
				Files.createDirectories(imagesDir);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
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
		return filename;
	}

	public void deleteImagesInMarkdown(String html) {
		if (html == null || html.isBlank())
			return;
		Matcher m = imgPattern.matcher(html);
		while (m.find()) {
			String filename = m.group(1);
			Path imgFile = imagesDir.resolve(filename);
			try {
				Files.deleteIfExists(imgFile);
			} catch (IOException ignored) {
			}
		}
	}

	public Path getImagesDir() {
		return imagesDir;
	}
}
