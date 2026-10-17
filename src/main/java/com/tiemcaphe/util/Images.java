package com.tiemcaphe.util;

import javafx.scene.image.Image;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Đọc ảnh người dùng chọn, thu nhỏ rồi lưu dạng byte[] vào CSDL; chuyển byte[] thành {@link Image}.
 * Dùng ImageIO (java.desktop) để xử lý nên không cần javafx.swing.
 */
public final class Images {

    public static final int MAX_STORED_SIZE = 480;
    private static final float JPEG_QUALITY = 0.85f;
    private static final String PLACEHOLDER = "/img/placeholder-mon.png";

    private static final Map<byte[], Image> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static Image placeholder;

    private Images() {
    }

    /** Đọc file ảnh, thu nhỏ về tối đa {@link #MAX_STORED_SIZE}px và trả về dữ liệu để lưu. */
    public static byte[] readForStorage(File file) throws IOException {
        BufferedImage src = ImageIO.read(file);
        if (src == null) {
            throw new IOException("Không đọc được ảnh: " + file.getName());
        }
        return encode(scale(src, MAX_STORED_SIZE));
    }

    /** Đọc ảnh từ resource trong jar (dùng cho dữ liệu mẫu). Trả về null nếu không có. */
    public static byte[] readResourceForStorage(String resourcePath) {
        try (InputStream in = Images.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            BufferedImage src = ImageIO.read(in);
            return src == null ? null : encode(scale(src, MAX_STORED_SIZE));
        } catch (IOException e) {
            return null;
        }
    }

    /** byte[] → Image (có cache). Null hoặc lỗi → ảnh mặc định. */
    public static Image toImage(byte[] data) {
        if (data == null || data.length == 0) {
            return placeholder();
        }
        Image cached = CACHE.get(data);
        if (cached != null) {
            return cached;
        }
        Image img = new Image(new ByteArrayInputStream(data));
        if (img.isError()) {
            return placeholder();
        }
        CACHE.put(data, img);
        return img;
    }

    public static synchronized Image placeholder() {
        if (placeholder == null) {
            placeholder = resource(PLACEHOLDER);
        }
        return placeholder;
    }

    /** Thay ảnh mặc định (giao diện dựng một ảnh mềm mại hơn lúc khởi động). */
    public static synchronized void setPlaceholder(Image image) {
        if (image != null) {
            placeholder = image;
        }
    }

    public static Image resource(String path) {
        InputStream in = Images.class.getResourceAsStream(path);
        if (in == null) {
            throw new IllegalArgumentException("Không tìm thấy resource " + path);
        }
        return new Image(in);
    }

    private static BufferedImage scale(BufferedImage src, int max) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= max && h <= max) {
            return src;
        }
        double ratio = Math.min((double) max / w, (double) max / h);
        int nw = Math.max(1, (int) Math.round(w * ratio));
        int nh = Math.max(1, (int) Math.round(h * ratio));
        int type = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage out = new BufferedImage(nw, nh, type);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }

    private static byte[] encode(BufferedImage img) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (img.getColorModel().hasAlpha()) {
            ImageIO.write(img, "png", bos);
            return bos.toByteArray();
        }
        BufferedImage rgb = img;
        if (img.getType() != BufferedImage.TYPE_INT_RGB) {
            rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            ImageIO.write(rgb, "png", bos);
            return bos.toByteArray();
        }
        ImageWriter writer = writers.next();
        try (MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos)) {
            writer.setOutput(out);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(JPEG_QUALITY);
            }
            writer.write(null, new IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
        }
        return bos.toByteArray();
    }
}
