import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
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

class ImageFrame extends JFrame {
    private int WIDTH, HEIGHT;
    private BufferedImage image;
    private Graphics2D g2d;
    private int[] colors;

    boolean Mandelbrot, Julia;
    boolean zoomDirection = true;
    private Timer timer;

    double x0, y0, x1, y1;              // scale factor, make local, functon to get
    double currWidth, currHeight;
    double topLeftX, topLeftY;
    double zoom_destination_x, zoom_destination_y;


    public ImageFrame(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.setTitle("Fractal Explorer");
        this.setSize(width, height);
        setupImage();
        addMenu();
        Mandelbrot();
    }

    private void addMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem Mandelbrot = new JMenuItem("Mandelbrot");
        Mandelbrot.addActionListener( e -> {
            SwingUtilities.invokeLater(() -> { freshImage(); this.Mandelbrot = true; Julia = false; Mandelbrot(); });
        });
        fileMenu.add(Mandelbrot);
        JMenuItem Julia = new JMenuItem("Julia");
        Julia.addActionListener( e -> {
            SwingUtilities.invokeLater(() -> { freshImage(); this.Mandelbrot = false; this.Julia = true; Julia(); });
        });
        fileMenu.add(Julia);
        JMenuItem save_image = new JMenuItem("Save image");
        save_image.addActionListener(e -> {
            saveImage();
        });
        JMenuItem fps = new JMenuItem("Configure frame rate");
        fps.addActionListener(e -> {
            configureFPS(false);
        });

        fileMenu.add(save_image);
        fileMenu.add(fps);

