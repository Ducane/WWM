package de.ducane.wwm.screen;

import static de.androbin.collection.util.ObjectCollectionUtil.*;
import static de.androbin.math.util.floats.FloatArrayMathUtil.*;
import static de.androbin.math.util.floats.FloatMathUtil.*;
import static de.androbin.math.util.ints.IntRandomUtil.*;
import static de.ducane.util.StringUtil.*;
import static de.ducane.wwm.Configuration.gui_.play_.*;
import static de.ducane.wwm.Main.*;
import static java.awt.event.KeyEvent.*;
import de.androbin.gfx.util.*;
import de.androbin.json.*;
import de.androbin.screen.*;
import de.androbin.screen.transit.*;
import de.androbin.shell.*;
import de.androbin.shell.gfx.*;
import de.androbin.shell.input.*;
import de.ducane.wwm.*;
import de.ducane.wwm.gfx.*;
import de.ducane.wwm.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.sound.sampled.*;

public final class PlayScreen extends AbstractShell implements AWTGraphics {
  private static final int BUTTON_LENGTH = 4;
  private static final int JOKER_LENGTH = 3;
  
  private static final String[] RIGHT_SOUNDS = fill( new String[ 4 ],
      i -> "richtig_stufe_" + ( i + 1 ) + ".wav" );
  private static final String[] LEVELS = fill( new String[ 4 ],
      i -> "stufe_" + ( i + 1 ) + ".wav" );
  private static final String[] JOKERS = fill( new String[ 3 ], i -> "joker_" + i + ".png" );
  
  private SmoothScreenManager<AWTTransition> sm;
  private ResourceManager<String> rm;
  
  private List<List<Question>> difficulties;
  
  private Layout layout;
  private GUI gui;
  
  private Overlay overlay;
  
  private State state;
  private Question currentQuestion;
  
  private int round;
  private int difficulty;
  
  private int question_selection;
  private int joker_selection;
  
  public PlayScreen( final SmoothScreenManager<AWTTransition> sm, final ResourceManager<String> rm,
      final XArray array ) {
    this.sm = sm;
    this.rm = rm;
    
    loadResources();
    
    getInputs().keyboard = createKeyInput();
    getInputs().mouse = createMouseInput();
    getInputs().mouseMotion = createMouseMotionInput();
    
    difficulties = QuestionParser.getQuestions( array );
    
    nextQuestion();
  }
  
  private boolean canShift() {
    return state != State.QUESTION_FOOBAR && state != State.QUESTION_LOGGED;
  }
  
  private void logout() {
    if ( state == State.QUESTION_LOGGED ) {
      state = State.QUESTION_KEYSELECTION;
      rm.stopSounds();
      rm.loopSound( LEVELS[ difficulty ], Clip.LOOP_CONTINUOUSLY );
    }
  }
  
  private boolean isCorrectAnswer() {
    return question_selection == currentQuestion.correctAnswer;
  }
  
  private KeyInput createKeyInput() {
    return new KeyInput() {
      @ Override
      public void keyPressed( final int keycode ) {
        if ( overlay != null ) {
          overlay.actionPerformed();
          return;
        }
        
        if ( state == State.QUESTION_FOOBAR || gui.slider.progress >= 1F ) {
          return;
        }
        
        if ( canShift() ) {
          state = State.QUESTION_KEYSELECTION;
          switch ( keycode ) {
            case VK_UP:
              shiftSelection( -2 );
              break;
            case VK_DOWN:
              shiftSelection( 2 );
              break;
            case VK_RIGHT:
              shiftSelection( 1 );
              break;
            case VK_LEFT:
              shiftSelection( -1 );
              break;
            
            case VK_A:
              switchSelection( 0 );
              break;
            case VK_B:
              switchSelection( 1 );
              break;
            case VK_C:
              switchSelection( 2 );
              break;
            case VK_D:
              switchSelection( 3 );
              break;
          }
        }
      }
      
      @ Override
      public void keyReleased( final int keycode ) {
        if ( gui.slider.progress >= 1F && state != State.QUESTION_FOOBAR ) {
          return;
        }
        
        switch ( keycode ) {
          case KeyEvent.VK_F11:
            ON_FULLSCREEN_REQUESTED_LISTENER.onFullScreenRequested();
            break;
          
          case VK_BACK_SPACE:
            sm.fadeClose( new AWTSlideTransition( 1, 0, 1.0f ) );
            break;
          
          case VK_ENTER:
          case VK_SPACE:
            if ( state == State.QUESTION_FOOBAR && gui.slider.progress >= 1F ) {
              gui.slider.direction = false;
              state = State.QUESTION_KEYSELECTION;
              
              if ( isCorrectAnswer() && round < SCORE_LABELS.length ) {
                sm.fadeCall( PlayScreen.this, new AWTSlideTransition( 0, -1, 1.0f ) );
                nextQuestion();
              }
            } else if ( ( state == State.QUESTION_KEYSELECTION && gui.slider.progress < 1F )
                || state == State.QUESTION_LOGGED ) {
              runButtonCommand();
            }
            break;
          
          case VK_ESCAPE:
            logout();
            break;
        }
      }
    };
  }
  
