import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.geom.*;

public class FractalExplorer_1
{
    private static final int WIDTH = 600;
    private static final int HEIGHT = 450;
    
    public static void main( String[] args ) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        } );
    }
    
    private static void createAndShowGUI()
    {
        JFrame frame = new ImageFrame( WIDTH, HEIGHT);             // setup new frame
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );    // exit when the user closes the frame
        frame.setVisible( true );                         		   // make the frame visible
    }
}

class ImageFrame extends JFrame
{
    private static final int WIDTH = 600;
    private static final int HEIGHT = 450;
    private BufferedImage image = null;
    private Graphics2D g2d = null;
    private AreaSelectPanel panel = null;
    private int[] colorArray = null;
    double x0, y0, x1, y1;
    double currWidth, currHeight;
    double topLeftX = 0, topLeftY = 0;
    double[] U = null;
    boolean MandelbrotImage, JuliaImage;
    
    public ImageFrame( int width, int height)
    {
        
        this.setTitle( "Fractal Explorer" );
        this.setSize( width, height );
        setupImage();                                            // setup bufferedImage
        addMenu();                                               // setup and add a menu bar for this frame
    }
    
    private void addMenu() {       // setup menu bar
        
                                                            // setup the File menu
        JMenu fileMenu = new JMenu( "File" );               // create a new menu that will appear as "File" when added to menu bar
        JMenuItem exitItem = new JMenuItem( "Exit" );       // create a new menu item that will appear as "Exit" within a menu
        exitItem.addActionListener( new ActionListener()    // define what happens when this menu item is selected
                                   {
            public void actionPerformed( ActionEvent event )
            {
                System.exit( 0 );  // terminate the program
            }
        } );
        JMenuItem Mandelbrot = new JMenuItem( "Mandelbrot" );
        Mandelbrot.addActionListener( new ActionListener()
                                   {
            public void actionPerformed( ActionEvent event )
            {
                new Thread( new Runnable()
                           {
                    public void run()
                    {
                         Mandelbrot( );
                    }
                } ).start();    // EDT starts the worker thread
            }
        } );
        fileMenu.add( Mandelbrot );
        JMenuItem Julia = new JMenuItem( "Julia" );
        Julia.addActionListener( new ActionListener()
                                   {
            public void actionPerformed( ActionEvent event )
            {
                new Thread( new Runnable()
                           {
                    public void run()
                    {
                        Julia();
                    }
                } ).start();    // EDT starts the worker thread
            }
        } );
        fileMenu.add( Julia );
        JMenuItem save_image = new JMenuItem( "Save image" );
        save_image.addActionListener( new ActionListener()
                                   {
            public void actionPerformed( ActionEvent event )
            {
                saveImage();
            }
        } );
        fileMenu.add( save_image );
        JMenuItem clearImage = new JMenuItem( "Clear image" );
        clearImage.addActionListener( new ActionListener()
                                     {
            public void actionPerformed( ActionEvent event )
            {
                freshImage();
            }
        } );
        JMenuItem animateMandel = new JMenuItem( "Animated zoom" );
        animateMandel.addActionListener( new ActionListener()
                                     {
            public void actionPerformed( ActionEvent event )
            {
                MandelbrotAnimate();
            }
        } );
        fileMenu.add( save_image );
        fileMenu.add( animateMandel );
        fileMenu.add( clearImage );
        fileMenu.add( exitItem );
        JMenuBar menuBar = new JMenuBar();                  // create a menu bar
        menuBar.add( fileMenu );                            // add the "File" menu to the menu bar
        this.setJMenuBar( menuBar );                        // attach the finalized menu bar to the frame
    }
    
    public void displayBufferedImage( BufferedImage image )
    {
        this.setContentPane( new JScrollPane( new JLabel( new ImageIcon( image ))));  // display the image
        this.validate();  // causes the container to lay out its subcomponents that may have been modified
    }
    
