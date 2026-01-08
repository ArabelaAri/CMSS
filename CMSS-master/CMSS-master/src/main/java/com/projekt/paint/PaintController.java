package com.projekt.paint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.Stack;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;


public class PaintController {

    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider sizeSlider;
    @FXML private ComboBox<String> brushesCombo;
    @FXML private ScrollPane scrollPane;
    @FXML private Label sizeLabel;

    private GraphicsContext gc;
    private Tool currentTool;

    private final Stack<Image> undoHistory = new Stack<>();
    private final Stack<Image> redoHistory = new Stack<>();
    private final int MAX_HISTORY = 20; 

    // --- Zoom ---
    private double zoomFactor = 1.0;
    private final double ZOOM_STEP = 0.2;
    private final double MIN_ZOOM = 0.5;
    private final double MAX_ZOOM = 5.0;
    
    // Zoom anchor point (for zooming towards cursor/touch)
    private double zoomAnchorX = 0;
    private double zoomAnchorY = 0;

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

        // Initialize size slider listener with better formatting
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int size = (int) Math.round(newVal.doubleValue());
            sizeLabel.setText(size + " px");
        });
        // Set initial label
        sizeLabel.setText((int)sizeSlider.getValue() + " px");

        // Uloží počáteční stav canvasu pro Undo
        saveStateForUndo();

        canvas.setOnMousePressed(e -> currentTool.onPress(gc, e.getX(), e.getY()));
        canvas.setOnMouseDragged(e -> currentTool.onDrag(gc, e.getX(), e.getY()));
        canvas.setOnMouseReleased(e -> {
            currentTool.onRelease(gc, e.getX(), e.getY());
            saveStateForUndo();
        });

        // Podpora zoomu kolečkem myši
        canvas.setOnScroll(event -> {
            event.consume();
            
            // získání pozice myši a určení směru zoomu
            double mouseX = event.getX();
            double mouseY = event.getY();
            double zoomDirection = event.getDeltaY() > 0 ? 1 : -1;
            double zoomChange = zoomDirection * ZOOM_STEP * 0.5; // menší krok pro jemnější zoom
            
            // Spočítání nového zoom faktoru
            double oldZoom = zoomFactor;
            zoomFactor = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomFactor + zoomChange));
            
            if (oldZoom != zoomFactor) {
                // uložení pozice myši jako anchor pointu
                zoomAnchorX = mouseX;
                zoomAnchorY = mouseY;
                applyZoom();
            }
        });

        // popravena podpora zoomu pomocí gesto pinch-to-zoom (dotykové obrazovky)
        canvas.setOnZoom(event -> {
            event.consume();
            
            double oldZoom = zoomFactor;
            zoomFactor = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomFactor * event.getZoomFactor()));
            
            if (oldZoom != zoomFactor) {
                zoomAnchorX = event.getX();
                zoomAnchorY = event.getY();
                applyZoom();
            }
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
        redoHistory.clear(); // Vymaže redo historii při novém tahu
    }

    @FXML
    private void onUndo() {
        if (undoHistory.size() > 1) {
            Image lastState = undoHistory.pop();
            redoHistory.push(lastState);
            Image previous = undoHistory.peek();
            gc.drawImage(previous, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

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

    // Uložení obrázku
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

    // Načtení obrázku
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

    @FXML
    private void onExitApp() {
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.close();
    }

    // Pomocná metoda pro získání snapshotu canvasu
    public WritableImage getCanvasSnapshot() {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        return snapshot;
    }

    public void setCurrentColor(Color color) {
        colorPicker.setValue(color);
    }

    @FXML
    private void onAbout() {
        // Vytvoření okna s informacemi o apliaci a týmu
        Stage messageStage = new Stage();
        messageStage.setTitle("About");

        TextArea textArea = new TextArea( 
                                    "Application informations:\n" +
                                        "\n"+
                                        "Date of last update: 8.1.2026\n" +
                                        "Version: 1.0\n" +
                                        "\n"+
                                    "Our team: \n" +
                                        "\n"+
                                        "Adéla Kozinová \n" +
                                        "Lucie Fišerová\n" +
                                        "Adam Fendrich\n" +
                                        "\n"+
                                        "   Our team is made up of inexperienced fresh adult students from the same school. Despite lacking experience, we have the advantage that our skills and knowledge are frowing rapidly and this application is growing with us. Thank you for using our application and we wish you many successful creations.\n" +
                                        "\n"+
                                    "Developed as a school project for the subject programming.\n");
                                        
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("message-text");

        // Rozložení 
        VBox layout = new VBox(15, textArea);
        layout.getStyleClass().add("message-window");
        layout.setAlignment(Pos.CENTER);

        // Scéna a CSS
        Scene scene = new Scene(layout, 700, 280);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        messageStage.setScene(scene);
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
            saveStateForUndo();     
            System.out.println("Použit efekt: " + effect.getName());
        }
    }

    // Metoda pro přepnutí aktivního nástroje podle názvu 
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

    // Obnoví plátno podle posledního uloženého stavu (Undo historie).
     
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
            zoomAnchorX = canvas.getWidth() / 2;
            zoomAnchorY = canvas.getHeight() / 2;
            zoomFactor = Math.min(zoomFactor + ZOOM_STEP, MAX_ZOOM);
            applyZoom();
        }
    }

    @FXML
    private void onZoomOut() {
        if (zoomFactor > MIN_ZOOM) {
            zoomAnchorX = canvas.getWidth() / 2;
            zoomAnchorY = canvas.getHeight() / 2;
            zoomFactor = Math.max(zoomFactor - ZOOM_STEP, MIN_ZOOM);
            applyZoom();
        }
    }

    private void applyZoom() {
        // Calculate the position of the anchor point before zoom
        double oldScale = canvas.getScaleX();
        
        canvas.setScaleX(zoomFactor);
        canvas.setScaleY(zoomFactor);
        
        // Adjust translation to zoom towards the anchor point
        double scaledWidth = canvas.getWidth() * zoomFactor;
        double scaledHeight = canvas.getHeight() * zoomFactor;
        
        // Calculate how much to translate to keep the anchor point stationary
        double deltaX = (zoomFactor - 1) * (canvas.getWidth() / 2 - zoomAnchorX);
        double deltaY = (zoomFactor - 1) * (canvas.getHeight() / 2 - zoomAnchorY);
        
        canvas.setTranslateX(deltaX);
        canvas.setTranslateY(deltaY);
        
        // Show scrollbars only when zoomed in
        if (zoomFactor > 1.0) {
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        } else {
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        }
        
        System.out.println("Zoom: " + Math.round(zoomFactor * 100) + "%");
    }

    @FXML
    private void onEraserToggle() {
        currentTool = new EraserTool(this);
    }

    // --- NOVÁ FUNKCIONALITA: Generování náhodných tvarů ---
    @FXML
    private void onGenerateRandomShapes() {
        Random random = new Random();
        int count = 3; // Počet obrazců
        double w = canvas.getWidth();
        double h = canvas.getHeight();
 
        for (int i = 0; i < count; i++) {
            Color color = Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble());
            gc.setFill(color);
            gc.setStroke(color);
            gc.setLineWidth(2 + random.nextDouble() * 5);
 
            // Výběr z 5 druhů
            int shapeType = random.nextInt(5);
            double size = 40 + random.nextDouble() * 80;
            double x = random.nextDouble() * (w - size);
            double y = random.nextDouble() * (h - size);
 
            switch (shapeType) {
                case 0 -> gc.fillOval(x, y, size, size); // Kruh
                case 1 -> gc.fillRect(x, y, size, size); // Obdélník
                case 2 -> gc.strokeLine(x, y, random.nextDouble() * w, random.nextDouble() * h); // Čára
                case 3 -> drawRandomTriangle(x, y, size, random); // Trojúhelník
                case 4 -> drawRandomStar(x + size/2, y + size/2, random.nextInt(3,7), size/2, size/4, random); // Hvězda
            }
        }
        saveStateForUndo();
    }
 
    // Pomocná metoda pro vykreslení trojúhelníku
    private void drawRandomTriangle(double x, double y, double size, Random r) {
        double[] xPoints = {x, x + size, x + r.nextDouble() * size};
        double[] yPoints = {y + size, y + size, y};
        gc.fillPolygon(xPoints, yPoints, 3);
    }
 
    // Pomocná metoda pro vykreslení hvězdy
    private void drawRandomStar(double centerX, double centerY, int arms, double outerRadius, double innerRadius, Random r) {
        double[] xPoints = new double[arms * 2];
        double[] yPoints = new double[arms * 2];
        double angleStep = Math.PI / arms;
 
        for (int i = 0; i < arms * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = centerX + Math.cos(i * angleStep) * radius;
            yPoints[i] = centerY + Math.sin(i * angleStep) * radius;
        }
        gc.fillPolygon(xPoints, yPoints, arms * 2);
    }    
}