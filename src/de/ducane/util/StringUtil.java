package de.ducane.util;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class StringUtil {
  private StringUtil() {
  }
  
  public static List<String> wrapLines( final String label, final FontMetrics fm,
      final int width ) {
    final List<String> lines = new LinkedList<>();
    String l = label;
    
    while ( fm.stringWidth( l ) > width ) {
      final int i = trimIndex( l, fm, width );
      lines.add( l.substring( 0, i ) );
      l = l.substring( i + 1 );
    }
    
    lines.add( l );
    return lines;
  }
  
  public static int trimIndex( final String l, final FontMetrics fm, final int width ) {
    int pw = 0;
    
    for ( int i = 0; i < l.length(); i++ ) {
      pw += fm.charWidth( l.charAt( i ) );
      
      if ( pw > width ) {
        return l.lastIndexOf( ' ', i );
      }
    }
    
    return -1;
  }
}