  private MouseInput createMouseInput() {
    return new MouseInput() {
      @ Override
      public void mouseReleased( final int x, final int y, final int button ) {
        if ( overlay != null ) {
          overlay.actionPerformed();
          return;
        }
        
        if ( state == State.QUESTION_KEYSELECTION ) {
          for ( int i = 0; i < layout.buttons.bounds.length; i++ ) {
            if ( layout.buttons.visible[ i ]
                && layout.buttons.bounds[ i ].contains( x, y ) ) {
              state = State.QUESTION_MOUSESELECTION;
              question_selection = i;
              break;
            }
          }
        }
        
        if ( state == State.QUESTION_FOOBAR ) {
          if ( gui.slider.progress == 1F ) {
            gui.slider.direction = false;
            state = State.QUESTION_KEYSELECTION;
            
            if ( isCorrectAnswer() ) {
              sm.fadeCall( PlayScreen.this, new AWTSlideTransition( 0, -1, 1.0f ) );
              nextQuestion();
            }
          }
        } else if ( ( state == State.QUESTION_MOUSESELECTION && gui.slider.progress < 1F )
            || state == State.QUESTION_LOGGED ) {
          runButtonCommand();
        } else if ( state == State.JOKER_SELECTION ) {
          if ( layout.slider.jokers.visible[ joker_selection ] ) {
            runJokerCommand();
          }
        }
      }
    };
  }
  
  private MouseMotionInput createMouseMotionInput() {
    return new MouseMotionInput() {
      @ Override
      public void mouseMoved( int x, int y ) {
        if ( canShift() ) {
          boolean noButtonsEntered = true;
          for ( int i = 0; i < layout.buttons.bounds.length; i++ ) {
            if ( layout.buttons.visible[ i ]
                && layout.buttons.bounds[ i ].contains( x, y ) ) {
              state = State.QUESTION_MOUSESELECTION;
              question_selection = i;
              noButtonsEntered = false;
              break;
            }
          }
          
          for ( int i = 0; i < layout.slider.jokers.bounds.length; i++ ) {
            if ( layout.slider.jokers.visible[ i ]
                && layout.slider.jokers.bounds[ i ].contains( x, y ) ) {
              state = State.JOKER_SELECTION;
              joker_selection = i;
              noButtonsEntered = false;
              break;
            }
          }
          
          if ( noButtonsEntered ) {
            state = State.QUESTION_KEYSELECTION;
          }
        }
        
        if ( state != State.QUESTION_LOGGED && state != State.QUESTION_FOOBAR ) {
          gui.slider.direction = x >= Math.min( layout.slider.x,
              getWidth() - layout.slider.width / 4 );
        }
      }
    };
  }
  
  private void loadResources() {
    rm.loadImage( "play_background", "play_background.png" );
    rm.loadImage( "play_image", "wer_wird_milionar.png" );
    
    rm.loadImage( "button", "button.png" );
    rm.loadImage( "button_selected", "button_selected.png" );
    rm.loadImage( "button_logged", "button_logged.png" );
    rm.loadImage( "button_correct", "button_correct.png" );
    rm.loadImage( "button_wrong", "button_wrong.png" );
    
    rm.loadImage( "score", "score.png" );
    rm.loadImage( "score_selected", "score_selected.png" );
    
    for ( final String key : JOKERS ) {
      rm.loadImage( key, key );
    }
    
    rm.loadSound( "false", "falsch.wav" );
    
    for ( final String key : RIGHT_SOUNDS ) {
      rm.loadSound( key, key );
    }
    
    for ( final String key : LEVELS ) {
      rm.loadSound( key, key );
    }
    
    rm.loadSound( "eingeloggt", "eingeloggt_start.wav" );
    rm.loadSound( "eingeloggt_loop", "eingeloggt_loop.wav" );
    rm.loadSound( "wechsel", "wechsel_nach_stufe_2.wav" );
    rm.loadSound( "50_50", "50_50.wav" );
    rm.loadSound( "telefonjoker_loop", "telefonjoker_loop.wav" );
    rm.loadSound( "telefonjoker_ende", "telefonjoker_ende.wav" );
    rm.loadSound( "publikumsjoker_ende", "publikumsjoker_ende.wav" );
  }
  
