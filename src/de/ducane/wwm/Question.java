package de.ducane.wwm;

public final class Question
{
	public final String[] answers;
	public final String   text;
	public final int	   correctAnswer;
						   
	public Question( final String question, final String[] answers, final int correctAnswer )
	{
		this.text = question;
		this.answers = answers;
		this.correctAnswer = correctAnswer;
	}
}