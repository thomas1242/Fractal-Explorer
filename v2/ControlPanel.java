import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

        JButton juliaButton = createButton("Julia", Color.BLACK, 13);
        juliaButton.addActionListener(e -> {
            imageFrame.freshImage();
            imageFrame.currentSet = "Julia";
            imageFrame.Julia();
        });
        JButton mandelbrotButton = createButton("Mandelbrot", Color.BLACK, 13);
        mandelbrotButton.addActionListener(e -> {
            imageFrame.freshImage();
            imageFrame.currentSet = "Mandelbrot";
            imageFrame.Mandelbrot();
        });
        JButton saveImage = createButton("Save Image", Color.BLACK, 13);
        saveImage.addActionListener(e -> imageFrame.saveImage());

        add(mandelbrotButton);
        add(juliaButton);
        add(saveImage);
        add(animationSpeedSlider());
        add(createZoomGranularitySlider());
    }

    private JPanel animationSpeedSlider() {
        JLabel label = createLabel(50 + " fps", new Color(0xffdddddd), 14);
        JSlider slider = createSlider(1, 100, 30);
        slider.addChangeListener(e -> {
            label.setText(slider.getValue() + " fps");
            imageFrame.configureFPS(slider.getValue());
        });
        return createSliderPanel(label, slider);
    }

    private JPanel createZoomGranularitySlider() {
        JLabel label = createLabel("0.5% zoom", new Color(0xffdddddd), 14);
        JSlider slider = createSlider(0, 50, 5);
        NumberFormat formatter = new DecimalFormat("#0.0");
        slider.addChangeListener(e -> {
            label.setText(formatter.format(slider.getValue() / 1.0 / 1000 * 100) + "% zoom");
            imageFrame.setZoomFactor(slider.getValue() / 1.0 / 1000);
        });
        return createSliderPanel(label, slider);
    }

    private JLabel createLabel(String s, Color color, int fontSize) {
        JLabel label = new JLabel(s);
        label.setFont(new Font("plain", Font.BOLD, fontSize));
        label.setForeground(color);
        return label;
    }

    private JSlider createSlider(int low, int high, int value) {
        JSlider slider = new JSlider(low, high, value);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(false);
        slider.setSnapToTicks(true);
        return slider;
    }

    private JPanel createSliderPanel(JLabel label, JSlider slider) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        panel.add(label, BorderLayout.CENTER);
        panel.add(slider, BorderLayout.SOUTH);
        panel.setOpaque(false);
        panel.setVisible(true);
        return panel;
    }

    private JButton createButton(String s, Color color, int fontSize) {
        JButton button = new JButton(s);
        button.setForeground(color);
        button.setFont(new Font("plain", Font.BOLD, fontSize));
        button.setOpaque(false);
        return button;
    }
}