  private void nextQuestion() {
    if ( round < SCORE_LABELS.length ) {
      difficulty = round == SCORE_LABELS.length - 1 ? 3 : round / 5;
      
      final Question current = randomElement( difficulties.get( difficulty ), null );
      currentQuestion = current;
      difficulties.get( difficulty ).remove( currentQuestion );
    } else {
      switchToEndscreen();
    }
    
    // TODO: Ask robin because of bug
    if ( layout != null ) {
      for ( int i = 0; i < layout.buttons.visible.length; i++ ) {
        layout.buttons.visible[ i ] = true;
      }
    }
    
    round++;
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
    
    for ( final Overlay overlay : Overlay.values() ) {
      overlay.updateData( width, height );
    }
  }
  
  @ Override
  public void onResumed() {
    rm.stopSounds();
    rm.loopSound( LEVELS[ difficulty ], Clip.LOOP_CONTINUOUSLY );
  }
  
  @ Override
  public void render( final Graphics2D g ) {
    gui.render( g );
    
    if ( overlay != null ) {
      overlay.render( g, getWidth(), getHeight() );
    }
  }
  
  private void runButtonCommand() {
    if ( state == State.QUESTION_LOGGED ) {
      rm.stopSounds();
      rm.playSound( isCorrectAnswer() ? RIGHT_SOUNDS[ difficulty ] : "false" );
      state = State.QUESTION_FOOBAR;
    } else {
      rm.stopSounds();
      rm.playSound( "eingeloggt" );
      rm.loopSound( "eingeloggt_loop", Clip.LOOP_CONTINUOUSLY );
      state = State.QUESTION_LOGGED;
    }
  }
  
  private void runJokerCommand() {
    switch ( joker_selection ) {
      case 0: {
        final int[] r = random( null, BUTTON_LENGTH, BUTTON_LENGTH >> 1,
            currentQuestion.correctAnswer );
        
        for ( final int i : r ) {
          layout.buttons.visible[ i ] = false;
        }
        
        while ( !layout.buttons.visible[ question_selection ] ) {
          question_selection++;
          question_selection %= BUTTON_LENGTH;
        }
        
        rm.playSound( "50_50" );
        
        break;
      }
      
      case 1: {
        overlay = Overlay.TELEFON_JOKER;
        overlay.reset();
        overlay.init( this );
        
        rm.stopSounds();
        rm.loopSound( "telefonjoker_loop", Clip.LOOP_CONTINUOUSLY );
        break;
      }
      
      case 2: {
        overlay = Overlay.CROWD_JOKER;
        overlay.reset();
        overlay.init( this );
        
        rm.stopSounds();
        rm.playSound( "publikumsjoker_ende" );
        break;
      }
    }
    
    layout.slider.jokers.visible[ joker_selection ] = false;
  }
  
  private void shiftSelection( final int shift ) {
    switchSelection(
        de.androbin.math.util.ints.IntMathUtil.mod( question_selection + shift, BUTTON_LENGTH ) );
  }
  
  private void switchToEndscreen() {
    final EndScreen screen = new EndScreen( rm );
    screen.setScore( round - 1 == 0 ? "0"
        : SCORE_LABELS[ round == SCORE_LABELS.length ? round - 1 : round - 2 ] );
    
    sm.fadeCall( screen, new AWTColorCrossfade( Color.BLACK, 0.5f, 1.0f ) );
  }
  
  private void switchSelection( final int selection ) {
    if ( canShift() ) {
      int i = selection;
      
      while ( !layout.buttons.visible[ i ] ) {
        i++;
        i %= BUTTON_LENGTH;
      }
      
      this.question_selection = i;
    }
  }
  
