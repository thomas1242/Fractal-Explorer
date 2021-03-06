import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class ImageFrame extends JFrame {

    private int WIDTH, HEIGHT;
    private BufferedImage image;
    private Graphics2D g2d;
    private int[] colors;

    private String currSet = "Mandelbrot";
    private boolean zoomIn = true;
    private Timer timer;

    private double currWidth, currHeight, topLeftX, topLeftY;
    private double zoom_destination_x, zoom_destination_y;
    private double scaleFactor = 0.02;

    public ImageFrame(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.setTitle("Fractal Explorer");
        this.setSize(width, height);
        setupImage();
        configureFPS(30);
    }

    private void drawBackground(Color color) {
        g2d.setColor(color);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    public void saveImage() {
        String inputString = JOptionPane.showInputDialog("output file?");
        if(inputString == null || inputString.length() == 0)
            return;
        try {
            File outputFile = new File(inputString);
            ImageIO.write(image, "png", outputFile);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(ImageFrame.this, "Error saving file", "oops!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void configureFPS(int fps) {
        timer = new Timer(1000 / fps, e -> {
            timer.stop();
            topLeftX += (zoom_destination_x / (image.getWidth()  - 1) - 0.5) * currWidth;
            topLeftY += (zoom_destination_y / (image.getHeight() - 1) - 0.5) * currHeight;
            SwingUtilities.invokeLater(() -> {
                updateImage();
                repaint();
            });
            timer.restart();
        });
    }

    public void updateImage() {
        double x0, y0, x1, y1;
        x0 = y0 = zoomIn ? scaleFactor: -scaleFactor;
        x1 = y1 = zoomIn ? 1 - scaleFactor : 1 + scaleFactor;

        topLeftX = x0 * currWidth + topLeftX;
        topLeftY = y0 * currHeight + topLeftY;
        currWidth =  x1 * currWidth + topLeftX - topLeftX;
        currHeight = y1 * currHeight + topLeftY - topLeftY;

        double delta_X = currWidth / (WIDTH - 1);         // change in x per sample
        double delta_Y = currHeight / (HEIGHT - 1);       // change in y per sample
        double startX = topLeftX;

        for(double i = 0; i < image.getWidth(); i++) {
            double startY = topLeftY;
            for(double j = 0; j < image.getHeight(); j++) {       
               
                double u_r = !currSet.equals("Mandelbrot") ? -0.8   : (startX / (image.getWidth()  - 1)) * 4 - 2;
                double u_i = !currSet.equals("Mandelbrot") ?  0.156 : 1.5 - (startY / (image.getHeight() - 1)) * 3;
                double z_r =  currSet.equals("Mandelbrot") ?  0     : (startX / (image.getWidth()-1)) * 4 - 2;
                double z_i =  currSet.equals("Mandelbrot") ?  0     : 1.5 - (startY / (image.getHeight()-1)) * 3;

                int t = 0;
                while(t++ != 100) {                         // while t != tMax
                    double temp = z_r;                      
                    z_r = (z_r * z_r - z_i * z_i) + u_r;
                    z_i = (temp * z_i + temp * z_i) + u_i;  
                    if(z_r * z_r + z_i * z_i > 4)           // z^2 + u
                        break;                              // diverge
                }

                if(t < 100)            image.setRGB((int)i, (int)j,  colors[t]);  // z diverged, not in the set
                else if (t % 2 == 0)   image.setRGB((int)i, (int)j, 0xFF000000);  // if even, mark for visual effects
                else                   image.setRGB((int)i, (int)j, 0xFF000000);  // z might be in the set
                
                startY += delta_Y;
            }
            startX += delta_X;
        }
    }

    private void initColorArray() {
        colors =  new int[100];

        int[] startColors = new int[]{ 255 << 24 |   4 << 16 |  15 << 8 | 114, 255 << 24 | 255 << 16 | 238 << 8 | 0, 255 << 24 | 109 << 16 | 35 << 8 | 188 };
        int[] endColors   = new int[]{ 255 << 24 | 132 << 16 | 248 << 8 | 255, 255 << 24 | 255 << 16 |  80 << 8 | 0, 255 << 24 |  37 << 16 | 14 << 8 | 255 };

        int[] colorOne = getColors(startColors[0], endColors[0],  colors.length / 5);     // Fill first 1/5 with interpolated colors
        System.arraycopy(colorOne, 0, colors, 0, colors.length / 5);

        int[] colorTwo = getColors(startColors[1], endColors[1],  3 * colors.length / 5); // Fill 1/5 to 4/5
        System.arraycopy(colorTwo, 0, colors, colors.length / 5, 4 * colors.length / 5 - colors.length / 5);

        int[] colorThree = getColors(startColors[2], endColors[2], colors.length / 5);    // Fill 4/5 to the end
        System.arraycopy(colorThree, 0, colors, 4 * colors.length / 5, colors.length - 4 * colors.length / 5);
    }

    public static int[] getColors(int startRGB, int endRGB, int length) {
        int[] colors = new int[length];

        colors[0]          = startRGB;
        colors[length - 1] = endRGB;

        double R = startRGB >> 16 & 0xFF;
        double G = startRGB >> 8  & 0xFF;
        double B = startRGB       & 0xFF;

        double deltaR = ((endRGB >> 16 & 0xFF) - (startRGB >> 16 & 0xFF)) / 1.0 / length;
        double deltaG = ((endRGB >> 8  & 0xFF) - (startRGB >> 8  & 0xFF)) / 1.0 / length;
        double deltaB = ((endRGB       & 0xFF) - (startRGB       & 0xFF)) / 1.0 / length;

        for (int i = 1; i < length - 1; i++) {   // fill 1D array with interpolated colors
            R += deltaR;
            G += deltaG;
            B += deltaB;

            int intARGB = 0xFF << 24 | (int)R << 16 | (int)G << 8 | (int)B;
            colors[i] = intARGB ;
        }

        return colors;
    }

    private void setupImage() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g2d = (Graphics2D) image.createGraphics();
        initColorArray();

        FractalDisplayPanel panel = new FractalDisplayPanel(this);
        JLabel label = new JLabel("Click and hold to zoom (LMB to zoom in/RMB to zoom out)", 0);
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.getContentPane().add(label, BorderLayout.SOUTH);
        this.pack();

        freshImage("Mandelbrot");
    }

    public void freshImage(String s) {         // clear image state, set white background
        drawBackground(Color.WHITE);
        topLeftX = topLeftY = 0;
        currWidth = image.getWidth();
        currHeight = image.getHeight();
        currSet = s;
        updateImage();
        repaint();
    }

    public  void setZoomFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    private BufferedImage getImage() {
        return image;
    }

    class FractalDisplayPanel extends JLayeredPane {

        private BufferedImage image;

        public FractalDisplayPanel(ImageFrame imageFrame) {
            this.image = imageFrame.getImage();
            g2d = image.createGraphics();
            setBounds(0, 0, image.getWidth(), image.getHeight());
            Dimension size = new Dimension(image.getWidth(), image.getHeight());
            setMinimumSize(size); setMaximumSize(size); setPreferredSize(size);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent event) {
                    if(event.getButton() == MouseEvent.BUTTON1)
                        LMBisPressed(event.getPoint());
                    else if(event.getButton() == MouseEvent.BUTTON2)
                        RMBisPressed(event.getPoint());
                    else if(event.getButton() == MouseEvent.BUTTON3)
                        RMBisPressed(event.getPoint());
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent event) {
                    updateSelection(event.getPoint());
                }
            });
            addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent event) {
                    timer.stop();
                }
            });

            ControlPanel controlPanel = new ControlPanel(imageFrame);
            this.add(controlPanel);

            int x1 = (int)(image.getWidth() - image.getWidth() * 0.15);
            int x2 = (int)(image.getWidth() * 0.15);
            int y1 = (int)(image.getHeight() * 0.333);
            int y2 = (int)(image.getHeight() * 0.333);
            controlPanel.setBounds(x1, y1, x2, y2);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }

        private void updateSelection(Point p) {
            zoom_destination_x = p.getX();     // update zoom destination
            zoom_destination_y = p.getY();
        }

        private void LMBisPressed(Point p) {
            updateSelection(p);
            zoomIn = true;
            timer.start();
        }

        private void RMBisPressed(Point p) {
            updateSelection(p);
            zoomIn = false;
            timer.start();
        }
    }
}