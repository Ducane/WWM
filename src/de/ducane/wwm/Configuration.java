package de.ducane.wwm;

import de.androbin.json.*;
import java.awt.*;

public final class Configuration {
  private static final XObject CONFIG = XUtil.readJSON( "config.json" ).get().asObject();
  
  public static final class window_ {
    private static final XObject CONFIG_WINDOW = CONFIG.get( "window" ).asObject();
    
    public static final boolean RESIZABLE = CONFIG_WINDOW.get( "resizable" ).asBoolean();
    public static final boolean UNDECORATED = CONFIG_WINDOW.get( "undecorated" ).asBoolean();
    public static final float SCALE = CONFIG_WINDOW.get( "scale" ).asFloat();
    public static final String TITLE = CONFIG_WINDOW.get( "title" ).asString();
  }
  
  public static final class game_ {
    private static final XObject CONFIG_GAME = CONFIG.get( "game" ).asObject();
    
    public static final int FPS = CONFIG_GAME.get( "fps" ).asInt();
    
  }
  
  public static final class gui_ {
    private static final XObject CONFIG_GUI = CONFIG.get( "gui" ).asObject();
    
    public static final class menu_ {
      private static final XObject CONFIG_MENU = CONFIG_GUI.get( "menu" ).asObject();
      
      public static final String[] BUTTON_LABELS = CONFIG_MENU.get( "button_labels" )
          .asStringArray();
      public static final Color BUTTON_LABEL_COLOR = CONFIG_MENU.get( "button_label_color" )
          .asColor();
      
      public static final Color BUTTON_BORDER_COLOR = CONFIG_MENU.get( "button_border_color" )
          .asColor();
      public static final float BUTTON_BORDER_THICKNESS = CONFIG_MENU
          .get( "button_border_thickness" ).asFloat();
      
      public static final String BUTTON_FONT_NAME = CONFIG_MENU.get( "button_font_name" )
          .asString();
      public static final int BUTTON_FONT_STYLE = CONFIG_MENU.get( "button_font_style" ).asInt();
      public static final float BUTTON_FONT_SIZE = CONFIG_MENU.get( "button_font_size" ).asFloat();
      
      public static final float BUTTON_ARC = CONFIG_MENU.get( "button_arc" ).asFloat();
      public static final float BUTTON_DY = CONFIG_MENU.get( "button_dy" ).asFloat();
      public static final float BUTTON_HEIGHT = CONFIG_MENU.get( "button_height" ).asFloat();
      public static final float BUTTON_WIDTH = CONFIG_MENU.get( "button_width" ).asFloat();
      public static final float BUTTON_X = CONFIG_MENU.get( "button_x" ).asFloat();
      public static final float BUTTON_Y = CONFIG_MENU.get( "button_y" ).asFloat();
      
      public static final float IMAGE_X = CONFIG_MENU.get( "image_x" ).asFloat();
      public static final float IMAGE_Y = CONFIG_MENU.get( "image_y" ).asFloat();
      public static final float IMAGE_WIDTH= CONFIG_MENU.get( "image_width" ).asFloat();
      public static final float IMAGE_HEIGHT = CONFIG_MENU.get( "image_height" ).asFloat();
    }
    
    public static final class play_ {
      private static final XObject CONFIG_PLAY = CONFIG_GUI.get( "play" ).asObject();
      
      public static final Color BUTTON_LABEL_COLOR = CONFIG_PLAY.get( "button_label_color" )
          .asColor();
      
      public static final Color BUTTON_BORDER_COLOR = CONFIG_PLAY.get( "button_border_color" )
          .asColor();
      public static final float BUTTON_BORDER_THICKNESS = CONFIG_PLAY
          .get( "button_border_thickness" ).asFloat();
      
      public static final String BUTTON_FONT_NAME = CONFIG_PLAY.get( "button_font_name" )
          .asString();
      public static final int BUTTON_FONT_STYLE = CONFIG_PLAY.get( "button_font_style" ).asInt();
      public static final float BUTTON_FONT_SIZE = CONFIG_PLAY.get( "button_font_size" ).asFloat();
      
      public static final float BUTTON_DX = CONFIG_PLAY.get( "button_dx" ).asFloat();
      public static final float BUTTON_DY = CONFIG_PLAY.get( "button_dy" ).asFloat();
      public static final float BUTTON_HEIGHT = CONFIG_PLAY.get( "button_height" ).asFloat();
      public static final float BUTTON_WIDTH = CONFIG_PLAY.get( "button_width" ).asFloat();
      public static final float BUTTON_X = CONFIG_PLAY.get( "button_x" ).asFloat();
      public static final float BUTTON_Y = CONFIG_PLAY.get( "button_y" ).asFloat();
      
      public static final Color QUESTION_LABEL_COLOR = CONFIG_PLAY.get( "question_label_color" )
          .asColor();
      
      public static final Color QUESTION_BORDER_COLOR = CONFIG_PLAY.get( "question_border_color" )
          .asColor();
      public static final float QUESTION_BORDER_THICKNESS = CONFIG_PLAY
          .get( "question_border_thickness" ).asFloat();
      
      public static final String QUESTION_FONT_NAME = CONFIG_PLAY.get( "question_font_name" )
          .asString();
      public static final int QUESTION_FONT_STYLE = CONFIG_PLAY.get( "question_font_style" )
          .asInt();
      public static final float QUESTION_FONT_SIZE = CONFIG_PLAY.get( "question_font_size" )
          .asFloat();
      