  @ Override
  public void update( final float delta ) {
    gui.update( delta, layout );
    
    if ( overlay != null && overlay.update( delta ) ) {
      if ( overlay == Overlay.TELEFON_JOKER ) {
        rm.stopSounds();
        rm.playSound( "telefonjoker_ende" );
      }
      overlay = null;
      
      rm.loopSound( LEVELS[ difficulty ], Clip.LOOP_CONTINUOUSLY );
    }
    
    if ( state == State.QUESTION_FOOBAR ) {
      final Clip sound = rm.getSound( isCorrectAnswer() ? RIGHT_SOUNDS[ difficulty ] : "false" );
      final float music_progress = (float) sound.getMicrosecondPosition()
          / sound.getMicrosecondLength();
      
      if ( music_progress >= 1F ) {
        if ( isCorrectAnswer() && round < SCORE_LABELS.length ) {
          gui.slider.direction = true;
        } else {
          switchToEndscreen();
        }
      }
    }
  }
  
  private enum Overlay {
    TELEFON_JOKER {
      private final Color BACKGROUND_COLOR = new Color( 0f, 0f, 0f, 0.5f );
      private final Color TEXT_COLOR = Color.WHITE;
      
      private Font font;
      
      private float time;
      
      @ Override
      public void actionPerformed() {
      }
      
      @ Override
      public void init( final PlayScreen screen ) {
      }
      
      @ Override
      public void render( final Graphics2D g, final int width, final int height ) {
        {
          g.setColor( BACKGROUND_COLOR );
          g.fillRect( 0, 0, width, height );
        }
        
        {
          g.setColor( TEXT_COLOR );
          g.setFont( font );
          
          final FontMetrics fm = g.getFontMetrics();
          final String text = String.valueOf( Math.round( time ) );
          final Rectangle2D bounds = fm.getStringBounds( text, g );
          
          g.drawString( text, width - (int) bounds.getWidth() >> 1,
              ( height - (int) bounds.getHeight() >> 1 ) + fm.getAscent() );
        }
      }
      
      @ Override
      public void reset() {
        time = 30f;
      }
      
      @ Override
      public boolean update( final float delta ) {
        time -= delta;
        return time <= 0f;
      }
      
      @ Override
      public void updateData( final int width, final int height ) {
        font = new Font( "Calibri", Font.PLAIN, (int) ( 0.25f * height ) );
      }
    },
    CROWD_JOKER {
      private final Color BACKGROUND_COLOR = new Color( 0f, 0f, 0f, 0.5f );
      
      private final BufferedImage BAR = ImageUtil.loadImage( "crowd_joker_bar.png" );
      
      private final String[] LETTERS = { "A", "B", "C", "D" };
      
      private float[] p;
      
      private boolean alive;
      private float progress;
      
      @ Override
      public void actionPerformed() {
        alive = false;
      }
      
      @ Override
      public void init( final PlayScreen screen ) {
        if ( p == null ) {
          p = new float[ BUTTON_LENGTH ];
        }
        
        final Random random = ThreadLocalRandom.current();
        
        for ( int i = 0; i < p.length; i++ ) {
          if ( !screen.layout.buttons.visible[ i ] ) {
            continue;
          }
          
          p[ i ] = bound( -1f, (float) random.nextGaussian(), 3f ) * 0.25f + 0.25f;
          
          if ( i == screen.currentQuestion.correctAnswer ) {
            p[ i ] += 0.5f / ( screen.difficulty + 1 );
          }
        }
        
        norm1( p );
      }
      
      @ Override
      public void render( final Graphics2D g, final int width, final int height ) {
        {
          g.setColor( BACKGROUND_COLOR );
          g.fillRect( 0, 0, width, height );
        }
        
        final float y = 0.1875f * height;
        
        final float w = 0.125f * width;
        final float h = 0.625f * height;
        
        {
          g.setColor( new Color( 0f, 0.5f, 1f, 0.25f ) );
          g.fillRect( (int) ( 0.1f * width ), (int) y, (int) ( 0.8f * width ), (int) h );
        }
        
        {
          for ( int i = 0; i < BUTTON_LENGTH; i++ ) {
            final float x = ( 0.1f + i * 0.225f ) * width;
            
            final float ph = p[ i ] * progress * h;
            final float py = y + h - ph;
            
            final int lx = (int) ( x + ( w - g.getFontMetrics()
                .stringWidth( LETTERS[ i ] + " (" + (int) ( p[ i ] * 100 ) + "%)" ) ) / 2 );
            final int ly = (int) ( y + h + y / 2 );
            
            g.drawImage( BAR, (int) x, (int) py, (int) w, (int) ph, null );
            
            g.setColor( Color.WHITE );
            g.drawString( LETTERS[ i ] + " (" + (int) ( p[ i ] * 100 ) + "%)", lx, ly );
          }
        }
        
        {
          g.setColor( new Color( 0f, 0.25f, 0.5f ) );
          g.drawRect( (int) ( 0.1f * width ), (int) y, (int) ( 0.8f * width ), (int) h );
        }
      }
      
      @ Override
      public void reset() {
        alive = true;
        progress = 0f;
      }
      
      @ Override
      public boolean update( final float delta ) {
        progress = Math.min( progress + delta, 1f );
        return !alive;
      }
      
      @ Override
      public void updateData( final int width, final int height ) {
      }
    };
    
