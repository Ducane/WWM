package de.ducane.wwm;

import java.awt.image.*;

public final class Question
{
	public final String[] answers;
	public final String   text;
	public final int	   correctAnswer;
	public final BufferedImage image;
						   
	public Question( final String question, final String[] answers, final int correctAnswer, final BufferedImage image ) {
	  this.text = question;
    this.answers = answers;
    this.correctAnswer = correctAnswer;
    this.image = image; 
	}
}