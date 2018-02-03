import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.imageio.*;
import java.io.*;

public class Main {
    private static final int WIDTH  = 900;
    private static final int HEIGHT = 675;

    public static void main( String[] args ) {
        SwingUtilities.invokeLater( () -> createAndShowGUI() );
    }

    private static void createAndShowGUI() {
        JFrame frame = new ImageFrame( WIDTH, HEIGHT);             // setup new frame
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );    // exit when the user closes the frame
        frame.setVisible( true );                                  // make the frame visible
    }
}

class ImageFrame extends JFrame {
    private static final int WIDTH  = 900;
    private static final int HEIGHT = 675;
    private BufferedImage image = null;
    private Graphics2D g2d = null;
    private AreaSelectPanel panel = null;
    private int[] colors = null;
    private double x0, y0, x1, y1;
    private double currWidth, currHeight;
    private double topLeftX = 0, topLeftY = 0;
    private boolean MandelbrotImage;

    public ImageFrame( int width, int height) {
        this.setTitle( "Fractal Explorer" );
        this.setSize( width, height );
        setupImage();                                  
        addMenu();                                            
        Mandelbrot();
    }

    private void addMenu() {
        JMenu fileMenu = new JMenu( "File" );           
       
        JMenuItem Mandelbrot = new JMenuItem( "Mandelbrot" );
        Mandelbrot.addActionListener( e -> { freshImage(); Mandelbrot(); });
        JMenuItem Julia = new JMenuItem( "Julia" );
        Julia.addActionListener(e -> { freshImage(); Julia(); });
        JMenuItem save_image = new JMenuItem( "Save image" );
        save_image.addActionListener( e ->  saveImage() );
        JMenuItem animateMandel = new JMenuItem( "Animated zoom" );
        animateMandel.addActionListener( e ->  MandelbrotAnimate() );

        fileMenu.add( Mandelbrot );
        fileMenu.add( Julia );
        fileMenu.add( save_image );
        fileMenu.add( animateMandel );

        JMenuBar menuBar = new JMenuBar();                  // create a menu bar
        menuBar.add( fileMenu );                            // add the "File" menu to the menu bar
        this.setJMenuBar( menuBar );                        // attach the finalized menu bar to the frame
    }

    private void saveImage() {
        String inputString = JOptionPane.showInputDialog("ouput file?"); // prompt user for output file
        try {
            File outputFile = new File( inputString );
            ImageIO.write( image, "png", outputFile );
        }
        catch ( IOException e ) {
            JOptionPane.showMessageDialog( ImageFrame.this,
                    "Error saving file",
                    "oops!",
                    JOptionPane.ERROR_MESSAGE );
        }
    }

    private void Julia() {
        MandelbrotImage = false;

        double[] U = new double[]{-0.8, 0.156};
        int t = 0;

        double startX = x0 * currWidth + topLeftX;
        double startY = y0 * currHeight + topLeftY;
        double endX = x1 * currWidth + topLeftX;
        double endY = y1 * currHeight + topLeftY;

        topLeftX = startX;
        topLeftY = startY;

        currWidth = endX - startX;
        currHeight = endY - startY;

        // initial u
        double z_r =  (double)(startX/(image.getWidth()-1)) * 4 - 2;        // sample the complex plane
        double z_i =  1.5 - (double)(startY/(image.getHeight()-1)) * 3;
        double sY = startY;

        double delta_X = (endX - startX) / (WIDTH - 1);         // change in x per sample
        double delta_Y = (endY - startY) / (HEIGHT - 1);         // change in y per sample

        for(double i = 0; i < image.getWidth(); i++) {
            startY = sY;
            for(double j = 0; j < image.getHeight(); j++) {                 // for each pixel in the image
                z_r =  (double)(startX/(image.getWidth()-1)) * 4 - 2;       // sample the complex plane
                z_i =  1.5 - (double)(startY/(image.getHeight()-1)) * 3;
                t = 0;  // t := 0

                while(t != 100) {  // while t != tMax
                    double temp = z_r;                  // z = z^2 + u
                    z_r = ( z_r * z_r - z_i * z_i ) + U[0];
                    z_i = ( temp * z_i + temp * z_i           ) + U[1];

                    if( (z_r * z_r + z_i * z_i) > 4)  // diverge
                        break;
                    t++;
                }

                if(t < 100)            // z diverged, not in the set
                    image.setRGB( (int)i, (int)j,  colors[t]);
                else if (t % 2 == 0)    // if even, mark for visual effects
                    image.setRGB( (int)i, (int)j, 0xFF000000 );
                else                   // z might be in the set
                    image.setRGB( (int)i, (int)j,  0xFF000000);
                startY += delta_Y;
            }
            startX += delta_X;
        }
        repaint();
    }

    private void zoomedMandelBrot() {   // zoom in towards the center of the image
        x0 = y0 = .01;
        x1 = y1 = .99;

        if(MandelbrotImage)
            Mandelbrot();
        else
            Julia();
    }

