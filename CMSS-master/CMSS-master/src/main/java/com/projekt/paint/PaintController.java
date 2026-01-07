package com.projekt.paint;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import java.io.IOException;
import java.util.Stack;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import java.util.Optional;


public class PaintController {

    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider sizeSlider;
    @FXML private ComboBox<String> brushesCombo;
    @FXML private ToggleButton eraserButton;
    @FXML private Button undoButton;
    @FXML private Button redoButton;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private MenuButton menuButton;
    @FXML private MenuButton filtersMenu;
    @FXML private ScrollPane scrollPane;
    @FXML private Label sizeLabel;

    private GraphicsContext gc;
    private Tool currentTool;

    private final Stack<Image> undoHistory = new Stack<>();
    private final Stack<Image> redoHistory = new Stack<>();
    private final int MAX_HISTORY = 20; 

    // --- Zoom ---
    private double zoomFactor = 1.0;
    private final double ZOOM_STEP = 1.1;
    private final double MIN_ZOOM = 0.1;
    private final double MAX_ZOOM = 10.0;

    @FXML
    private void initialize() {

        // výchozí zobrazení
        brushesCombo.getItems().addAll("Pen", "Spray", "Highlighter", "Calligraphy", "Watercolor", "Oil Colors");
        brushesCombo.getSelectionModel().select("Pen");

        // výchozí barva
        colorPicker.setValue(Color.BLACK);

        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // výchozí nástroj
        currentTool = new PenTool(this);

        brushesCombo.setOnAction(e -> {
            String selected = brushesCombo.getValue();
            onToolSelected(selected);
        });

        // Initialize size slider listener
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sizeLabel.setText(String.valueOf((int) Math.round(newVal.doubleValue())) + "px");
        });

        // Uloží počáteční stav canvasu pro Undo
        saveStateForUndo();

        canvas.setOnMousePressed(e -> currentTool.onPress(gc, e.getX(), e.getY()));
        canvas.setOnMouseDragged(e -> currentTool.onDrag(gc, e.getX(), e.getY()));
        canvas.setOnMouseReleased(e -> {
            currentTool.onRelease(gc, e.getX(), e.getY());
            saveStateForUndo();
        });

        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.isControlDown()) {
                        switch (event.getCode()) {
                            case Z -> onUndo();
                            case Y -> onRedo();
                            case S -> onSaveImage();
                        }
                    }
                });
            }
        });

    }

    // Uloží aktuální stav pro Undo
    private void saveStateForUndo() {
        if (undoHistory.size() >= MAX_HISTORY) {
            undoHistory.remove(0);
        }
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        undoHistory.push(snapshot);
        redoHistory.clear(); // po nové akci se Redo historie maže
    }

    // --- Funkce UNDO ---
    @FXML
    private void onUndo() {
        if (undoHistory.size() > 1) {
            Image lastState = undoHistory.pop();
            redoHistory.push(lastState);
            Image previous = undoHistory.peek();
            gc.drawImage(previous, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    // --- Funkce REDO ---
    @FXML
    private void onRedo() {
        if (!redoHistory.isEmpty()) {
            Image next = redoHistory.pop();
            undoHistory.push(next);
            gc.drawImage(next, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    @FXML
    private void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        saveStateForUndo();
    }

    // Getter pro barvu a velikost (Tool třídy)
    public Color getCurrentColor() {
        return colorPicker.getValue();
    }

    public int getBrushSize() {
        return (int) Math.round(sizeSlider.getValue());
    }

    // --- ULOŽENÍ OBRÁZKU ---
    @FXML
    private void onSaveImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Uložit obrázek");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG obrázek", "*.png"),
                new FileChooser.ExtensionFilter("JPEG obrázek", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("BMP obrázek", "*.bmp"),
                new FileChooser.ExtensionFilter("WEBP obrázek", "*.webp")
        );
        fileChooser.setInitialFileName("obrazek.png");

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

                // Zjištění formátu podle přípony nebo filtru
                String fileName = file.getName().toLowerCase();
                String ext = "png"; // výchozí formát

                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    ext = "jpg";
                } else if (fileName.endsWith(".bmp")) {
                    ext = "bmp";
                } else if (fileName.endsWith(".webp")) {
                    ext = "webp";
                } else {
                    // Pokud nemá příponu, doplníme podle vybraného filtru
                    String selectedExt = fileChooser.getSelectedExtensionFilter().getExtensions().get(0).replace("*", "");
                    file = new File(file.getAbsolutePath() + selectedExt);
                    ext = selectedExt.replace(".", "");
                }

                ImageIO.write(bufferedImage, ext, file);
                System.out.println("Obrázek uložen jako: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Chyba při ukládání obrázku: " + e.getMessage());
            }
        }
    }

    // --- NAČTENÍ OBRÁZKU ---
    @FXML
    private void onLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otevřít obrázek");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Obrázky", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
                saveStateForUndo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --- ZAVŘENÍ APLIKACE ---
    @FXML
    private void onExitApp() {
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.close();
    }

    // --- Pomocné ---
    public WritableImage getCanvasSnapshot() {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        return snapshot;
    }

    public void setCurrentColor(Color color) {
        colorPicker.setValue(color);
    }

    @FXML
    private void onShowMessageWindow() {
        // Vytvoření nového okna
        Stage messageStage = new Stage();
        messageStage.setTitle("Informace");

        // Text, který se zobrazí
        Label textLabel = new Label( "About: \n" +
                                        "App version: 1.0.8 \n" +
                                        "Owners: Křivský Matěj \n" +
                                        "        Soldán Tomáš \n" +
                                        "        Vašek Martin\n" +
                                        "        Všianský Kryštof\n" +
                                        "Tester & Consultant: Rousek Ondřej");
        textLabel.getStyleClass().add("message-text");

        // Tlačítko pro zavření
        Button closeButton = new Button("Zavřít");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(e -> messageStage.close());

        // Rozložení (svislé)
        VBox layout = new VBox(15, textLabel, closeButton);
        layout.getStyleClass().add("message-window");
        layout.setAlignment(Pos.CENTER);

        // Scéna + připojení CSS stylu
        Scene scene = new Scene(layout, 300, 240);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        messageStage.setScene(scene);

        // Zamezí interakci s hlavním oknem (volitelné)
        messageStage.initModality(Modality.WINDOW_MODAL);
        messageStage.initOwner(canvas.getScene().getWindow());

        messageStage.show();
    }

    @FXML
    private void onApplyEffect(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String effectType = (String) source.getUserData();

        Function effect = switch (effectType) {
            case "negative" -> new NegativeFunction();
            case "blur" -> new BlurFunction(8);
            case "grayscale" -> new GrayscaleFunction();
            case "brighten" -> new BrightnessFunction(0.3);
            default -> null;
        };

        applyEffect(effect);
    }

    private void applyEffect(Function effect) {
        if (effect != null) {
            effect.apply(canvas, gc);
            saveStateForUndo(); // pokud máš undo systém
            System.out.println("Použit efekt: " + effect.getName());
        }
    }

    /**
     * Tato metoda se stará o přepnutí aktivního nástroje podle názvu.
     * Může být volána z libovolného místa (např. i z menu nebo tlačítka).
     */
    private void onToolSelected(String selected) {
        switch (selected) {
            // Hlavní nástroje
            case "Pen" -> currentTool = new PenTool(this);
            case "Spray" -> currentTool = new SprayTool(this);
            case "Highlighter" -> currentTool = new HighlighterTool(this);
            case "Calligraphy" -> currentTool = new CalligraphyTool(this);
            case "Watercolor" -> currentTool = new WatercolorTool(this);
            case "Oil Colors" -> currentTool = new OilColorsTool(this);

            default -> System.out.println("Neznámý nástroj: " + selected);
        }

        System.out.println("Aktuální nástroj: " + selected);
    }

    /**
     * Obnoví plátno podle posledního uloženého stavu (Undo historie).
     * Používá se např. při dočasných efektech jako lupa.
     */
    public void redrawCanvas() {
        if (!undoHistory.isEmpty()) {
            Image lastState = undoHistory.peek();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(lastState, 0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            // Pokud není historie, vyplní bílé pozadí
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    @FXML
    private void onChangeResolution() {
        // Vytvoření dialogu
        Dialog<Pair<Integer, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Změna rozlišení plátna");
        dialog.setHeaderText("Zadejte nové rozměry plátna:");

        // Tlačítka
        ButtonType applyButtonType = new ButtonType("Použít", ButtonBar.ButtonData.OK_DONE);
        ButtonType resetButtonType = new ButtonType("Obnovit výchozí", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, resetButtonType, ButtonType.CANCEL);

        // Pole pro šířku a výšku (jen čísla)
        TextField widthField = new TextField(String.valueOf((int) canvas.getWidth()));
        widthField.setPromptText("Šířka");
        widthField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) widthField.setText(newVal.replaceAll("[^\\d]", ""));
        });

        TextField heightField = new TextField(String.valueOf((int) canvas.getHeight()));
        heightField.setPromptText("Výška");
        heightField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) heightField.setText(newVal.replaceAll("[^\\d]", ""));
        });

        // Rozložení v mřížce
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Šířka:"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Výška:"), 0, 1);
        grid.add(heightField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Výchozí focus
        Platform.runLater(widthField::requestFocus);

        // Zpracování výsledku
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButtonType) {
                try {
                    int w = Integer.parseInt(widthField.getText());
                    int h = Integer.parseInt(heightField.getText());
                    return new Pair<>(w, h);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (dialogButton == resetButtonType) {
                return new Pair<>(1060, 600); // výchozí hodnoty
            }
            return null;
        });

        // Získání výsledku
        Optional<Pair<Integer, Integer>> result = dialog.showAndWait();

        result.ifPresent(size -> {
            int newWidth = size.getKey();
            int newHeight = size.getValue();

            WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, snapshot);

            canvas.setWidth(newWidth);
            canvas.setHeight(newHeight);

            gc.drawImage(snapshot, 0, 0, newWidth, newHeight);
            saveStateForUndo();
        });
    }

    @FXML
    private void onNewCanvas() {
        clearCanvas();
    }

    @FXML
    private void onOpenFile() {
        onLoadImage();
    }

    @FXML
    private void onSaveFile() {
        onSaveImage();
    }

    @FXML
    private void onSaveAsFile() {
        onSaveImage();
    }

    @FXML
    private void onImportImage() {
        onLoadImage();
    }

    @FXML
    private void onZoomIn() {
        if (zoomFactor < MAX_ZOOM) {
            zoomFactor *= ZOOM_STEP;
            applyZoom();
        }
    }

    @FXML
    private void onZoomOut() {
        if (zoomFactor > MIN_ZOOM) {
            zoomFactor /= ZOOM_STEP;
            applyZoom();
        }
    }

    private void applyZoom() {
        canvas.setScaleX(zoomFactor);
        canvas.setScaleY(zoomFactor);
        redrawCanvas();
        scrollPane.setHbarPolicy(zoomFactor > 1.0 ? ScrollPane.ScrollBarPolicy.ALWAYS : ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(zoomFactor > 1.0 ? ScrollPane.ScrollBarPolicy.ALWAYS : ScrollPane.ScrollBarPolicy.NEVER);
    }

    @FXML
    private void onEraserToggle() {
        currentTool = new EraserTool(this);
    }



}
