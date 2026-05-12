
import javafx.scene.image.Image;

public final class BackgroundAssets {

    private BackgroundAssets() {
    }

    private static final String BACKGROUND_RESOURCE = "/background.jpg";

    public static Image loadBackgroundImage() {
        try {
            java.net.URL url = BackgroundAssets.class.getResource(BACKGROUND_RESOURCE);
            if (url == null) {
                System.out.println("Background PNG resource not found: " + BACKGROUND_RESOURCE);
                return null;
            }
            return new Image(url.toExternalForm(), 0, 0, true, true);
        } catch (Exception e) {
            return null;
        }
    }
}
