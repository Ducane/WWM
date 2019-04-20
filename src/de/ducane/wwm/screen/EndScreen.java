package de.ducane.wwm.screen;

import static de.ducane.wwm.Configuration.gui_.end_.*;
import de.androbin.screen.*;
import de.androbin.shell.*;
import de.androbin.shell.gfx.*;
import de.androbin.shell.input.*;
import de.ducane.wwm.*;
import de.ducane.wwm.gfx.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public final class EndScreen extends AbstractShell implements AWTGraphics {
  private ResourceManager<String> rm;
  
  private Layout layout;
  
  private String score;
  
  private Stroke button_stroke;
  
  private Font label_font;
  private Font button_font;
  
  private int label_y;
  
  private boolean selected;
  
  public EndScreen( final ResourceManager<String> rm ) {
    this.rm = rm;
    
    loadResources();
    
    getInputs().mouse = createMouseInput();
    getInputs().mouseMotion = createMouseMotionInput();
  }
  
  private void loadResources() {
    rm.loadImage( "end_background", "end_background.png" );
    
    rm.loadImage( "button", "button.png" );
    rm.loadImage( "button_selected", "button_selected.png" );
    
    rm.loadSound( "outro", "outro.wav" );
  }
  
  public void keyPressed( final KeyEvent event ) {
    switch ( event.getKeyCode() ) {
      case KeyEvent.VK_F11:
        Main.ON_FULLSCREEN_REQUESTED_LISTENER.onFullScreenRequested();
        break;
    }
  }
  
  public MouseInput createMouseInput() {
    return new MouseInput() {
      @ Override
      public void mouseReleased( final int x, final int y, final int button ) {
        if ( selected ) {
          System.exit( 0 );
        }
      }
    };
  }
  
  public MouseMotionInput createMouseMotionInput() {
    return new MouseMotionInput() {
      @ Override
      public void mouseMoved( final int x, final int y ) {
        selected = layout.button.bounds.contains( x, y );
      }
    };
  }
  
  @ Override
  protected void onResized( final int width, final int height ) {
    if ( layout == null ) {
      layout = new Layout();
    }
    
    label_y = (int) ( LABEL_Y * height );
    label_font = new Font( LABEL_FONT_NAME, LABEL_FONT_STYLE, (int) ( LABEL_FONT_SIZE * width ) );
    
    button_font = new Font( BUTTON_FONT_NAME, BUTTON_FONT_STYLE,
        (int) ( BUTTON_FONT_SIZE * height ) );
    button_stroke = new BasicStroke( BUTTON_BORDER_THICKNESS * height );
    
    layout.updateData();
  }
  
  @ Override
  public void onResumed() {
    rm.stopSounds();
    rm.playSound( "outro" );
  }
  
  @ Override
  public void render( Graphics2D g ) {
    g.drawImage( rm.getImage( "end_background" ), 0, 0, getWidth(), getHeight(), null );
    renderLabel( g );
    renderButton( g, layout.button );
  }
  
  private void renderLabel( final Graphics2D g ) {
    g.setColor( LABEL_COLOR );
    g.setFont( label_font );
    
    final FontMetrics fm = g.getFontMetrics();
    final String text = "Score:";
    
    g.drawString( text, getWidth() - fm.stringWidth( text ) >> 1, getHeight() >> 2 );
    g.drawString( score, getWidth() - fm.stringWidth( score ) >> 1, label_y );
  }
  
  private void renderButton( final Graphics2D g, final Layout.Button layout ) {
    g.drawImage( selected ? rm.getImage( "button_selected" ) : rm.getImage( "button" ), layout.x,
        layout.y, layout.width, layout.height, null );
    
    {
      final Stroke oldStroke = g.getStroke();
      g.setColor( BUTTON_BORDER_COLOR );
      g.setStroke( button_stroke );
      layout.bounds.draw( g );
      g.setStroke( oldStroke );
    }
    
    {
      final FontMetrics fm = g.getFontMetrics( button_font );
      final Rectangle2D b = fm.getStringBounds( BUTTON_LABEL, g );
      
      final int dx = (int) ( layout.width - b.getWidth() ) / 2;
      final int dy = (int) ( layout.height - b.getHeight() ) / 2;
      
      g.setFont( button_font );
      g.drawString( BUTTON_LABEL, layout.x + dx, layout.y + dy + fm.getAscent() );
    }
  }
  
  public void setScore( final String score ) {
    this.score = score;
  }
  
  @ Override
  public void update( final float delta ) {
  }
  
  private class Layout {
    private final Button button;
    
    public Layout() {
      button = new Button();
    }
    
    public void updateData() {
      button.updateData();
    }
    
    private class Button {
      private Hexagon bounds;
      
      private int height;
      private int width;
      private int x;
      private int y;
      
      public void updateData() {
        width = (int) ( BUTTON_WIDTH * getWidth() );
        height = (int) ( BUTTON_HEIGHT * getHeight() );
        
        x = getWidth() - width >> 1;
        y = (int) ( BUTTON_Y * getHeight() );
        
        bounds = new Hexagon( x, y, width, height );
      }
    }
  }
  
}