    private void MandelbrotAnimate() {
        new Thread( () -> {
                for(int i = 0; i < 50; i++)   // zoom in 50 times
                    zoomedMandelBrot(); 
            }
        ).start();    // EDT starts the worker thread
    }

    private void Mandelbrot() {
        MandelbrotImage = true;

        double z0 = 0, z1 = 0;
        double startX = x0 * currWidth + topLeftX;
        double startY = y0 * currHeight + topLeftY;
        double endX = x1 * currWidth + topLeftX;
        double endY = y1 * currHeight + topLeftY;

        topLeftX = startX;
        topLeftY = startY;
        currWidth = endX - startX;
        currHeight = endY - startY;

        double u_r =  (double)(startX/(image.getWidth()-1)) * 4 - 2;
        double u_i =  1.5 - (double)(startY/(image.getHeight()-1)) * 3;
        double sY = startY;

        double delta_X = (endX - startX) / (WIDTH - 1);         // change in x per sample
        double delta_Y = (endY - startY) / (HEIGHT - 1);         // change in y per sample
        int t = 0;

        for(double i = 0; i < image.getWidth(); i++) {
            startY = sY;
            for(double j = 0; j < image.getHeight(); j++) {    // for each pixel in the image
                u_r =        (double)( startX / ( image.getWidth()  - 1 )) * 4 - 2; // sample the complex plane
                u_i =  1.5 - (double)( startY / ( image.getHeight() - 1 )) * 3;
                z0 = 0; // z := 0 + 0i
                z1 = 0;
                t = 0;  // t := 0

                while(t != 100) {  // while t != tMax
                    double temp = z0;               // z = z^2 + u
                    z0 = ( z0 * z0 - z1 * z1 ) + u_r;
                    z1 = ( temp * z1 + temp * z1 ) + u_i;

                    if( (z0 * z0 + z1 * z1) > 4)  // diverge
                        break;
                    t++;
                }

                if(t < 100)         // z diverged, not in set
                    image.setRGB( (int)i, (int)j,  colors[t]);
                else if (t % 2 == 0)
                    image.setRGB( (int)i, (int)j, 0xFF000000 );
                else                // z might be in the set
                    image.setRGB( (int)i, (int)j,  0xFF000000);
                startY += delta_Y;
            }
            startX += delta_X;
        }
        repaint();
    }

    private void populateColorArray() {
        colors =  new int[100];

        // Fill first 1/5 with interpolated Colors
        int start = (255 << 24) | (4 << 16) | (15 << 8) | 114;        // start color
        int end =   (255 << 24) | (132 << 16) | (248 << 8) | 255;
        int[] colorOne = getColors(start, end,  colors.length / 5);
        for (int i = 0; i < colors.length / 5; i++)
            colors[i] = colorOne[i];

        end = (255 << 24) | (255 << 16) | (80 << 8) | 0;
        start =   (255 << 24) | (255 << 16) | (238 << 8) | 0;

        // Fill 1/5 to 4/5
        int[] colorTwo = getColors(start, end,  3 * colors.length / 5);
        for (int i = colors.length / 5; i < 4 * colors.length / 5; i++)
            colors[i] = colorTwo[i - colors.length / 5];

        end = (255 << 24) | (37 << 16) | (14 << 8) | 255;
        start =  (255 << 24) | (109 << 16) | (35 << 8) | 188;

        // Fill 4/5 to the end of the colors array
        int[] colorThree = getColors(start, end, colors.length / 5);
        for (int i = 4 * colors.length / 5; i < colors.length ; i++)
            colors[i] = colorThree[i - 4 * colors.length / 5];
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
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);    // create square image
        g2d = (Graphics2D) image.createGraphics();
        g2d.setColor( Color.WHITE );
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());             // draw white background
        populateColorArray();                                                // fill the colors array

        panel = new AreaSelectPanel( image );
        JButton button = new JButton( "Zoom" );
        button.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent event ) {
                button.setText( "Zoom" );
                // get the relative corner coordinates
                x0 = panel.getUpperLeft().getX();
                y0 = panel.getUpperLeft().getY();
                x1 = panel.getLowerRight().getX();
                y1 = panel.getLowerRight().getY();

                if(MandelbrotImage)
                    Mandelbrot();
                else
                    Julia();
                panel.clearSelection();
                repaint();
            }
        });

        this.getContentPane().add( panel, BorderLayout.CENTER );    // add panel to frame
        this.getContentPane().add( button, BorderLayout.SOUTH );    // add zoom button to frame
        this.pack();
        freshImage();
    }

    private void freshImage() {         // clear the image and clear current zoom state
        g2d.setColor( Color.WHITE );
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        x0 = y0 = 0;
        x1 = y1 = 1;
        topLeftX = topLeftY = 0;
        currWidth = image.getWidth();
        currHeight = image.getHeight();
        repaint();
    }
}

