package gui.main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSystemImageHandler {
	private final Path assetsDir;
	private final Path imagesDir;
	private final Pattern imgPattern = Pattern.compile("<img[^>]+src=['\"]([^'\"]+)['\"]");

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
		String name = imageFile.getName();
		String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
		String uuidName = UUID.randomUUID() + ext;
		Path dest = imagesDir.resolve(uuidName);
		Files.copy(imageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
		return "assets/images/" + uuidName;
	}

	public void deleteImagesInMarkdown(String html) {
		if (html == null || html.isBlank())
			return;
		Matcher m = imgPattern.matcher(html);
		while (m.find()) {
			String src = m.group(1);
			if (src.startsWith("assets/images/")) {
				Path imgFile = imagesDir.resolve(src.substring("assets/images/".length()));
				try {
					Files.deleteIfExists(imgFile);
				} catch (IOException ignored) {
				}
			}
		}
	}

	public Path getImagesDir() {
		return imagesDir;
	}
}
