package de.ducane.wwm;

import de.androbin.gfx.*;
import de.androbin.screen.*;
import de.androbin.screen.transit.*;
import de.androbin.shell.env.*;
import de.ducane.wwm.Configuration.*;
import de.ducane.wwm.screen.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class Main {
  public static OnFullscreenRequestedListener ON_FULLSCREEN_REQUESTED_LISTENER;
  
  private Main() {
  }
  
  public static void main( final String[] args ) throws Exception {
    // setSystemLookAndFeel();
    
    final SmoothScreenManager<AWTTransition> screens = new AWTScreenManager();
    final SimpleResourceManager<String> resources = new SimpleResourceManager<>();
    
    final AWTEnv env = new AWTEnv( screens, 60 );
    env.start( window_.TITLE );
    
    final CustomPane canvas = env.canvas;
    
    SwingUtilities.invokeAndWait( () -> {
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      final int width = (int) ( screenSize.getWidth() * window_.SCALE );
      final int height = (int) ( screenSize.getHeight() * window_.SCALE );
      
      final JFrame window = new JFrame( window_.TITLE );
      window.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
      window.setUndecorated( window_.UNDECORATED );
      window.setBackground( Color.BLACK );
      window.setIgnoreRepaint( true );
      window.setSize( width, height );
      window.setResizable( window_.RESIZABLE );
      window.setLocationRelativeTo( null );
      window.addWindowListener( new WindowAdapter() {
        @ Override
        public void windowClosing( final WindowEvent event ) {
          screens.exit();
        }
      } );
      
      setOnFullscreenRequestedListener( () -> {
        final GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();
        
        final boolean fullscreen = graphicsDevice.getFullScreenWindow() == window;
        
        window.dispose();
        if ( fullscreen ) {
          graphicsDevice.setFullScreenWindow( null );
          window.setUndecorated( false );
        } else {
          window.setUndecorated( true );
          graphicsDevice.setFullScreenWindow( window );
        }
        window.setVisible( true );
      } );
      
      window.setContentPane( canvas );
      window.setVisible( true );
    } );
    
    SwingUtilities.invokeAndWait( () -> {
      screens.call( new MenuScreen( screens, resources ) );
    } );
  }
  
  private static void setOnFullscreenRequestedListener(
      final OnFullscreenRequestedListener listener ) {
    ON_FULLSCREEN_REQUESTED_LISTENER = listener;
  }
}