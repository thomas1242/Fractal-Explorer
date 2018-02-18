import java.awt.*;
import javax.swing.*;

public class Main {
    private static final int WIDTH  = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight() * .7 * 4 / 3);
    private static final int HEIGHT = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight() * .7);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new ImageFrame(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}