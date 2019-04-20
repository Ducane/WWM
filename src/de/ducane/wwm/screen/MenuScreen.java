package de.ducane.wwm.screen;

import static de.androbin.math.util.ints.IntMathUtil.*;
import static de.ducane.wwm.Configuration.gui_.menu_.*;
import de.androbin.json.*;
import de.androbin.screen.*;
import de.androbin.screen.transit.*;
import de.androbin.shell.*;
import de.androbin.shell.gfx.*;
import de.androbin.shell.input.*;
import de.ducane.wwm.gfx.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public final class MenuScreen extends AbstractShell implements AWTGraphics {
  private ResourceManager<String> rm;
  private SmoothScreenManager<AWTTransition> sm;
  
  private Layout layout;
  private GUI gui;
  
  private Path questions;
  
  private State state;
  
  private int selection;
  
  public MenuScreen( final SmoothScreenManager<AWTTransition> sm,
      final ResourceManager<String> rm ) {
    this.rm = rm;
    this.sm = sm;
    
    state = State.SELECTION;
    
    loadResources();
    
    getInputs().keyboard = createKeyInput();
    getInputs().mouse = createMouseInput();
    getInputs().mouseMotion = createMouseMotionInput();
  }
  
  public KeyInput createKeyInput() {
    return new KeyInput() {
      @ Override
      public void keyPressed( final int keycode ) {
        if ( state == State.CROSSING ) {
          return;
        }
        
        switch ( keycode ) {
          case KeyEvent.VK_W:
          case KeyEvent.VK_UP:
            shiftSelection( -1 );
            break;
          
          case KeyEvent.VK_S:
          case KeyEvent.VK_DOWN:
            shiftSelection( 1 );
            break;
          
          case KeyEvent.VK_F11:
            break;
        }
      }
      
      @ Override
      public void keyReleased( final int keycode ) {
        switch ( keycode ) {
          case KeyEvent.VK_SPACE:
          case KeyEvent.VK_ENTER:
            runCommand( selection );
            break;
        }
      }
    };
  }
  
  public MouseInput createMouseInput() {
    return new MouseInput() {
      @ Override
      public void mouseReleased( final int x, final int y, final int button ) {
        if ( layout.buttons.bounds[ selection ].contains( x, y ) ) {
          runCommand( selection );
        }
      }
    };
  }
  
  public MouseMotionInput createMouseMotionInput() {
    return new MouseMotionInput() {
      @ Override
      public void mouseMoved( final int x, final int y ) {
        if ( state == State.CROSSING ) {
          return;
        }
        
        for ( int i = 0; i < layout.buttons.bounds.length; i++ ) {
          if ( layout.buttons.bounds[ i ].contains( x, y ) ) {
            selection = i;
          }
        }
      }
    };
  }
  
  private void loadResources() {
    rm.loadImage( "menu_background", "menu_background.png" );
    rm.loadImage( "ohm_background",  "ohm_background.png" );
    
    rm.loadImage( "button", "button.png" );
    rm.loadImage( "button_selected", "button_selected.png" );
    
    rm.loadSound( "intro", "intro.wav" );
  }
  
  @ Override
  protected void onResized( final int width, final int height ) {
    if ( layout == null ) {
      layout = new Layout();
    }
    
    if ( gui == null ) {
      gui = new GUI();
    }
    
    layout.updateData();
    gui.updateData();
  }
  
  @ Override
  public void onResumed() {
    state = State.SELECTION;
    rm.stopSounds();
    rm.loopSound( "intro", Clip.LOOP_CONTINUOUSLY );
  }
  
  private void runCommand( final int index ) {
    switch ( index ) {
      case 0:
        if ( questions == null ) {
          JOptionPane.showMessageDialog( null,
              "Es sind noch keine Fragen hinzugefuegt worden!", "Fehler",
              JOptionPane.ERROR_MESSAGE );
        } else {
          state = State.CROSSING;
          
          sm.fadeCall( new PlayScreen( sm, rm, XUtil.readJSON( questions ).get().asArray() ),
              new AWTColorCrossfade( Color.BLACK, 0.5f, 1.0f ) );
        }
        break;
      case 1:
        final JFileChooser chooser = new JFileChooser();
        
        final FileNameExtensionFilter filter = new FileNameExtensionFilter( "json (*.json)",
            "json" );
        chooser.setFileFilter( filter );
        
        final int option = chooser.showOpenDialog( null );
        
        if ( option == JFileChooser.APPROVE_OPTION ) {
          final File selectedFile = chooser.getSelectedFile();
          final String name = selectedFile.getName();
          
          if ( name.endsWith( ".json" ) ) {
            questions = selectedFile.toPath();;
            JOptionPane.showMessageDialog( null,
                "Die hinzugefuegten Fragen koennen jetzt verwendet werden.",
                selectedFile.getPath(), JOptionPane.INFORMATION_MESSAGE );
          } else {
            JOptionPane.showMessageDialog( null, "Es dürfen nur .json Dateien ausgewaehlt werden!",
                "Fehler", JOptionPane.WARNING_MESSAGE );
          }
        }
        break;
      case 2:
        System.exit( 0 );
        break;
    }
  }
  
  private void shiftSelection( final int shift ) {
    selection = mod( selection + shift, BUTTON_LABELS.length );
  }
  
  private enum State {
    CROSSING, SELECTION;
  }
  
  private class Layout {
    private final Buttons buttons;
    private final Background background;
    
    public Layout() {
      buttons = new Buttons();
      background = new Background();
    }
    
    public void updateData() {
      buttons.updateData();
      background.updateData();
    }
    
    private class Buttons {
      private Hexagon[] bounds;
      
      private int x;
      private int y;
      private int dy;
      private int width;
      private int height;
      
      public Buttons() {
        init();
      }
      
      private void init() {
        initBounds();
      }
      
      private void initBounds() {
        bounds = new Hexagon[ BUTTON_LABELS.length ];
      }
      
      private void updateBounds() {
        for ( int i = 0; i < bounds.length; i++ ) {
          bounds[ i ] = new Hexagon( x, y + dy * i, width, height );
        }
      }
      
      private void updateData() {
        x = (int) ( BUTTON_X * getWidth() );
        y = (int) ( BUTTON_Y * getHeight() );
        dy = (int) ( BUTTON_DY * getHeight() );
        width = (int) ( BUTTON_WIDTH * getWidth() );
        height = (int) ( BUTTON_HEIGHT * getHeight() );
        
        updateBounds();
      }
    }
    
    private class Background {
      private int image_height;
      private int image_width;
      private int image_x;
      private int image_y;
      
      public void updateData() {
        image_x = (int) ( IMAGE_X * getWidth() );
        image_y = (int) ( IMAGE_Y * getHeight() );
        image_width = (int) ( IMAGE_WIDTH * getWidth() );
        image_height = (int) ( IMAGE_HEIGHT * getHeight() );
      }
    }
  }
  
  private class GUI {
    private final Background background;
    private final Buttons buttons;
    
    public GUI() {
      background = new Background();
      buttons = new Buttons();
    }
    
    public void updateData() {
      buttons.updateData( layout.buttons );
    }
    
    public void render( final Graphics2D g ) {
      background.render( g, layout.background );
      buttons.render( g, layout.buttons );
    }
    
    private class Buttons {
      private Font font;
      private Stroke stroke;
      
      public void updateData( final Layout.Buttons layout ) {
        font = new Font( BUTTON_FONT_NAME, BUTTON_FONT_STYLE,
            (int) ( BUTTON_FONT_SIZE * getHeight() ) );
        stroke = new BasicStroke( BUTTON_BORDER_THICKNESS * layout.height );
      }
      
      public void render( final Graphics2D g, final Layout.Buttons layout ) {
        for ( int i = 0; i < BUTTON_LABELS.length; i++ ) {
          g.drawImage( selection == i ? rm.getImage( "button_selected" ) : rm.getImage( "button" ),
              layout.x, layout.y + ( layout.dy * i ), layout.width, layout.height, null );
          
          {
            g.setStroke( stroke );
            g.setColor( BUTTON_BORDER_COLOR );
            layout.bounds[ i ].draw( g );
          }
          
          final String label = BUTTON_LABELS[ i ];
          
          final FontMetrics fm = g.getFontMetrics( font );
          final Rectangle2D b = fm.getStringBounds( label, g );
          
          final int dx = (int) ( layout.width - b.getWidth() ) / 2;
          final int dy = (int) ( layout.height - b.getHeight() ) / 2;
          
          g.setColor( BUTTON_LABEL_COLOR );
          g.setFont( font );
          g.drawString( label, layout.x + dx, layout.y + layout.dy * i + dy + fm.getAscent() );
        }
      }
    }
    
    private class Background {
      public void render( final Graphics2D g, final Layout.Background layout ) {
        g.drawImage( rm.getImage( "menu_background" ), 0, 0, getWidth(), getHeight(), null );
        g.drawImage( rm.getImage( "ohm_background" ), layout.image_x, layout.image_y, layout.image_width,
         layout.image_height, null );
      }
    }
  }
  
  @ Override
  public void render( final Graphics2D g ) {
    gui.render( g );
  }
}