    private double[] getMu() {
        
        double a;                             // real part  ->  u = a + bi
        double b;                             // imaginary
        
        String a1 = JOptionPane.showInputDialog("u = a + bi , enter a:");   // Prompt the user for mu
        String b1 = JOptionPane.showInputDialog("u = a + bi , enter b:");

        if(a1 == null || b1 == null){         // if the user selects cancel when prompted for input
            return null;
        }
        try                                   // enclose code that might throw an exception with a try block
        {
            a = Double.parseDouble( a1 );     // try to parse a double from user's input string
            b = Double.parseDouble( b1 );     // try to parse a double from user's input string
        }
        catch( NumberFormatException e )      // catch and handle potential exception of type NumberFormatException
        {
            JOptionPane.showMessageDialog( this, e );
            return null;
        }
        double[] u = {a, b};                  // u = a + bi
        return u;                             // return u to Julia
    }
    
    private void saveImage()
    {
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
        JuliaImage = true;
        MandelbrotImage = false;
        
       // if(U == null) { U = getMu(); }      // get u from user
       // if(U == null) return;
        U = new double[2];
        U[0] = -0.8;
        U[1] = 0.156;
        
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
        double teet = startY;
        
        double delta_X = (endX - startX) / (600 - 1);         // change in x per sample
        double delta_Y = (endY - startY) / (450 - 1);         // change in y per sample
        
        
        for(double i = 0; i < image.getWidth(); i++) {
            startY = teet;
            for(double j = 0; j < image.getHeight(); j++) {                 // for each pixel in the image
                
                z_r =  (double)(startX/(image.getWidth()-1)) * 4 - 2;       // sample the complex plane
                z_i =  1.5 - (double)(startY/(image.getHeight()-1)) * 3;
                t = 0;  // t := 0
                
                while(t != 100) {  // while t != tMax
                    
                    double temp = z_r;                  // z = z^2 + u
                    z_r = ( z_r * z_r - z_i * z_i ) + U[0];
                    z_i = ( temp * z_i + temp * z_i           ) + U[1];
                    
                    if( (z_r * z_r + z_i * z_i) > 4) {  // diverge
                        break;
                    }
                    t++;
                }
                
                if(t < 100) {           // z diverged, not in the set
                    image.setRGB( (int)i, (int)j,  colorArray[t]);
                }
                else if (t % 2 == 0){   // if even, mark for sweet visual effects
                    image.setRGB( (int)i, (int)j, 0xFF000000 );
                }
                else {                  // z might be in the set
                    image.setRGB( (int)i, (int)j,  0xFF000000);
                }
                startY += delta_Y;
            }
            startX += delta_X;
        }
        repaint();
    }

    private void zoomedMandelBrot() {   // zoom in towards the center of the image
        x0 = .01;
        y0 = .01;
        x1 = .99;
        y1 = .99;
        if(MandelbrotImage){
            Mandelbrot();
        }
        else{
            Julia();
        }
    }
    
    private void MandelbrotAnimate() {
        new Thread( new Runnable()
        {
            public void run()                   // worker thread
            {
                for(int i = 0; i < 50; i++) {   // zoom in 50 times
                    zoomedMandelBrot();
                }
            }
        } ).start();    // EDT starts the worker thread
    }
    