class AreaSelectPanel extends JPanel {
    static private final Color OUTLINE_COLOR = Color.BLACK;

    private final int WIDTH, MAX_X;     // panel size
    private final int HEIGHT, MAX_Y;
    private BufferedImage image;        // image displayed on panel
    private Graphics2D g2d;

    private int x = -1;                 // current selection
    private int y = -1;
    private int w = 0;
    private int h = 0;

    public AreaSelectPanel( BufferedImage image ) {
        this.image = image;
        g2d = image.createGraphics();
        g2d.setXORMode( OUTLINE_COLOR );

        WIDTH = image.getWidth();       // define panel characteristics
        HEIGHT = image.getHeight();
        Dimension size = new Dimension( WIDTH, HEIGHT );
        setMinimumSize( size );
        setMaximumSize( size );
        setPreferredSize( size );
        MAX_X = WIDTH - 1;
        MAX_Y = HEIGHT - 1;
        addMouseListener( new MouseAdapter() {
            public void mousePressed( MouseEvent event ) {
                clearSelection( event.getPoint() );
            }
        });
        addMouseMotionListener( new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event) {
                updateSelection( event.getPoint() );
            }
        });
    }

    // accessors - get points defining the area selected
    Point2D.Double getUpperLeft() {
        return getUpperLeft( new Point2D.Double() );
    }
    Point2D.Double getUpperLeft( Point2D.Double p ) {
        if ( w < 0 )
            if ( h < 0 )
                p.setLocation( (x+w)/((double) MAX_X), (y+h)/((double) MAX_Y) );
            else
                p.setLocation( (x+w)/((double) MAX_X), y/((double) MAX_Y) );
        else if ( h < 0 )
            p.setLocation( x/((double) MAX_X), (y+h)/((double) MAX_Y) );
        else
            p.setLocation( x/((double) MAX_X), y/((double) MAX_Y) );

        return p;
    }

    Point2D.Double getLowerRight() {
        return getLowerRight( new Point2D.Double() );
    }

    Point2D.Double getLowerRight( Point2D.Double p ) {
        if ( w < 0 )
            if ( h < 0 )
                p.setLocation( x/((double) MAX_X), y/((double) MAX_Y) );
            else
                p.setLocation( x/((double) MAX_X), (y+h)/((double) MAX_Y) );
        else if ( h < 0 )
            p.setLocation( (x+w)/((double) MAX_X), y/((double) MAX_Y) );
        else
            p.setLocation( (x+w)/((double) MAX_X), (y+h)/((double) MAX_Y) );

        return p;
    }

    private void updateSelection( Point p ) {
        // erase old selection
        drawSelection();

        // modify current selection
        int px = (p.x < 0) ? 0 : ( (p.x < WIDTH) ? p.x : MAX_X );
        int py = (p.y < 0) ? 0 : ( (p.y < HEIGHT) ? p.y : MAX_Y );

        h = py - y;
        w = px - x;
        // if width too large
        if( (py - y) !=0 &&  Math.abs( (px - x)/(py - y) ) > (6/4.5)) {
            w = Math.abs((int)((double)(py - y) * (WIDTH/HEIGHT)));
            h = py - y;
            if((px - x)<0) {
                w *= -1;
            }
        }               // if height too large
        else if( (px - x) !=0 && Math.abs( (py - y)/(px - x) ) > (4.5/6)) {
            h = Math.abs((int)((double)(px - x) * (4.5/6)));
            w = px - x;
            if((py - y)<0) {
                h *= -1;
            }
        }
        drawSelection();
    }

    // change background image
    public void setImage( BufferedImage src ) {
        g2d.setPaintMode();
        g2d.drawImage( src,
                0, 0, MAX_X, MAX_Y,
                0, 0, (src.getWidth() - 1), (src.getHeight() - 1),
                OUTLINE_COLOR, null );
        g2d.setXORMode( OUTLINE_COLOR );
        x = y = -1;
        w = h = 0;
        repaint();
    }

    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        g.drawImage( image, 0, 0, null );
    }

    private void clearSelection( Point p ) {
        drawSelection();                                  // erase old selection
        x = p.x < 0 ? 0 : (p.x < WIDTH  ? p.x : MAX_X);   // begin new selection
        y = p.y < 0 ? 0 : (p.y < HEIGHT ? p.y : MAX_Y);
        w = h = 0;
        drawSelection();
    }

    public void clearSelection() {
        x = y = 0;
        w = h = 0;
        drawSelection();
    }

    private void drawSelection() {
        if ( w < 0 )
            if ( h < 0 )
                g2d.drawRect( (x+w), (y+h), -w, -h );
            else
                g2d.drawRect( (x+w), y, -w, h );
        else if ( h < 0 )
            g2d.drawRect( x, (y+h), w, -h );
        else
            g2d.drawRect( x, y, w, h );
        repaint();
    }
}