    public abstract void actionPerformed();
    
    public abstract void init( final PlayScreen screen );
    
    public abstract void render( final Graphics2D g, final int width, final int height );
    
    public abstract void reset();
    
    public abstract boolean update( final float delta );
    
    public abstract void updateData( final int width, final int height );
  }
  
  private enum State {
    QUESTION_MOUSESELECTION,
    QUESTION_KEYSELECTION,
    JOKER_SELECTION,
    QUESTION_LOGGED,
    QUESTION_FOOBAR;
  }
  
  private class Layout {
    public final Buttons buttons;
    public final Slider slider;
    public final Question question;
    public final Background background;
    
    public Layout() {
      buttons = new Buttons();
      slider = new Slider();
      question = new Question();
      background = new Background();
    }
    
    public void updateData() {
      buttons.updateData();
      slider.updateData();
      question.updateData();
      background.updateData();
    }
    
    private final class Buttons {
      public Hexagon[] bounds;
      
      public boolean[] visible;
      
      public int x;
      public int y;
      public int width;
      public int height;
      
      public int dx;
      public int dy;
      
      public Buttons() {
        init();
      }
      
      public void init() {
        initBounds();
        initVisibility();
      }
      
      private void initBounds() {
        bounds = new Hexagon[ BUTTON_LENGTH ];
      }
      
      private void initVisibility() {
        visible = new boolean[ BUTTON_LENGTH ];
        Arrays.fill( visible, true );
      }
      
      public void updateData() {
        x = (int) ( BUTTON_X * getWidth() );
        y = (int) ( BUTTON_Y * getHeight() );
        dy = (int) ( BUTTON_DY * getHeight() );
        dx = (int) ( BUTTON_DX * getWidth() );
        width = (int) ( BUTTON_WIDTH * getWidth() );
        height = (int) ( BUTTON_HEIGHT * getHeight() );
        
        updateBounds();
      }
      
      private void updateBounds() {
        for ( int i = 0; i < bounds.length; i++ ) {
          final int bx = x + dx * ( i % 2 );
          final int by = y + dy * ( i / 2 );
          
          bounds[ i ] = new Hexagon( bx, by, width, height );
        }
      }
    }
    
    private final class Slider {
      private int x;
      private int width;
      
      public Score score = new Score();
      public Jokers jokers = new Jokers();
      
      public void setX( final int x ) {
        if ( x == this.x ) {
          return;
        }
        
        this.x = x;
        updateBounds();
      }
      
      public void updateBounds() {
        jokers.updateBounds();
      }
      
      public void updateData() {
        width = (int) ( SLIDER_WIDTH * getWidth() );
        
        score.updateData();
        jokers.updateData();
      }
      
      private final class Jokers {
        private RoundRectangle2D[] bounds;
        
        private boolean[] visible;
        
        private int x;
        private int y;
        private int height;
        private int dx;
        private int arc;
        
        private Joker joker;
        
        public Jokers() {
          init();
        }
        
        public void init() {
          initBounds();
          initJoker();
          initVisibility();
        }
        
        private void initBounds() {
          bounds = new RoundRectangle2D.Float[ JOKER_LENGTH ];
        }
        
        private void initJoker() {
          joker = new Joker();
        }
        
        private void initVisibility() {
          visible = new boolean[ JOKER_LENGTH ];
          Arrays.fill( visible, true );
        }
        
        public void updateBounds() {
          for ( int i = 0; i < jokers.bounds.length; i++ ) {
            jokers.bounds[ i ] = new RoundRectangle2D.Float( slider.x + jokers.x + jokers.dx * i,
                jokers.y, jokers.joker.width, jokers.height, jokers.arc, jokers.arc );
          }
        }
        
