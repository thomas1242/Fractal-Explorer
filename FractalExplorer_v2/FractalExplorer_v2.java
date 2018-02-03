import java.awt.*;
import javax.swing.*;

public class FractalExplorer_v2 {
    private static final int WIDTH  = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth() * .75);
    private static final int HEIGHT = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight() * .75);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new ImageFrame(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
