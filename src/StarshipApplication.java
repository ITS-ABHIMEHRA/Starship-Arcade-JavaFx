
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class StarshipApplication extends Application {

    private static final int WIDTH = GameLogic.WIDTH;
    private static final int HEIGHT = GameLogic.HEIGHT;

    private Pane root;

    private Label scoreLabel;

    private GameLogic logic;

    private AnimationTimer gameTimer;

    @Override
    public void start(Stage stage) {

        root = new Pane();
        root.setStyle("-fx-background-color: grey;");

        // Background image (also visible on start/highscore screens)
        javafx.scene.image.Image bg = BackgroundAssets.loadBackgroundImage();
        BackgroundUtil.ensureBackground(root, bg, WIDTH, HEIGHT);

        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);

        scoreLabel = new Label("Score: 0");
        scoreLabel.setTextFill(Color.BLACK);
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);

        logic = new GameLogic(root, scoreLabel);

        showStartMenu();

        scene.setOnKeyPressed(e -> {

            if (!logic.isGameRunning()) {
                return;
            }

            if (e.getCode() == KeyCode.LEFT) {
                logic.setInput(true, false);
            }

            if (e.getCode() == KeyCode.RIGHT) {
                logic.setInput(false, true);
            }

            if (e.getCode() == KeyCode.SPACE) {
                logic.tryShoot(System.currentTimeMillis());
            }
        });

        scene.setOnKeyReleased(e -> {

            if (e.getCode() == KeyCode.LEFT) {
                logic.setInput(false, false);
            }

            if (e.getCode() == KeyCode.RIGHT) {
                logic.setInput(false, false);
            }
        });

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Only tick during the actual game.
                if (!logic.isGameRunning()) {
                    return;
                }

                logic.tick();

                // Collision ended the game.
                if (!logic.isGameRunning()) {
                    stop();
                    showGameOver();
                }
            }
        };

        gameTimer.start();

        stage.setTitle("Starship Dodge 🚀");
        stage.setScene(scene);
        stage.show();
    }

    // START MENU
    private void showStartMenu() {
        logic.setGameRunning(false);
        root.getChildren().clear();

        Label title = new Label("STARSHIP DODGE 🚀");
        title.setTextFill(Color.BLACK);
        title.setStyle("-fx-font-size: 24px;");
        title.setLayoutX(55);
        title.setLayoutY(160);

        double centerX = WIDTH / 2.0;

        Button startGameButton = new Button("START GAME");
        startGameButton.setPrefWidth(160);
        startGameButton.setLayoutX(centerX - 80);
        startGameButton.setLayoutY(240);
        startGameButton.setOnAction(e -> {
            startGame();
        });

        Button highscoreButton = new Button("HIGHSCORE");
        highscoreButton.setPrefWidth(160);
        highscoreButton.setLayoutX(centerX - 80);
        highscoreButton.setLayoutY(290);
        highscoreButton.setOnAction(e -> showHighscoreScreen());

        Button quitButton = new Button("QUIT");
        quitButton.setPrefWidth(160);
        quitButton.setLayoutX(centerX - 80);
        quitButton.setLayoutY(340);
        quitButton.setOnAction(e -> javafx.application.Platform.exit());

        // Ensure background exists for this UI
        BackgroundUtil.ensureBackground(root, BackgroundAssets.loadBackgroundImage(), WIDTH, HEIGHT);

        root.getChildren().addAll(title, startGameButton, highscoreButton, quitButton);

    }

    // START GAME
    private void startGame() {
        root.getChildren().clear();
        logic.resetGame();

        // spaceship + initial scoreLabel already added by logic.resetGame()
        scoreLabel.toFront();

        logic.setGameRunning(true);

        // Ensure loop continues after leaving menu/highscores.
        if (gameTimer != null) {
            gameTimer.start();
        }
    }

    private void showHighscoreScreen() {
        logic.setGameRunning(false);
        root.getChildren().clear();

        // Ensure background exists for this UI
        BackgroundUtil.ensureBackground(root, BackgroundAssets.loadBackgroundImage(), WIDTH, HEIGHT);

        Label title = new Label("HIGHSCORES");

        title.setTextFill(Color.BLACK);
        title.setStyle("-fx-font-size: 22px;");
        title.setLayoutX(145);
        title.setLayoutY(120);

        java.util.List<Integer> scores = logic.loadHighscores();

        Label l1 = new Label(scores.size() > 0 ? "1. " + scores.get(0) : "1.");
        Label l2 = new Label(scores.size() > 1 ? "2. " + scores.get(1) : "2.");
        Label l3 = new Label(scores.size() > 2 ? "3. " + scores.get(2) : "3.");
        Label l4 = new Label(scores.size() > 3 ? "4. " + scores.get(3) : "4.");
        Label l5 = new Label(scores.size() > 4 ? "5. " + scores.get(4) : "5.");

        for (Label l : new Label[]{l1, l2, l3, l4, l5}) {
            l.setTextFill(Color.BLACK);
            l.setStyle("-fx-font-size: 18px;");
        }

        l1.setLayoutX(145);
        l2.setLayoutX(145);
        l3.setLayoutX(145);
        l4.setLayoutX(145);
        l5.setLayoutX(145);

        l1.setLayoutY(190);
        l2.setLayoutY(220);
        l3.setLayoutY(250);
        l4.setLayoutY(280);
        l5.setLayoutY(310);

        Button back = new Button("BACK");
        back.setLayoutX(160);
        back.setLayoutY(380);
        back.setOnAction(e -> showStartMenu());

        root.getChildren().addAll(title, l1, l2, l3, l4, l5, back);
    }

    private void showGameOver() {
        logic.setGameRunning(false);

        // Record score and update highscore list
        logic.recordHighscore(logic.getScore());

        root.getChildren().clear();

        Label over = new Label("GAME OVER");
        over.setTextFill(Color.RED);
        over.setStyle("-fx-font-size: 24px;");
        over.setLayoutX(130);
        over.setLayoutY(240);

        Button restart = new Button("RESTART");
        restart.setLayoutX(150);
        restart.setLayoutY(300);
        restart.setOnAction(e -> startGame());

        Button backToMenu = new Button("BACK TO MENU");
        backToMenu.setLayoutX(130);
        backToMenu.setLayoutY(350);
        backToMenu.setOnAction(e -> showStartMenu());

        // Ensure background exists for this UI
        BackgroundUtil.ensureBackground(root, BackgroundAssets.loadBackgroundImage(), WIDTH, HEIGHT);

        root.getChildren().addAll(over, restart, backToMenu);

        // Restart timer by recreating game state; AnimationTimer will keep running but logic is off.
    }

    public static void main(String[] args) {
        launch();
    }
}