        public void updateData() {
          x = (int) ( JOKER_X * width );
          y = (int) ( JOKER_Y * getHeight() );
          height = (int) ( JOKER_HEIGHT * getHeight() );
          dx = (int) ( JOKER_DX * width );
          arc = (int) ( JOKER_ARC * width );
          
          joker.updateData();
        }
        
        private final class Joker {
          private int width;
          
          public void updateData() {
            width = (int) ( JOKER_WIDTH * Slider.this.width );
          }
        }
      }
      
      private final class Score {
        private int y;
        private int dy;
        
        private int font_height;
        private int border_width;
        
        private int marker_x;
        private int marker_size;
        
        public void updateData() {
          y = (int) ( SCORE_Y * getHeight() );
          dy = (int) ( SCORE_DY * getHeight() );
          
          font_height = (int) ( SCORE_FONT_SIZE * getHeight() );
          border_width = (int) ( SCORE_BORDER_THICKNESS * getHeight() );
          
          marker_x = (int) ( SCORE_MARKER_X * width );
          marker_size = (int) ( SCORE_MARKER_SIZE * width );
        }
      }
    }
    
    private final class Question {
      private Hexagon bounds;
      
      private int x;
      private int y;
      private int width;
      private int height;
      
      private void updateBounds() {
        bounds = new Hexagon( x, y, width, height );
      }
      
      public void updateData() {
        x = (int) ( QUESTION_X * getWidth() );
        y = (int) ( QUESTION_Y * getHeight() );
        width = (int) ( QUESTION_WIDTH * getWidth() );
        height = (int) ( QUESTION_HEIGHT * getHeight() );
        
        updateBounds();
      }
    }
    
    private final class Background {
      private int image_x;
      private int image_y;
      private int image_scale;
      
      public void updateData() {
        image_x = (int) ( IMAGE_X * getWidth() );
        image_y = (int) ( IMAGE_Y * getHeight() );
        image_scale = (int) ( IMAGE_SCALE * getWidth() );
      }
    }
  }
  
  private class GUI {
    private final Background background;
    private final Question question;
    private final Buttons buttons;
    private final Slider slider;
    
    public GUI() {
      background = new Background();
      question = new Question();
      buttons = new Buttons();
      slider = new Slider();
    }
    
    public void update( final float delta, final Layout layout ) {
      slider.update( delta, layout.slider );
    }
    
    private void updateData() {
      background.updateData( layout.background, layout.buttons );
      question.updateData( layout.question );
      buttons.updateData( layout.buttons );
      slider.updateData( layout.slider );
    }
    
    public void render( final Graphics2D g ) {
      background.render( g, layout.background, layout.buttons );
      question.render( g, layout.question );
      buttons.render( g, layout.buttons );
      
      {
        final Color color = new Color( FOCUS_COLOR.getRed(), FOCUS_COLOR.getGreen(),
            FOCUS_COLOR.getBlue(), FOCUS_ALPHA * slider.progress );
        g.setColor( color );
        g.fillRect( 0, 0, getWidth(), getHeight() );
      }
      
      slider.render( g, layout.slider );
    }
    
    private class Buttons {
      public Stroke border;
      
      public Font font;
      
      public void updateData( final Layout.Buttons layout ) {
        font = new Font( BUTTON_FONT_NAME, BUTTON_FONT_STYLE,
            (int) ( BUTTON_FONT_SIZE * layout.height ) );
        border = new BasicStroke( BUTTON_BORDER_THICKNESS * layout.height );
      }
      
