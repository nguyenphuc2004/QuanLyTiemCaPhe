package com.tiemcaphe.ui;

import com.tiemcaphe.service.BusinessException;
import com.tiemcaphe.util.Dates;
import com.tiemcaphe.util.Images;
import com.tiemcaphe.util.Money;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/** Tiện ích giao diện dùng chung: nạp FXML, ô nhập số, cột bảng định dạng sẵn. */
public final class Fx {

    public record Loaded<T>(Parent root, T controller) {
    }

    private Fx() {
    }

    public static <T> Loaded<T> load(String fxml) {
        FXMLLoader loader = new FXMLLoader(Fx.class.getResource(fxml));
        if (loader.getLocation() == null) {
            throw new IllegalStateException("Không tìm thấy giao diện " + fxml);
        }
        try {
            Parent root = loader.load();
            return new Loaded<>(root, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException("Không tải được giao diện " + fxml + ": " + e.getMessage(), e);
        }
    }

    public static String css() {
        return Objects.requireNonNull(Fx.class.getResource("/css/app.css")).toExternalForm();
    }

    // ---------- Ô nhập ----------

    public static void integerInput(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
        selectAllOnFocus(tf);
    }

    /** Chỉ cho nhập chữ số và dấu chấm/phẩy; khi rời ô sẽ tự định dạng 25.000. */
    public static void moneyInput(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[\\d.,\\s]*") ? c : null));
        tf.focusedProperty().addListener((obs, was, is) -> {
            if (is) {
                Platform.runLater(tf::selectAll);
            } else {
                try {
                    long v = Money.parse(tf.getText());
                    tf.setText(v == 0 ? "" : Money.formatPlain(v));
                } catch (NumberFormatException ignored) {
                    // giữ nguyên để người dùng sửa
                }
            }
        });
    }

    public static long moneyValue(TextField tf, String fieldName) {
        try {
            return Money.parse(tf.getText());
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + " không hợp lệ.");
        }
    }

    public static void decimalInput(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[\\d.,]*") ? c : null));
        selectAllOnFocus(tf);
    }

    public static double decimalValue(TextField tf, String fieldName) {
        try {
            return Money.parseQty(tf.getText());
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + " không hợp lệ.");
        }
    }

    public static void selectAllOnFocus(TextField tf) {
        tf.focusedProperty().addListener((obs, was, is) -> {
            if (is) {
                Platform.runLater(tf::selectAll);
            }
        });
    }

    // ---------- Cột bảng ----------

    public static <S> void text(TableColumn<S, String> col, Function<S, String> f) {
        col.setCellValueFactory(d -> new ReadOnlyStringWrapper(f.apply(d.getValue())));
    }

    public static <S> void money(TableColumn<S, Number> col, ToLongFunction<S> f) {
        col.setCellValueFactory(d -> new ReadOnlyLongWrapper(f.applyAsLong(d.getValue())));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Money.format(item.longValue()));
            }
        });
        right(col);
    }

    public static <S> void qty(TableColumn<S, Number> col, ToDoubleFunction<S> f) {
        col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(f.applyAsDouble(d.getValue())));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Money.qty(item.doubleValue()));
            }
        });
        right(col);
    }

    public static <S> void integer(TableColumn<S, Number> col, ToIntFunction<S> f) {
        col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(f.applyAsInt(d.getValue())));
        right(col);
    }

    public static <S> void dateTime(TableColumn<S, String> col, Function<S, LocalDateTime> f) {
        col.setCellValueFactory(d -> new ReadOnlyStringWrapper(Dates.display(f.apply(d.getValue()))));
    }

    public static <S> void yesNo(TableColumn<S, Boolean> col, Predicate<S> f, String yes, String no) {
        col.setCellValueFactory(d -> new ReadOnlyBooleanWrapper(f.test(d.getValue())));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(item ? yes : no);
                badge.getStyleClass().setAll("badge", item ? "badge-success" : "badge-muted");
                setGraphic(badge);
                setText(null);
            }
        });
        center(col);
    }

    public static <S> void image(TableColumn<S, byte[]> col, Function<S, byte[]> f, double size) {
        col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(f.apply(d.getValue())));
        col.setCellFactory(c -> new TableCell<>() {
            private final ImageView view = new ImageView();

            {
                view.setFitWidth(size);
                view.setFitHeight(size);
                view.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(byte[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    view.setImage(Images.toImage(item));
                    setGraphic(view);
                }
            }
        });
        center(col);
        col.setSortable(false);
    }

    public static void right(TableColumn<?, ?> col) {
        if (!col.getStyleClass().contains("col-right")) {
            col.getStyleClass().add("col-right");
        }
    }

    public static void center(TableColumn<?, ?> col) {
        if (!col.getStyleClass().contains("col-center")) {
            col.getStyleClass().add("col-center");
        }
    }

    public static void placeholder(TableView<?> table, String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        table.setPlaceholder(l);
    }

    public static Button iconButton(String icon, String text, String... styleClasses) {
        Button b = new Button(text);
        b.setGraphic(Icons.icon(icon));
        b.getStyleClass().addAll(styleClasses);
        return b;
    }

    public static Window windowOf(Node n) {
        return n == null || n.getScene() == null ? null : n.getScene().getWindow();
    }
}
