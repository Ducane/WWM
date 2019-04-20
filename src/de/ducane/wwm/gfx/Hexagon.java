package de.ducane.wwm.gfx;

import java.awt.*;

public final class Hexagon extends Polygon
{
	private static final long serialVersionUID = 1L;
	
	public Hexagon( final int x, final int y, final int width, final int height )
	{
		final int[] xPoints =
		{ x, x + width / 24, x + width - width / 24, x + width, x + width - width / 24, x + width / 24 };
		this.xpoints = xPoints;
		
		final int[] yPoints =
		{ y + height / 2, y, y, y + height / 2, y + height, y + height };
		this.ypoints = yPoints;
		this.npoints = xpoints.length;
	}
	
	public void draw( final Graphics2D g )
	{
		g.drawPolygon( xpoints, ypoints, npoints );
	}
	
	public void fill( final Graphics2D g )
	{
		g.fillPolygon( xpoints, ypoints, npoints );
	}
}