      private void render( final Graphics2D g,
          final Layout.Buttons layout ) {
        g.setColor( LINE_COLOR );
        
        for ( int i = 0; i < layout.bounds.length >> 1; i++ ) {
          g.drawLine( 0, layout.y + layout.dy * i + layout.height / 2, getWidth(),
              layout.y + layout.dy * i + layout.height / 2 );
        }
        
        for ( int i = 0; i < layout.bounds.length; i++ ) {
          if ( !layout.visible[ i ] ) {
            continue;
          }
          
          {
            final BufferedImage image = (BufferedImage) rm.getImage( "button" );
            final BufferedImage image_selected = (BufferedImage) rm.getImage( "button_selected" );
            final BufferedImage image_logged = (BufferedImage) rm.getImage( "button_logged" );
            final BufferedImage image_correct = (BufferedImage) rm.getImage( "button_correct" );
            final BufferedImage image_wrong = (BufferedImage) rm.getImage( "button_wrong" );
            
            g.setFont( font );
            g.drawImage(
                state == State.QUESTION_LOGGED ? i == question_selection ? image_logged : image
                    : state == State.QUESTION_FOOBAR
                        ? i == currentQuestion.correctAnswer ? image_correct : image_wrong
                        : i == question_selection ? image_selected : image,
                layout.bounds[ i ].getBounds().x, layout.bounds[ i ].getBounds().y,
                layout.bounds[ i ].getBounds().width, layout.bounds[ i ].getBounds().height,
                null );
          }
          
          {
            g.setStroke( border );
            g.setColor( BUTTON_BORDER_COLOR );
            layout.bounds[ i ].draw( g );
          }
          
          {
            final String answer = currentQuestion.answers[ i ];
            final FontMetrics fm = g.getFontMetrics();
            final Rectangle2D rect = fm.getStringBounds( answer, g );
            
            final int bx = layout.x + layout.dx * ( i % 2 );
            final int by = layout.y + layout.dy * ( i / 2 );
            final int px = bx + ( layout.width - (int) rect.getWidth() ) / 2;
            final int py = by + ( layout.height - (int) rect.getHeight() ) / 2;
            
            g.setColor( BUTTON_LABEL_COLOR );
            g.drawString( answer, px, py + fm.getAscent() );
          }
        }
      }
    }
    
    private class Slider {
      public Jokers jokers;
      public Score score;
      
      private boolean direction;
      
      private float progress;
      
      public Slider() {
        jokers = new Jokers();
        score = new Score();
      }
      
      public void render( final Graphics2D g,
          final Layout.Slider layout ) {
        score.render( g, layout, layout.score );
        jokers.render( g, layout, layout.jokers );
      }
      
      public void update( final float delta, final Layout.Slider layout ) {
        progress = direction ? Math.min( progress + SLIDER_SPEED * delta, 1 )
            : Math.max( progress - SLIDER_SPEED * delta, 0 );
        layout.setX( (int) ( getWidth() + layout.score.border_width
            - Math.sin( progress * Math.PI / 2 ) * ( layout.width - 1 ) ) );
      }
      
      public void updateData( final Layout.Slider layout ) {
        layout.x = (int) ( ( getWidth() + layout.score.border_width
            - Math.sin( progress * Math.PI ) * layout.width ) );
        score.updateData( layout, layout.score );
      }
      
      private class Jokers {
        public void render( final Graphics2D g,
            final Layout.Slider layout_slider, final Layout.Slider.Jokers layout ) {
          for ( int i = 0; i < layout.bounds.length; i++ ) {
            if ( !layout.visible[ i ] ) {
              continue;
            }
            
            layout.bounds[ i ] = new RoundRectangle2D.Float(
                layout_slider.x + layout.x + layout.dx * i, layout.y, layout.joker.width,
                layout.height, layout.arc, layout.arc );
            
            final RoundRectangle2D joker = layout.bounds[ i ];
            g.drawImage( rm.getImage( JOKERS[ i ] ), (int) joker.getX(), (int) joker.getY(),
                (int) joker.getWidth(), (int) joker.getHeight(), null );
          }
        }
      }
      
      private class Score {
        private Stroke border;
        
        private Font font;
        
        public void render( final Graphics2D g,
            final Layout.Slider layout_slider, final Layout.Slider.Score layout ) {
          g.drawImage( rm.getImage( "score" ), layout_slider.x, layout.y,
              layout_slider.width, getHeight(), null );
          g.setFont( font );
          
          for ( int i = 0; i < SCORE_LABELS.length; i++ ) {
            final int y = getHeight() - layout.dy * i;
            final FontMetrics fm = g.getFontMetrics();
            
            if ( i == round - 1 ) {
              g.setStroke( border );
              g.setColor( SCORE_BORDER_COLOR );
              g.drawRect( layout_slider.x,
                  state == State.QUESTION_FOOBAR ? y - 2 * layout.dy : y - layout.dy,
                  layout_slider.width, layout.dy );
              g.drawImage( rm.getImage( "score_selected" ), layout_slider.x,
                  state == State.QUESTION_FOOBAR ? y - 2 * layout.dy : y - layout.dy,
                  getWidth() - layout_slider.x, layout.dy, null );
            }
            
            {
              final int width = fm.stringWidth( SCORE_LABELS[ i ] );
              
              g.setColor( SCORE_LABEL_COLOR );
              g.drawString( SCORE_LABELS[ i ], layout_slider.x + layout_slider.width - width,
                  y - fm.getDescent() );
            }
          }
          
          for ( int i = 1; state == State.QUESTION_FOOBAR ? i <= round : i <= round - 1; i++ ) {
            final int y = getHeight() - layout.dy * i;
            g.fillOval( layout_slider.x + layout.marker_x,
                ( y + y + layout.dy ) / 2 - layout.marker_size / 2, layout.marker_size,
                layout.marker_size );
          }
          
          {
            g.setStroke( border );
            g.setColor( SCORE_BORDER_COLOR );
            g.drawRect( layout_slider.x, layout.y, layout_slider.width, getHeight() );
          }
        }
        