        JMenuBar menuBar = new JMenuBar();                  // create a menu bar
        menuBar.add(fileMenu);                            // add the "File" menu to the menu bar
        this.setJMenuBar(menuBar);                        // attach the finalized menu bar to the frame
    }

    private void configureFPS(boolean isNew) {
        int framesPerSec = 30;
        if(!isNew) {
            String s = JOptionPane.showInputDialog("Frames per second (default: 30 fps):");   // Prompt the user
            try{                                           // enclose code that might throw an exception with a try block
                framesPerSec = Integer.parseInt(s);     // parse int from user's input string
            }
            catch(NumberFormatException e) {             // catch and handle potential exception
                JOptionPane.showMessageDialog(this, e);
            }
        }

        timer = new Timer((1000 / framesPerSec),  e -> {
            timer.stop();
            topLeftX += ((zoom_destination_x / (image.getWidth()  - 1)) - 0.5) * currWidth;
            topLeftY += ((zoom_destination_y / (image.getHeight() - 1)) - 0.5) * currHeight;

            if(zoomDirection) zoomIn();
            else                 zoomOut();

            repaint();
            timer.restart();
        });
    }

    private void saveImage() {
        String inputString = JOptionPane.showInputDialog("ouput file?");
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

    private void Julia() {
        double startX = topLeftX = x0 * currWidth + topLeftX;
        double startY = topLeftY = y0 * currHeight + topLeftY;
        double endX = x1 * currWidth + topLeftX;
        double endY = y1 * currHeight + topLeftY;

        currWidth = endX - startX;
        currHeight = endY - startY;

        double delta_X = (endX - startX) / (WIDTH - 1);         // change in x per sample
        double delta_Y = (endY - startY) / (HEIGHT - 1);         // change in y per sample

        double[] U = new double[]{-0.8, 0.156}; // try u = -0.8 + 0.156i or u = 0.285 +0.01i
        double z_r, z_i;       // sample the complex plane

        int t;
        for(double i = 0; i < image.getWidth(); i++) {
            startY = topLeftY;
            for(double j = 0; j < image.getHeight(); j++) {                 // for each pixel in the image
                z_r =  (startX/(image.getWidth()-1)) * 4 - 2;       // sample the complex plane
                z_i =  1.5 - (startY/(image.getHeight()-1)) * 3;
                t = 0;

                while(t != 100) {  // while t != tMax
                    double temp = z_r;                  // z = z^2 + u
                    z_r = (z_r * z_r - z_i * z_i) + U[0];
                    z_i = (temp * z_i + temp * z_i) + U[1];

                    if((z_r * z_r + z_i * z_i) > 4)   // diverge
                        break;
                    t++;
                }

                if(t < 100)            // z diverged, not in the set
                    image.setRGB((int)i, (int)j,  colors[t]);
                else if (t % 2 == 0)  // if even, mark for visual effects
                    image.setRGB((int)i, (int)j, 0xFF000000);
                else                   // z might be in the set
                    image.setRGB((int)i, (int)j,  0xFF000000);
                startY += delta_Y;
            }
            startX += delta_X;
        }
        repaint();
    }

    private void Mandelbrot() {
        double startX = topLeftX = x0 * currWidth + topLeftX;
        double startY = topLeftY = y0 * currHeight + topLeftY;
        double endX = x1 * currWidth + topLeftX;
        double endY = y1 * currHeight + topLeftY;

        currWidth = endX - startX;
        currHeight = endY - startY;

        double delta_X = (endX - startX) / (WIDTH - 1);         // change in x per sample
        double delta_Y = (endY - startY) / (HEIGHT - 1);         // change in y per sample

        int t;
        double u_r, u_i, z0, z1;

        for(double i = 0; i < image.getWidth(); i++) {
            startY = topLeftY;
            for(double j = 0; j < image.getHeight(); j++) {              // for each pixel in the image
                u_r =        (startX / (image.getWidth()  - 1)) * 4 - 2; // sample the complex plane
                u_i =  1.5 - (startY / (image.getHeight() - 1)) * 3;
                z0 = z1 = 0;
                t = 0;

                while(t != 100) {  // while t != tMax
                    double temp = z0;               // z = z^2 + u
                    z0 = (z0 * z0 - z1 * z1) + u_r;
                    z1 = (temp * z1 + temp * z1) + u_i;

                    if((z0 * z0 + z1 * z1) > 4)   // diverge
                        break;
                    t++;
                }

                if(t < 100)          // z diverged, not in set
                    image.setRGB((int)i, (int)j,  colors[t]);
                else if (t % 2 == 0)
                    image.setRGB((int)i, (int)j, 0xFF000000);
                else               // z might be in the set
                    image.setRGB((int)i, (int)j,  0xFF000000);

                startY += delta_Y;
            }
            startX += delta_X;
        }
        repaint();
    }

    private void populateColorArray() {
        colors =  new int[100];
        int start, end;

        start = (255 << 24) | (4 << 16) | (15 << 8) | 114;        // start color
        end =   (255 << 24) | (132 << 16) | (248 << 8) | 255;     // end color
        int intARGB;                                                  // integer to hold synthesized color values
        int value = start;                                            // start value's channels:
        double value_R = (value >> 16) & 0xFF;
        double value_G = (value >> 8) & 0xFF;
        double value_B = (value     ) & 0xFF;

        double[] deltas = getDeltas(start, end, colors.length / 5 - 1);  // compute the change per step for each channel
        colors[0] = start;
        colors[colors.length / 5 - 1] = end;

        // Fill first 1/5 of colors array with interpolated Colors
        for (int i = 1; i < colors.length / 5 - 1; i++) {
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            intARGB = (0xFF000000) | ((int)value_R << 16) | ((int)value_G << 8) | (int)value_B;
            colors[i] = intARGB;
        }

        end = (255 << 24) | (255 << 16) | (80 << 8) | 0;
        start =   (255 << 24) | (255 << 16) | (238 << 8) | 0;
        value = start;
        value_R = (value >> 16) & 0xFF;
        value_G = (value >> 8) & 0xFF;
        value_B = (value     ) & 0xFF;

        deltas = getDeltas(start, end, 3* colors.length/5);
        colors[colors.length / 5 ] = start;
        colors[4 * colors.length/5 - 1] = end;

        // Fill 1/5 to 4/5 of the colors array with interpolated Colors
        for (int i = colors.length / 5; i < 4 * colors.length/5 - 1; i++) {
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            intARGB = (0xFF000000) | ((int)value_R << 16) | ((int)value_G << 8) | (int)value_B;
            colors[i] = intARGB;
        }

        end = (255 << 24) | (37 << 16) | (14 << 8) | 255;
        start =  (255 << 24) | (109 << 16) | (35 << 8) | 188;
        value = start;
        value_R = (value >> 16) & 0xFF;
        value_G = (value >> 8) & 0xFF;
        value_B = (value     ) & 0xFF;

        deltas = getDeltas(start, end, 4* colors.length/5 -1);
        colors[4 * colors.length / 5  ] = start;
        colors[colors.length - 1] = end;

        // Fill 4/5 to the end of the colors array with interpolated Colors
        for (int i = 4* colors.length / 5; i < colors.length - 1; i++) {
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            intARGB = (0xFF000000) | ((int)value_R << 16) | ((int)value_G << 8) | (int)value_B;
            colors[i] = intARGB;
        }
    }

    private double[] getDeltas(int start, int end, int n) {
        double deltaR = ((end >> 16 & 0xFF) - (start >> 16 & 0xFF)) / 1.0 / n;
        double deltaG = ((end >> 8  & 0xFF) - (start >> 8  & 0xFF)) / 1.0 / n;
        double deltaB = ((end       & 0xFF) - (start       & 0xFF)) / 1.0 / n;
        return new double[]{deltaR, deltaG, deltaB};
    }

    private void setupImage() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g2d = (Graphics2D) image.createGraphics();
        populateColorArray();

        FractalDisplayPanel panel = new FractalDisplayPanel(image);
        JLabel label = new JLabel("Click and hold to zoom (LMB to zoom in/RMB to zoom out)", 0);

        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.getContentPane().add(label, BorderLayout.SOUTH);
        this.pack();

        freshImage();
    }

    private void drawBackground(Color color) {
        g2d.setColor(color);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    private void freshImage() {         // clear image state, set white background
        drawBackground(Color.WHITE);
        x0 = y0 = 0;
        x1 = y1 = 1;
        topLeftX = topLeftY = 0;
        currWidth = image.getWidth();
        currHeight = image.getHeight();
        configureFPS(true);
        repaint();
    }

    private void zoomIn() {
        x0 = y0 = .0125;
        x1 = y1 = .9875;

        if(Mandelbrot) Mandelbrot();
        else                Julia();
    }

    private void zoomOut() {
        x0 = y0 = -.025;
        x1 = y1 = 1.025;

        if(Mandelbrot) Mandelbrot();
        else                Julia();
    }

    class FractalDisplayPanel extends JLayeredPane {

        private BufferedImage image;

        public FractalDisplayPanel(BufferedImage image) {
            this.image = image;
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
                    timer.stop();                       // stop zooming
                }
            });
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
            zoom_destination_x = p.getX();      // update zoom destination
            zoom_destination_y = p.getY();
            zoomDirection = true;            // zooming in
            timer.start();                      // start zooming
        }

        private void RMBisPressed(Point p) {
            zoom_destination_x = p.getX();      // update zoom destination
            zoom_destination_y = p.getY();
            zoomDirection = false;           // zooming out
            timer.start();                      // start zooming
        }
    }
}
