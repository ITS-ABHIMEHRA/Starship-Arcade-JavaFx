
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.util.Random;

public final class EntitiesFactory {

    private static final String SHIP_PNG_RESOURCE = "/output-onlinepngtools.png";

    private EntitiesFactory() {
    }

    public static Image loadShipImage() {
        try {
            java.net.URL url = EntitiesFactory.class.getResource(SHIP_PNG_RESOURCE);
            if (url == null) {
                System.out.println("SHIP PNG resource not found: " + SHIP_PNG_RESOURCE);
                return null;
            }
            System.out.println("SHIP PNG loaded from: " + url);
            return new Image(url.toExternalForm(), 0, 0, true, true);

        } catch (Exception ex) {
            return null;
        }
    }

    public static Polygon createSpaceship(int width, int height) {
        // Collision bounds polygon (visual sprite is rendered separately in GameLogic)
        double side = 70.0;
        double triHeight = side * Math.sqrt(3) / 2.0;

        Polygon ship = new Polygon(
                0.0, triHeight,
                side / 2.0, 0.0,
                side, triHeight
        );

        // Collision bounds only: keep polygon invisible.
        ship.setFill(javafx.scene.paint.Color.TRANSPARENT);
        ship.setStroke(javafx.scene.paint.Color.TRANSPARENT);
        ship.setOpacity(0);

        ship.setLayoutX(width / 2.0 - 25);
        ship.setLayoutY(height - 90);
        return ship;
    }

    public static Circle createAsteroid(int panelWidth, Random random) {
        Circle asteroid = new Circle(20, javafx.scene.paint.Color.BLACK);
        asteroid.setCenterX(random.nextInt(panelWidth - 40) + 20);
        asteroid.setCenterY(0);
        return asteroid;
    }
}