        public void updateData( final Layout.Slider layout_slider,
            final Layout.Slider.Score layout ) {
          font = new Font( SCORE_FONT_NAME, SCORE_FONT_STYLE, layout.font_height );
          border = new BasicStroke( layout.border_width );
        }
        
      }
    }
    
    private class Question {
      private Stroke border;
      
      private Font font;
      
      public void render( final Graphics2D g,
          final Layout.Question layout ) {
        g.drawLine( 0, layout.y + layout.height / 2, getWidth(), layout.y + layout.height / 2 );
        g.drawImage( rm.getImage( "button" ), layout.x, layout.y, layout.width,
            layout.height,
            null );
        
        {
          g.setStroke( border );
          g.setColor( QUESTION_BORDER_COLOR );
          layout.bounds.draw( g );
        }
        
        {
          g.setFont( font );
          
          final FontMetrics fm = g.getFontMetrics();
          final List<String> trim = wrapLines( currentQuestion.text, fm,
              layout.width - layout.width / 24 );
          
          final ListIterator<String> iter = trim.listIterator();
          
          while ( iter.hasNext() ) {
            final int i = iter.nextIndex();
            final Rectangle2D rect = fm.getStringBounds( iter.next(), g );
            
            final int x = layout.x + ( layout.width - (int) rect.getWidth() ) / 2;
            final int y = layout.y + (int) rect.getHeight();
            
            g.drawString( trim.get( i ), x, y + fm.getHeight() * i );
          }
        }
      }
      
      public void updateData( final Layout.Question layout ) {
        font = new Font( QUESTION_FONT_NAME, QUESTION_FONT_STYLE,
            (int) ( QUESTION_FONT_SIZE * layout.height ) );
        border = new BasicStroke( QUESTION_BORDER_THICKNESS * layout.height );
      }
      
    }
    
    private class Background {
      private Font round_font;
      
      public void render( final Graphics2D g,
          final Layout.Background layout_background, final Layout.Buttons layout_buttons ) {
        g.drawImage( rm.getImage( "play_background" ), 0, 0, getWidth(), getHeight(),
            null );
        
        g.setFont( round_font );
        
        final FontMetrics fm = g.getFontMetrics();
        final String round = String.valueOf( PlayScreen.this.round );
        
        g.setColor( ROUND_LABEL_COLOR );
        g.drawString( round, ( getWidth() - fm.stringWidth( round ) ) / 2,
            layout_buttons.y + layout_buttons.dy );
        
        final BufferedImage image;
        
        if ( currentQuestion.image == null ) {
          image = (BufferedImage) rm.getImage( "play_image" );
          
          // TODO: Deprecated
          // g.drawImage( image, layout_background.image_x, layout_background.image_y,
          // layout_background.image_scale, layout_background.image_scale, null );
          
        } else {
          image = currentQuestion.image;
        }
        
        final float srcWidth = image.getWidth();
        final float srcHeight = image.getHeight();
        
        final float destWidth = getWidth() * 0.95f;
        final float destHeight = getHeight() * 0.5f;
        
        final float scale = Math.min( destWidth / srcWidth, destHeight / srcHeight );
        
        final int width = (int) ( srcWidth * scale );
        final int height = (int) ( srcHeight * scale );
        
        final int x = (int) ( ( destWidth - width ) / 2f + 0.025f * getWidth() );
        final int y = (int) ( ( destHeight - height ) / 2f + 0.025f * getHeight() );
        
        g.drawImage( image, x, y, width, height, null );
        
      }
      
      public void updateData( final Layout.Background layout_background,
          final Layout.Buttons layout_buttons ) {
        round_font = new Font( ROUND_FONT_NAME, ROUND_FONT_STYLE,
            (int) ( ROUND_FONT_SIZE * getHeight() ) );
      }
      
    }
  }
}