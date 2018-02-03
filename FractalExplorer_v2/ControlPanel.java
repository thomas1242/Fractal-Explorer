import java.awt.*;
import javax.swing.*;

public class ControlPanel extends JPanel {

    private ImageFrame imageFrame;

    public ControlPanel(ImageFrame imageFrame) {
        this.imageFrame = imageFrame;
        drawBackground();
        addComponents();
        setVisible(true);
        setOpaque(true);
    }

    private void drawBackground() {
        setBackground(new Color(70, 70, 70, 130));
        setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50, 0), 7));
    }

    private void addComponents() {
        setLayout(new GridLayout(0, 1));

        JButton juliaButton = createButton("Julia", Color.BLACK, 15);
        juliaButton.addActionListener(e -> {
            imageFrame.freshImage();
            imageFrame.Mandelbrot = false;
            imageFrame.Julia = true;
            imageFrame.Julia();
        });
        JButton mandelbrotButton = createButton("Mandelbrot", Color.BLACK, 15);
        mandelbrotButton.addActionListener(e -> {
            imageFrame.freshImage();
            imageFrame.Mandelbrot = true;
            imageFrame.Julia = false;
            imageFrame.Mandelbrot();
        });
        JButton saveImage = createButton("Save Image", Color.BLACK, 15);
        saveImage.addActionListener(e -> {
            imageFrame.saveImage();
        });
        JButton configFrameRate = createButton("FrameRate", Color.BLACK, 15);
        configFrameRate.addActionListener(e -> {
            imageFrame.configureFPS(false);
        });

        add(mandelbrotButton);
        add(juliaButton);
        add(configFrameRate);
        add(saveImage);
    }

    private JButton createButton(String s, Color color, int fontSize) {
        JButton button = new JButton(s);
        button.setForeground(color);
        button.setFont(new Font("plain", Font.BOLD, fontSize));
        button.setOpaque(false);
        return button;
    }
}