      public static final float QUESTION_HEIGHT = CONFIG_PLAY.get( "question_height" ).asFloat();
      public static final float QUESTION_WIDTH = CONFIG_PLAY.get( "question_width" ).asFloat();
      public static final float QUESTION_X = CONFIG_PLAY.get( "question_x" ).asFloat();
      public static final float QUESTION_Y = CONFIG_PLAY.get( "question_y" ).asFloat();
      
      public static final String[] SCORE_LABELS = CONFIG_PLAY.get( "score_labels" ).asStringArray();
      public static final Color SCORE_LABEL_COLOR = CONFIG_PLAY.get( "score_label_color" )
          .asColor();
      
      public static final Color SCORE_BORDER_COLOR = CONFIG_PLAY.get( "score_border_color" )
          .asColor();
      public static final float SCORE_BORDER_THICKNESS = CONFIG_PLAY.get( "score_border_thickness" )
          .asFloat();
      
      public static final String SCORE_FONT_NAME = CONFIG_PLAY.get( "score_font_name" ).asString();
      public static final int SCORE_FONT_STYLE = CONFIG_PLAY.get( "score_font_style" ).asInt();
      public static final float SCORE_FONT_SIZE = CONFIG_PLAY.get( "score_font_size" ).asFloat();
      
      public static final float SCORE_DY = CONFIG_PLAY.get( "score_dy" ).asFloat();
      public static final float SCORE_Y = CONFIG_PLAY.get( "score_y" ).asFloat();
      
      public static final float SCORE_MARKER_X = CONFIG_PLAY.get( "score_marker_x" ).asFloat();
      public static final float SCORE_MARKER_SIZE = CONFIG_PLAY.get( "score_marker_width" )
          .asFloat();
      
      public static final float SLIDER_WIDTH = CONFIG_PLAY.get( "slider_width" ).asFloat();
      public static final float SLIDER_SPEED = CONFIG_PLAY.get( "slider_speed" ).asFloat();
      
      public static final Color ROUND_LABEL_COLOR = CONFIG_PLAY.get( "round_label_color" )
          .asColor();
      
      public static final String ROUND_FONT_NAME = CONFIG_PLAY.get( "round_font_name" ).asString();
      public static final int ROUND_FONT_STYLE = CONFIG_PLAY.get( "round_font_style" ).asInt();
      public static final float ROUND_FONT_SIZE = CONFIG_PLAY.get( "round_font_size" ).asFloat();
      
      public static final float JOKER_X = CONFIG_PLAY.get( "joker_x" ).asFloat();
      public static final float JOKER_Y = CONFIG_PLAY.get( "joker_y" ).asFloat();
      public static final float JOKER_DX = CONFIG_PLAY.get( "joker_dx" ).asFloat();
      public static final float JOKER_WIDTH = CONFIG_PLAY.get( "joker_width" ).asFloat();
      public static final float JOKER_HEIGHT = CONFIG_PLAY.get( "joker_height" ).asFloat();
      public static final float JOKER_ARC = CONFIG_PLAY.get( "joker_arc" ).asFloat();
      
      public static final float IMAGE_X = CONFIG_PLAY.get( "image_x" ).asFloat();
      public static final float IMAGE_Y = CONFIG_PLAY.get( "image_y" ).asFloat();
      public static final float IMAGE_SCALE = CONFIG_PLAY.get( "image_scale" ).asFloat();
      
      public static final Color FOCUS_COLOR = CONFIG_PLAY.get( "focus_color" ).asColor();
      public static final float FOCUS_ALPHA = CONFIG_PLAY.get( "focus_alpha" ).asFloat();
      
      public static final Color LINE_COLOR = CONFIG_PLAY.get( "line_color" ).asColor();
    }
    
    public static final class end_ {
      private static final XObject CONFIG_END = CONFIG_GUI.get( "end" ).asObject();
      
      public static final Color LABEL_COLOR = CONFIG_END.get( "label_color" ).asColor();
      
      public static final float LABEL_Y = CONFIG_END.get( "label_y" ).asFloat();
      
      public static final String LABEL_FONT_NAME = CONFIG_END.get( "label_font_name" ).asString();
      
      public static final int LABEL_FONT_STYLE = CONFIG_END.get( "label_font_style" ).asInt();
      public static final float LABEL_FONT_SIZE = CONFIG_END.get( "label_font_size" ).asFloat();
      
      public static final Color BUTTON_COLOR = CONFIG_END.get( "button_color" ).asColor();
      public static final Color BUTTON_COLOR_SELECTED = CONFIG_END.get( "button_color_selected" )
          .asColor();
      
      public static final Color BUTTON_BORDER_COLOR = CONFIG_END.get( "button_border_color" )
          .asColor();
      public static final float BUTTON_BORDER_THICKNESS = CONFIG_END
          .get( "button_border_thickness" ).asFloat();
      
      public static final String BUTTON_FONT_NAME = CONFIG_END.get( "button_font_name" ).asString();
      public static final int BUTTON_FONT_STYLE = CONFIG_END.get( "button_font_style" ).asInt();
      public static final float BUTTON_FONT_SIZE = CONFIG_END.get( "button_font_size" ).asFloat();
      
      public static final float BUTTON_ARC = CONFIG_END.get( "button_arc" ).asFloat();
      public static final float BUTTON_HEIGHT = CONFIG_END.get( "button_height" ).asFloat();
      public static final float BUTTON_WIDTH = CONFIG_END.get( "button_width" ).asFloat();
      public static final float BUTTON_Y = CONFIG_END.get( "button_y" ).asFloat();
      
      public static final String BUTTON_LABEL = CONFIG_END.get( "button_label" ).asString();
    }
  }
}