
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GameLogic {

    // Panel constants
    public static final int WIDTH = 400;
    public static final int HEIGHT = 600;

    private final Pane root;
    private final Label scoreLabel;

    private final Random random = new Random();

    // Game state
    private boolean left;
    private boolean right;
    private boolean gameRunning;

    // Entities/state
    private Polygon spaceship;
    private final ArrayList<Circle> asteroids = new ArrayList<>();
    private final ArrayList<Rectangle> bullets = new ArrayList<>();

    // Scoring/difficulty
    private int score;
    private int asteroidSpeed = 3;
    private int spawnRate = 50;

    private long startTime;
    private long lastScoreTime;

    // Bullet cooldown (0.5s)
    private long lastShotTimeMs = 0;
    private static final long SHOT_COOLDOWN_MS = 500;

    // Highscore persistence
    private static final int MAX_HIGHSCORES = 5;
    private static final String HIGHSCORE_FILE = "highscore.txt";

    public GameLogic(Pane root, Label scoreLabel) {
        this.root = root;
        this.scoreLabel = scoreLabel;
    }

    public void setInput(boolean left, boolean right) {
        this.left = left;
        this.right = right;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
    }

    public Polygon getSpaceship() {
        return spaceship;
    }

    public int getScore() {
        return score;
    }

    public void resetGame() {
        root.getChildren().clear();
        asteroids.clear();
        bullets.clear();

        // Background image
        javafx.scene.image.Image bg = BackgroundAssets.loadBackgroundImage();
        BackgroundUtil.ensureBackground(root, bg, WIDTH, HEIGHT);

        // Collision bounds polygon (visual sprite rendered separately)
        spaceship = EntitiesFactory.createSpaceship(WIDTH, HEIGHT);

        // Render ship sprite
        Image shipImg = EntitiesFactory.loadShipImage();
        System.out.println("SHIP IMG=" + shipImg + " w=" + (shipImg == null ? -1 : shipImg.getWidth()) + " h=" + (shipImg == null ? -1 : shipImg.getHeight()));
        if (shipImg != null && shipImg.getWidth() > 0 && shipImg.getHeight() > 0) {
            ImageView shipView = new ImageView(shipImg);

            shipView.setFitWidth(90);
            shipView.setFitHeight(75);

            shipView.setPreserveRatio(true);

            // Bind sprite position to collision polygon so it moves together
            shipView.layoutXProperty().bind(spaceship.layoutXProperty());
            shipView.layoutYProperty().bind(spaceship.layoutYProperty());

            root.getChildren().add(shipView);
            shipView.toFront();
            // keep polygon for collisions (it is invisible per EntitiesFactory)

            root.getChildren().add(spaceship);

        } else {
            // Fallback: show triangle ship so game is playable
            spaceship.setOpacity(1);
            spaceship.setFill(Color.CYAN);
            spaceship.setStroke(Color.CYAN);
            root.getChildren().add(spaceship);
        }

        score = 0;
        scoreLabel.setText("Score: 0");
        root.getChildren().add(scoreLabel);

        startTime = System.currentTimeMillis();
        lastScoreTime = startTime;
        lastShotTimeMs = 0;

        asteroidSpeed = 3;
        spawnRate = 50;

        gameRunning = true;
        left = false;
        right = false;
    }

    public void tick() {
        if (!gameRunning) {
            return;
        }

        moveSpaceship();
        updateDifficulty();
        spawnAsteroid();
        moveAsteroids();
        moveBullets();
        checkBulletCollision();
        updateTimeScore();
    }

    private void moveSpaceship() {
        double x = spaceship.getLayoutX();
        double minX = 0;
        double maxX = WIDTH - 50;

        if (left && x > minX) {
            spaceship.setLayoutX(x - 5);
        }

        if (right && spaceship.getLayoutX() < maxX) {
            spaceship.setLayoutX(spaceship.getLayoutX() + 5);
        }
    }

    public void tryShoot(long nowMs) {
        if (!gameRunning) {
            return;
        }
        if (nowMs - lastShotTimeMs < SHOT_COOLDOWN_MS) {
            return;
        }

        Rectangle bullet = new Rectangle(5, 15, Color.YELLOW);
        bullet.setX(spaceship.getLayoutX() + 22);
        bullet.setY(spaceship.getLayoutY());

        bullets.add(bullet);
        root.getChildren().add(bullet);

        lastShotTimeMs = nowMs;
    }

    private void spawnAsteroid() {
        if (random.nextInt(spawnRate) == 0) {
            Circle asteroid = EntitiesFactory.createAsteroid(WIDTH, random);
            asteroids.add(asteroid);
            root.getChildren().add(asteroid);
        }
    }

    private void moveAsteroids() {
        Iterator<Circle> iter = asteroids.iterator();

        while (iter.hasNext()) {
            Circle asteroid = iter.next();

            asteroid.setCenterY(asteroid.getCenterY() + asteroidSpeed);

            if (asteroid.getBoundsInParent().intersects(spaceship.getBoundsInParent())) {
                gameRunning = false;
                return;
            }

            if (asteroid.getCenterY() > HEIGHT) {
                root.getChildren().remove(asteroid);
                iter.remove();
            }
        }
    }

    private void moveBullets() {
        Iterator<Rectangle> iter = bullets.iterator();

        while (iter.hasNext()) {
            Rectangle bullet = iter.next();

            bullet.setY(bullet.getY() - 6);

            if (bullet.getY() < 0) {
                root.getChildren().remove(bullet);
                iter.remove();
            }
        }
    }

    private void checkBulletCollision() {
        Iterator<Circle> aIter = asteroids.iterator();

        while (aIter.hasNext()) {
            Circle asteroid = aIter.next();

            Iterator<Rectangle> bIter = bullets.iterator();

            while (bIter.hasNext()) {
                Rectangle bullet = bIter.next();

                if (asteroid.getBoundsInParent().intersects(bullet.getBoundsInParent())) {
                    root.getChildren().removeAll(asteroid, bullet);

                    aIter.remove();
                    bIter.remove();

                    score += 10;
                    scoreLabel.setText("Score: " + score);
                    break;
                }
            }
        }
    }

    private void updateTimeScore() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastScoreTime >= 1000) {
            score += 1;
            scoreLabel.setText("Score: " + score);
            lastScoreTime = currentTime;
        }
    }

    private void updateDifficulty() {
        long elapsed = System.currentTimeMillis() - startTime;
        long seconds = elapsed / 1000;

        asteroidSpeed = 3 + (int) (seconds / 10);
        spawnRate = Math.max(15, 50 - (int) (seconds / 5));
    }

    // Highscore persistence helpers
    public void recordHighscore(int scoreToRecord) {
        List<Integer> scores = loadHighscores();
        scores.add(scoreToRecord);
        scores.sort(Collections.reverseOrder());

        if (scores.size() > MAX_HIGHSCORES) {
            scores = new ArrayList<>(scores.subList(0, MAX_HIGHSCORES));
        }

        saveHighscores(scores);
    }

    public List<Integer> loadHighscores() {
        Path path = Paths.get(HIGHSCORE_FILE);

        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try {
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return new ArrayList<>();
            }

            String[] parts = content.split(",");
            List<Integer> scores = new ArrayList<>();
            for (String p : parts) {
                if (p == null || p.trim().isEmpty()) {
                    continue;
                }
                scores.add(Integer.parseInt(p.trim()));
            }

            scores.sort(Collections.reverseOrder());
            return scores;
        } catch (IOException | NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    private void saveHighscores(List<Integer> scores) {
        Path path = Paths.get(HIGHSCORE_FILE);

        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < scores.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(scores.get(i));
            }
            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
