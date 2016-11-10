import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.geom.*;
import javax.swing.SwingUtilities;

public class FractalExplorer_v2
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
    private FractalDisplayPanel panel = null;
    private int[] colorArray = null;
    double x0, y0, x1, y1;
    double currWidth, currHeight;
    double topLeftX = 0, topLeftY = 0;
    double[] U = null;
    boolean MandelbrotImage, JuliaImage;
    boolean currentDirection = true;
    double zoom_destination_x = 0, zoom_destination_y = 0;
    private Timer timer;
    boolean colorsConfigured = false;
    int[] colors = null;
    int framesPerSec;

    public ImageFrame( int width, int height)
    {
        this.setTitle( "Fractal Explorer" );
        this.setSize( width, height );
        setupImage();                                            // setup bufferedImage
        addMenu();                                               // setup and add a menu bar for this frame
    }
    
    private void addMenu() {
                                                            // setup the File menu
        JMenu fileMenu = new JMenu( "File" );               // create a new menu that will appear as "File" when added to menu bar
        JMenuItem exitItem = new JMenuItem( "Exit" );       // create a new menu item that will appear as "Exit" within a menu
        exitItem.addActionListener( new ActionListener()    // define what happens when this menu item is selected
        {
            public void actionPerformed( ActionEvent event )
            {
                System.exit( 0 );
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
                         Mandelbrot( "default" );
                    }
                } ).start();
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
                        Julia( "default" );
                    }
                } ).start();
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
        JMenuItem conf = new JMenuItem( "Configure colors" );
        conf.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent event )
            {
                configureColors();
            }
        } );
        JMenuItem fps = new JMenuItem( "Configure frame rate" );
        fps.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent event )
            {
                configureFPS(false);
            }
        } );
        
        fileMenu.add( save_image );
        fileMenu.add( exitItem );
        fileMenu.add( conf );
        fileMenu.add( fps );

        JMenuBar menuBar = new JMenuBar();                  // create a menu bar
        menuBar.add( fileMenu );                            // add the "File" menu to the menu bar
        this.setJMenuBar( menuBar );                        // attach the finalized menu bar to the frame
    }
    
    public void displayBufferedImage( BufferedImage image )
    {
        this.setContentPane( new JScrollPane( new JLabel( new ImageIcon( image ))));  // display the image
        this.validate();  // causes the container to lay out its subcomponents that may have been modified
    }
    
    private void configureFPS(boolean isNew) {
        
        if( !isNew ) {
            String s = JOptionPane.showInputDialog("Frames per second (default: 15 fps):");   // Prompt the user
            if(s == null)
                return;
            try                                           // enclose code that might throw an exception with a try block
            {
                framesPerSec = Integer.parseInt( s );     // parse int from user's input string
            }
            catch( NumberFormatException e )              // catch and handle potential exception
            {
                JOptionPane.showMessageDialog( this, e );
                return;
            }
        }
        
        timer = new Timer((1000 / framesPerSec), new ActionListener()   // create a new timer
        {
            public void actionPerformed( ActionEvent e)
            {
                timer.stop();
                topLeftX += ( (zoom_destination_x / (image.getWidth()  - 1) ) - 0.5) * currWidth;
                topLeftY += ( (zoom_destination_y / (image.getHeight() - 1) ) - 0.5) * currHeight;
                
                if( currentDirection )
                    zoomIn();
                else
                    zoomOut();
                
                repaint();
                timer.restart();
            }
        } );
    }
    
    private double[] getMu() {
        
        double a, b;
        String a1 = JOptionPane.showInputDialog("u = a + bi, enter a:\ntry u = -0.8 + 0.156i or u = 0.285 +0.01i");
        String b1 = JOptionPane.showInputDialog("u = a + bi, enter b:\ntry u = -0.8 + 0.156i or u = 0.285 +0.01i");
        
        if(a1 == null || b1 == null){
            return null;
        }
        try
        {
            a = Double.parseDouble( a1 );
            b = Double.parseDouble( b1 );
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog( this, e );
            return null;
        }
        double[] u = {a, b};                  // u = a + bi
        return u;                             // return u to Julia
    }
    
    private void saveImage()
    {
        String inputString = JOptionPane.showInputDialog("ouput file?");
        if(inputString == null || inputString.length() == 0) {
            return;
        }
        try
        {
            File outputFile = new File( inputString );
            ImageIO.write( image, "png", outputFile );
        }
        catch ( IOException e )
        {
            JOptionPane.showMessageDialog( ImageFrame.this,
                                          "Error saving file",
                                          "oops!",
                                          JOptionPane.ERROR_MESSAGE );
        }
    }
    
    private void Julia(String s) {
        
        if(s == "default") {
            freshImage();
        }
        
        JuliaImage = true;
        MandelbrotImage = false;
        
       if(U == null) U = getMu();
       if(U == null) return;
        
        int t = 0;
        double startX = x0 * currWidth + topLeftX;
        double startY = y0 * currHeight + topLeftY;
        double endX = x1 * currWidth + topLeftX;
        double endY = y1 * currHeight + topLeftY;
        
        topLeftX = startX;
        topLeftY = startY;

        currWidth = endX - startX;
        currHeight = endY - startY;
        
        double z_r =  (double)(startX/(image.getWidth()-1)) * 4 - 2;        // sample the complex plane
        double z_i =  1.5 - (double)(startY/(image.getHeight()-1)) * 3;
        double sY = startY;
        
        double delta_X = (endX - startX) / (600 - 1);         // change in x per sample
        double delta_Y = (endY - startY) / (450 - 1);         // change in y per sample
        
        
        for(double i = 0; i < image.getWidth(); i++) {
            startY = sY;
            for(double j = 0; j < image.getHeight(); j++) {                 // for each pixel in the image
                
                z_r =  (double)(startX/(image.getWidth()-1)) * 4 - 2;       // sample the complex plane
                z_i =  1.5 - (double)(startY/(image.getHeight()-1)) * 3;
                t = 0;
                
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
                else if (t % 2 == 0){   // if even, mark for visual effects
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
    
    private void Mandelbrot(String s) {
        
        if(s == "default") {
            freshImage();
        }
        
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
        
        double u_r =  (double)(startX/(image.getWidth()-1)) * 4 - 2;
        double u_i =  1.5 - (double)(startY/(image.getHeight()-1)) * 3;

        double sY = startY;
        
        double delta_X = (endX - startX) / (600 - 1);         // change in x per sample
        double delta_Y = (endY - startY) / (450 - 1);         // change in y per sample
        
        
        for(double i = 0; i < image.getWidth(); i++) {
            startY = sY;
            for(double j = 0; j < image.getHeight(); j++) {              // for each pixel in the image
                
                u_r =        (double)( startX / ( image.getWidth()  - 1 )) * 4 - 2; // sample the complex plane
                u_i =  1.5 - (double)( startY / ( image.getHeight() - 1 )) * 3;
                z0 = 0;
                z1 = 0;
                t  = 0;
                
                while(t != 100) {  // while t != tMax
                    
                    double temp = z0;               // z = z^2 + u
                    z0 = ( z0 * z0 - z1 * z1 ) + u_r;
                    z1 = ( temp * z1 + temp * z1 ) + u_i;
                    
                    if( (z0 * z0 + z1 * z1) > 4) {  // diverge
                        break;
                    }
                    t++;
                }
                
                if(t < 100) {          // z diverged, not in set
                    image.setRGB( (int)i, (int)j,  colorArray[t]);
                }
                else if (t % 2 == 0){
                    image.setRGB( (int)i, (int)j, 0xFF000000 );
                }
                else {                 // z might be in the set
                    image.setRGB( (int)i, (int)j,  0xFF000000);
                }
                startY += delta_Y;
            }
            startX += delta_X;
        }
        repaint();
    }
    
    private void populateColorArray() {
        
        colorArray =  new int[ 100 ];       // array of 100 color values, TYPE_INT_ARGB
        int start, end;
        
        if( colorsConfigured ) {
            start = colors[0];
            end = colors[1];
        }
        else {
            start = (255 << 24) | (4 << 16) | (15 << 8) | 114;        // start color
            end =   (255 << 24) | (132 << 16) | (248 << 8) | 255;     // end color
        }
        
        int intARGB;                                                  // integer to hold synthesized color values
        int value = start;                                            // start value's channels:
        double value_R = (value >> 16) & 0xFF;
        double value_G = (value >> 8 ) & 0xFF;
        double value_B = (value      ) & 0xFF;
        
        double[] deltas = getDeltas( start, end, colorArray.length / 5 - 1 );  // compute the change per step for each channel
        colorArray[0] = start;
        colorArray[colorArray.length / 5 - 1] = end;
        
        // Fill first 1/5 of colors array with interpolated Colors
        for (int i = 1; i < colorArray.length / 5 - 1; i++) {
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            // synthesize interpolated color values and put in color arr
            intARGB = (0xFF000000) | ((int)value_R << 16) | ((int)value_G << 8) | (int)value_B;
            colorArray[i] = intARGB;
        }
        
        if( colorsConfigured ) {
            start = colors[0];
            end = colors[1];
        }
        else {
            end = (255 << 24) | (255 << 16) | (80 << 8) | 0;
            start =   (255 << 24) | (255 << 16) | (238 << 8) | 0;
        }
        
        value = start;
        value_R = (value >> 16) & 0xFF;
        value_G = (value >> 8 ) & 0xFF;
        value_B = (value      ) & 0xFF;
        
        deltas = getDeltas( start, end, 3*colorArray.length/5 );
        colorArray[colorArray.length / 5 ] = start;
        colorArray[4 * colorArray.length/5 - 1] = end;
        
        // Fill 1/5 to 4/5 of the colors array with interpolated Colors
        for (int i = colorArray.length / 5; i < 4 * colorArray.length/5 - 1; i++) {
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            // synthesize interpolated color values and put in color arr
            intARGB = (0xFF000000) | ((int)value_R << 16) | ((int)value_G << 8) | (int)value_B;
            colorArray[i] = intARGB;
        }
        
        if( colorsConfigured ) {
            start = colors[0];
            end = colors[1];
        }
        else {
            end = (255 << 24) | (37 << 16) | (14 << 8) | 255;
            start =  (255 << 24) | (109 << 16) | (35 << 8) | 188;
        }
        
        value = start;
        value_R = (value >> 16) & 0xFF;
        value_G = (value >> 8 ) & 0xFF;
        value_B = (value      ) & 0xFF;

        deltas = getDeltas( start, end, 4*colorArray.length/5 -1 );
        colorArray[4 * colorArray.length / 5  ] = start;
        colorArray[colorArray.length - 1] = end;
        
        // Fill 4/5 to the end of the colors array with interpolated Colors
        for (int i = 4*colorArray.length / 5; i < colorArray.length - 1; i++) {
            value_R += deltas[0];
            value_G += deltas[1];
            value_B += deltas[2];
            // synthesize interpolated color values and put in color arr
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
        
        double[] deltas = { delta_R, delta_G, delta_B };
        return deltas;                                      // return change per channel to interpolating function
    }
    
    private int stringToInt(String s) { // convert string to integer, accepts hexadecimal numbers too
        int n;
        try{                            // try to parse integer value from the string
            if( s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X') ) {
                n = (int) Long.parseLong( s.substring( 2, s.length() ), 16 );
            }
            else {
                n = Integer.parseInt( s );
            }
        }
        catch( NumberFormatException e )
            {
                JOptionPane.showMessageDialog( this, e );
                colorsConfigured = false;
                return 0;
            }
        return n;
    }
    
    private void configureColors() {
        
        colors = new int[6];
        colorsConfigured = true;        // set flag so we know colors are configured
        
        String s1 = JOptionPane.showInputDialog("outer start color?");  // Prompt user for colors
        String s2 = JOptionPane.showInputDialog("outer end color?");
        String s3 = JOptionPane.showInputDialog("mid start color?");
        String s4 = JOptionPane.showInputDialog("mid end color?");
        String s5 = JOptionPane.showInputDialog("inner start color?");
        String s6 = JOptionPane.showInputDialog("inner end color?");
        
        if( s1 == null || s2 == null || s3 == null || s4 == null || s5 == null || s6 == null){
            return;
        }
        try                                             // try to parse integer values from input strings
        {
            colors[0] = stringToInt( s1 );
            colors[1] = stringToInt( s2 );
            colors[2] = stringToInt( s3 );
            colors[3] = stringToInt( s4 );
            colors[4] = stringToInt( s5 );
            colors[5] = stringToInt( s6 );
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog( this, e );
            return;
        }
        populateColorArray();           // repopulate the Color array with new configuration
        if(image != null) {             // if image exists, redraw it with the new colors
            if(MandelbrotImage) {
                Mandelbrot("");
            }
            else if(JuliaImage) {
                Julia("");
            }
        }
    }
    
    private void setupImage() {
        image = new BufferedImage(600, 450, BufferedImage.TYPE_INT_ARGB);
        g2d = (Graphics2D) image.createGraphics();
        g2d.setColor( Color.WHITE );
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        populateColorArray();
        
        panel = new FractalDisplayPanel( image );
        JLabel label = new JLabel("Click and hold to zoom (LMB to zoom in/RMB to zoom out)", 0);
        
        this.getContentPane().add( panel, BorderLayout.CENTER );
        this.getContentPane().add( label, BorderLayout.SOUTH );
        this.pack();
        
        framesPerSec = 15;
        freshImage();
    }
    
    private void freshImage() {         // clear image state, set white background
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
        configureFPS(true);
        repaint();
    }
    
    private void zoomIn() {
        if( image == null || (MandelbrotImage == false && JuliaImage == false) )
            return;
        x0 = .0125;
        y0 = .0125;
        x1 = .9875;
        y1 = .9875;
        
        if(MandelbrotImage){
            Mandelbrot("");
        }
        else{
            Julia("");
        }
    }
    
    private void zoomOut() {
        if( image == null || (MandelbrotImage == false && JuliaImage == false) )
            return;
        x0 = -.025;
        y0 = -.025;
        x1 = 1.025;
        y1 = 1.025;
        
        if(MandelbrotImage){
            Mandelbrot("");
        }
        else{
            Julia("");
        }
    }

    class FractalDisplayPanel extends JPanel
    {
        private Color OUTLINE_COLOR = Color.BLACK;
        // panel size
        private final int WIDTH, MAX_X;
        private final int HEIGHT, MAX_Y;
        // image displayed on panel
        private BufferedImage image;
        private Graphics2D g2d;
        // current selection
        private int x = -1;
        private int y = -1;
        private int w = 0;
        private int h = 0;
        
        public FractalDisplayPanel( BufferedImage image )
        {
            this.image = image;
            g2d = image.createGraphics();
            g2d.setXORMode( OUTLINE_COLOR );
            // define panel characteristics
            WIDTH = image.getWidth();
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
                    if(event.getButton() == MouseEvent.BUTTON1)
                    {
                        LMBisPressed( event.getPoint() );
                    }
                    else if(event.getButton() == MouseEvent.BUTTON2)
                    {
                        RMBisPressed( event.getPoint() );
                    }
                    else if(event.getButton() == MouseEvent.BUTTON3)
                    {
                        RMBisPressed( event.getPoint() );
                    }
                }
            } );
            addMouseMotionListener( new MouseMotionAdapter()
            {
                public void mouseDragged(MouseEvent event)
                {
                    updateSelection( event.getPoint() );
                }
            } );
            addMouseListener( new MouseAdapter()
            {
                public void mouseReleased(MouseEvent event)
                {
                    mouseIsReleased( event.getPoint() );
                }
            } );
        }
        //------------------------------------------------------------------------
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
                    p.setLocation(  x/((double) MAX_X), y/((double) MAX_Y) );
                else
                    p.setLocation(  x/((double) MAX_X), (y+h)/((double) MAX_Y) );
                else if ( h < 0 )
                    p.setLocation( (x+w)/((double) MAX_X), y/((double) MAX_Y) );
                else
                    p.setLocation( (x+w)/((double) MAX_X), (y+h)/((double) MAX_Y) );
            
            return p;
        }
        
        //------------------------------------------------------------------------
        // behaviors
        
        public void paintComponent( Graphics g )
        {
            super.paintComponent( g );
            g.drawImage( image, 0, 0, null );
        }
        
        private void updateSelection( Point p )
        {
            zoom_destination_x = p.getX();     // update zoom destination
            zoom_destination_y = p.getY();
        }
        
        private void LMBisPressed( Point p )
        {
            zoom_destination_x = p.getX();      // update zoom destination
            zoom_destination_y = p.getY();
            currentDirection = true;            // zooming in
            timer.start();                      // start zooming
        }
        
        private void RMBisPressed( Point p )
        {
            zoom_destination_x = p.getX();      // update zoom destination
            zoom_destination_y = p.getY();
            currentDirection = false;           // zooming out
            timer.start();                      // start zooming
        }
        
        private void mouseIsReleased( Point p )
        {
            timer.stop();                       // stop zooming
        }
    }
}


