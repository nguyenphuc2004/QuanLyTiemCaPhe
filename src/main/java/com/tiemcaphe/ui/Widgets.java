package com.tiemcaphe.ui;

import com.tiemcaphe.util.Dates;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Predicate;

/** Cấu hình nhanh cho vài control hay dùng. */
public final class Widgets {

    private Widgets() {
    }

    /** DatePicker hiển thị dd/MM/yyyy bất kể ngôn ngữ hệ điều hành. */
    public static void datePicker(DatePicker dp) {
        dp.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate d) {
                return d == null ? "" : d.format(Dates.DISPLAY_DATE);
            }

            @Override
            public LocalDate fromString(String s) {
                if (s == null || s.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(s.trim(), Dates.DISPLAY_DATE);
                } catch (DateTimeParseException e) {
                    return dp.getValue();
                }
            }
        });
        dp.setPromptText("dd/MM/yyyy");
    }

    /** Cột cuối co giãn theo bề rộng bảng. */
    public static void flex(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    /** Gắn style class cho dòng thỏa điều kiện (vd: tô đỏ nguyên liệu sắp hết). */
    public static <S> void rowStyle(TableView<S> table, String styleClass, Predicate<S> when) {
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(S item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove(styleClass);
                if (!empty && item != null && when.test(item)) {
                    getStyleClass().add(styleClass);
                }
            }
        });
    }

    /** Ảnh mặc định cho món / nguyên liệu chưa có hình: ly cà phê màu nâu nhạt trên nền kem. */
    public static javafx.scene.image.Image renderPlaceholder(int size) {
        javafx.scene.shape.SVGPath svg = new javafx.scene.shape.SVGPath();
        svg.setContent(Icons.COFFEE);
        svg.setFill(javafx.scene.paint.Color.web("#B9A79C"));
        double scale = size * 0.45 / 24.0;
        svg.setScaleX(scale);
        svg.setScaleY(scale);
        javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane(svg);
        pane.setMinSize(size, size);
        pane.setPrefSize(size, size);
        pane.setMaxSize(size, size);
        pane.setStyle("-fx-background-color: #EFE6DF; -fx-background-radius: 12px;");
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        return pane.snapshot(params, null);
    }

    public static Label badge(String text, String variant) {
        Label l = new Label(text);
        l.getStyleClass().setAll("badge", variant);
        return l;
    }
}
