package de.ducane.wwm.util;

import de.androbin.json.*;
import de.ducane.wwm.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;

public final class QuestionParser {
  private QuestionParser() {
  }
  
  @ SuppressWarnings( "unchecked" )
  public static List<List<Question>> getQuestions( final XArray array ) {
    final List<List<Question>> difficulties = new ArrayList<>();
    
    for ( int i = 0; i < array.size(); i++ ) {
      difficulties.add( new ArrayList<>() );
    }
    
    array.forEach( o -> {
      final XObject x = o.asObject();
      
      final String question = x.get( "question" ).asString();
      String[] answers = x.get( "answers" ).asStringArray();
      
      final int correctAnswer = x.get( "correctAnswer" ).asInt();
      final int difficulty = x.get( "difficulty" ).asInt();
      final String imageURL = x.get( "imageURL" ).asString();
      
      URL url = null;
      BufferedImage image = null;
      try {
        url = new URL( imageURL );
        image = ImageIO.read( url );
      } catch ( final Exception ignore ) {
      }
      
      final List<Question> difficultyList = difficulties.get( difficulty - 1 );
      difficultyList.add( new Question( question, answers, correctAnswer, image ) );
    } );
    
    return difficulties;
  }
}