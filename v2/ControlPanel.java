import java.awt.*;
import java.awt.event.*;
import java.text.*;
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
        add(createButton("Mandelbrot", Color.BLACK, 13, e -> imageFrame.freshImage("Mandelbrot")));
        add(createButton("Julia",      Color.BLACK, 13, e -> imageFrame.freshImage("Julia")));
        add(createButton("Save Image", Color.BLACK, 13, e -> imageFrame.saveImage()));
        add(animationSpeedSlider());
        add(createZoomGranularitySlider());
    }

    private JButton createButton(String s, Color color, int fontSize, ActionListener e) {
        JButton button = new JButton(s);
        button.addActionListener(e);
        button.setForeground(color);
        button.setFont(new Font("plain", Font.BOLD, fontSize));
        button.setOpaque(false);
        return button;
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

    private JPanel animationSpeedSlider() {
        JLabel label = createLabel(30 + " fps", new Color(0xffdddddd), 14);
        JSlider slider = createSlider(1, 100, 30);
        slider.addChangeListener(e -> {
            label.setText(slider.getValue() + " fps");
            imageFrame.configureFPS(slider.getValue());
        });
        return createSliderPanel(label, slider);
    }

    private JPanel createZoomGranularitySlider() {
        JLabel label = createLabel("2.50% zoom", new Color(0xffdddddd), 14);
        JSlider slider = createSlider(0, 50, 20);
        NumberFormat formatter = new DecimalFormat("#0.00");
        slider.addChangeListener(e -> {
            label.setText(formatter.format(slider.getValue() / 1.0 / 1000 * 100) + "% zoom");
            imageFrame.setZoomFactor(slider.getValue() / 1.0 / 1000);
        });
        return createSliderPanel(label, slider);
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

}