    private void Mandelbrot() {
        MandelbrotImage = true;
        JuliaImage  = false;
        
        int t = 0;
        double z0 = 0, z1 = 0;
        double startX = x0 * currWidth + topLeftX;
        double startY = y0 * currHeight + topLeftY;
        double endX = x1 * currWidth + topLeftX;
        double endY = y1 * currHeight + topLeftY;
        
        topLeftX = startX;
        topLeftY = startY;
        currWidth = endX - startX;
        currHeight = endY - startY;
        
        // initial u
        double u_r =  (double)(startX/(image.getWidth()-1)) * 4 - 2;
        double u_i =  1.5 - (double)(startY/(image.getHeight()-1)) * 3;

        double teet = startY;
        
        double delta_X = (endX - startX) / (600 - 1);         // change in x per sample
        double delta_Y = (endY - startY) / (450 - 1);         // change in y per sample
        
        
        for(double i = 0; i < image.getWidth(); i++) {
            startY = teet;
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
                    
                    if( (z0 * z0 + z1 * z1) > 4) {  // diverge
                        break;
                    }
                    t++;
                }
                
                if(t < 100) {         // z diverged, not in set
                    image.setRGB( (int)i, (int)j,  colorArray[t]);
                }
                else if (t % 2 == 0){
                    image.setRGB( (int)i, (int)j, 0xFF000000 );
                }
                else {                // z might be in the set
                    image.setRGB( (int)i, (int)j,  0xFF000000);
                }
                startY += delta_Y;
            }
            startX += delta_X;
        }
        repaint();
    }
    
    private void populateColorArray() {
        
        colorArray =  new int[ 100 ];       // array of 100 color values (BufferedImage.TYPE_INT_ARGB)
        
        int start = (255 << 24) | (4 << 16) | (15 << 8) | 114;        // start color
        int end =   (255 << 24) | (132 << 16) | (248 << 8) | 255;     // end color
        
        int intARGB;                                                  // integer to hold synthesized color values
        int value = start;                                            // start value's channels:
        double value_R = (value >> 16) & 0xFF;
        double value_G = (value >> 8 ) & 0xFF;
        double value_B = (value      ) & 0xFF;
        
        double[] deltas = getDeltas( start, end, colorArray.length / 5 - 1 );  // compute the change per step for each channel
        colorArray[0] = start;
        colorArray[colorArray.length / 5 - 1] = end;
        
        // fill first 1/5 of colors array with interpolated Colors
        for (int i = 1; i < colorArray.length / 5 - 1; i++) {         // synthesize interpolated color values and put in color arr
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            
            intARGB = (0xFF000000) | ((int)value_R << 16) | ((int)value_G << 8) | (int)value_B;
            colorArray[i] = intARGB;
        }
        
        end = (255 << 24) | (255 << 16) | (80 << 8) | 0;              // start color
        start =   (255 << 24) | (255 << 16) | (238 << 8) | 0;         // end color
        
        value = start;                                                // start value's channels:
        value_R = (value >> 16) & 0xFF;
        value_G = (value >> 8 ) & 0xFF;
        value_B = (value      ) & 0xFF;
        
        deltas = getDeltas( start, end, 3*colorArray.length/5 );      // compute the change per step for each channel
        colorArray[colorArray.length / 5 ] = start;
        colorArray[4 * colorArray.length/5 - 1] = end;
        
        // fill 1/5 to 4/5 of the colors array with interpolated Colors
        for (int i = colorArray.length / 5; i < 4 * colorArray.length/5 - 1; i++) {
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            
            intARGB = (0xFF000000) | ((int)value_R << 16) | ((int)value_G << 8) | (int)value_B;
            colorArray[i] = intARGB;
        }
        
        end = (255 << 24) | (37 << 16) | (14 << 8) | 255;           // start color
        start =  (255 << 24) | (109 << 16) | (35 << 8) | 188;       // end color
        
        value = start;
        value_R = (value >> 16) & 0xFF;
        value_G = (value >> 8 ) & 0xFF;
        value_B = (value      ) & 0xFF;
        
        deltas = getDeltas( start, end, 4*colorArray.length/5 -1 );
        colorArray[4 * colorArray.length / 5  ] = start;
        colorArray[colorArray.length - 1] = end;
        
        // fill 4/5 to the end of the colors array with interpolated Colors
        for (int i = 4*colorArray.length / 5; i < colorArray.length - 1; i++) {
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            
            intARGB = (0xFF000000) | ((int)value_R << 16) | ((int)value_G << 8) | (int)value_B;
            colorArray[i] = intARGB;
        }
    }
    
    private double[] getDeltas(int start, int end, int n)
    {
        double start_R, start_G, start_B,		// vars to hold the color channels
        end_R,   end_G,   end_B,
        delta_R, delta_G, delta_B;
        
        end_R = (end >> 16) & 0xFF;             // end value channels
        end_G = (end >> 8 ) & 0xFF;
        end_B = (end      ) & 0xFF;
        
        start_R = (start >> 16) & 0xFF;			// start value channels
        start_G = (start >> 8 ) & 0xFF;
        start_B = (start      ) & 0xFF;
        
        delta_R = (end_R - start_R) / n;		// change per channel
        delta_G = (end_G - start_G) / n;
        delta_B = (end_B - start_B) / n;
        
        double[] deltas = { delta_R, delta_G, delta_B };    // store change per channel into 1D array
        return deltas;                                      // return change per channel to interpolating function
    }
    
    private void setupImage() {
        
        image = new BufferedImage(600, 450, BufferedImage.TYPE_INT_ARGB);    // create square image
        g2d = (Graphics2D) image.createGraphics();
        g2d.setColor( Color.WHITE );
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());             // draw white background
        populateColorArray();                                                // fill the colors array
        
        panel = new AreaSelectPanel( image );
        JButton button = new JButton( "Zoom" );
        button.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent event )
            {
                button.setText( "Zoom" );
                // get the relative corner coordinates
                double Xi = panel.getUpperLeft().getX();
                double Yi = panel.getUpperLeft().getY();
                double Xf = panel.getLowerRight().getX();
                double Yf = panel.getLowerRight().getY();
                
                x0 = Xi;        // store relative corners
                y0 = Yi;
                x1 = Xf;
                y1 = Yf;
                
                if(MandelbrotImage){
                    Mandelbrot();
                }
                else{
                    Julia();
                }
                panel.clearSelection();
                repaint();
            }
        } );
        
        this.getContentPane().add( panel, BorderLayout.CENTER );    // add panel to frame
        this.getContentPane().add( button, BorderLayout.SOUTH );    // add zoom button to frame
        this.pack();
        freshImage();
    }
    
    private void freshImage() {         // clear the image and clear current zoom state
        g2d.setColor( Color.WHITE );
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        x0 = 0;
        y0 = 0;
        x1 = 1;
        y1 = 1;
        topLeftX = 0;
        topLeftY = 0;
        currWidth = image.getWidth();
        currHeight = image.getHeight();
        repaint();
    }
}

