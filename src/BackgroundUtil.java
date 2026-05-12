
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public final class BackgroundUtil {

    private BackgroundUtil() {
    }

    public static ImageView createScaledBackground(Image bg, double width, double height) {
        if (bg == null) {
            return null;
        }

        ImageView view = new ImageView(bg);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        return view;
    }

    public static void ensureBackground(Pane root, Image bg, double width, double height) {
        if (bg == null) {
            return;
        }

        // remove existing background(s)
        root.getChildren().removeIf(n -> n instanceof ImageView && n.getUserData() != null && n.getUserData().toString().equals("BACKGROUND"));

        ImageView view = createScaledBackground(bg, width, height);
        if (view == null) {
            return;
        }
        view.setUserData("BACKGROUND");
        view.setLayoutX(0);
        view.setLayoutY(0);
        root.getChildren().add(0, view);
    }
}