class AreaSelectPanel extends JPanel
{
    static private final Color OUTLINE_COLOR = Color.BLACK;
    
    private final int WIDTH, MAX_X;     // panel size
    private final int HEIGHT, MAX_Y;
    
    private BufferedImage image;        // image displayed on panel
    private Graphics2D g2d;
    
    private int x = -1;                 // current selection
    private int y = -1;
    private int w = 0;
    private int h = 0;
    
    // constructor
    public AreaSelectPanel( BufferedImage image )
    {
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
        addMouseListener( new MouseAdapter()
                         {
            public void mousePressed( MouseEvent event )
            {
                clearSelection( event.getPoint() );
            }
        } );
        addMouseMotionListener( new MouseMotionAdapter()
                               {
            public void mouseDragged(MouseEvent event)
            {
                updateSelection( event.getPoint() );
            }
        } );
    }
    
    // accessors - get points defining the area selected
    Point2D.Double getUpperLeft()
    {
        return getUpperLeft( new Point2D.Double() );
    }
    Point2D.Double getUpperLeft( Point2D.Double p )
    {
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
    
    Point2D.Double getLowerRight()
    {
        return getLowerRight( new Point2D.Double() );
    }
    
    Point2D.Double getLowerRight( Point2D.Double p )
    {
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
    
    private void updateSelection( Point p )
    {
        // erase old selection
        drawSelection();
        
        // modify current selection
        int px = (p.x < 0) ? 0 : ( (p.x < WIDTH) ? p.x : MAX_X );
        int py = (p.y < 0) ? 0 : ( (p.y < HEIGHT) ? p.y : MAX_Y );
        
        h = py - y;
        w = px - x;
        // if width too large
        if( (py - y) !=0 &&  Math.abs( (px - x)/(py - y) ) > (6/4.5)) {
            w = Math.abs((int)((double)(py - y) * (600/450)));
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
    public void setImage( BufferedImage src )
    {
        g2d.setPaintMode();
        g2d.drawImage( src,
                      0, 0, MAX_X, MAX_Y,
                      0, 0, (src.getWidth() - 1), (src.getHeight() - 1),
                      OUTLINE_COLOR, null );
        g2d.setXORMode( OUTLINE_COLOR );
        x = -1;
        y = -1;
        w = 0;
        h = 0;
        repaint();
    }

    // behaviors
    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        g.drawImage( image, 0, 0, null );
    }
    
    private void clearSelection( Point p )
    {
        // erase old selection
        drawSelection();
        // begin new selection
        x = (p.x < 0) ? 0 : ( (p.x < WIDTH) ? p.x : MAX_X );
        y = (p.y < 0) ? 0 : ( (p.y < HEIGHT) ? p.y : MAX_Y );
        w = 0;
        h = 0;
        drawSelection();
    }
    
    public void clearSelection()
    {
        // erase old selection
        x = 0;
        y = 0;
        w = 0;
        h = 0;
        drawSelection();
    }
    
    private void drawSelection()
    {
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




