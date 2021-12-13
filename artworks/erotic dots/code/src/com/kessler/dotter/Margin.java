package com.kessler.dotter;

import java.awt.Dimension;
import java.awt.Rectangle;

public class Margin {
	
	int			top;
	int			right;
	int			bottom;
	int			left;
	
	public Margin(String spec, Dimension dim)
	{
		if ( dim == null )
			dim = new Dimension(0, 0);
		
		if ( spec.indexOf(",") < 0 )
		{
			// all the same
			this.top = this.bottom = singleSpec(spec, dim.height);
			this.right = this.left = singleSpec(spec, dim.width);
		}
		else
		{
			String		toks[] = spec.split(",");
			
			this.top = singleSpec(toks[0], dim.height);
			this.right = singleSpec(toks[1], dim.width);
			this.bottom = singleSpec(toks[2], dim.height);
			this.left = singleSpec(toks[3], dim.width);
		}
	}

	private int singleSpec(String spec, int dim) 
	{
		if ( spec.endsWith("%") )
			return (int)Math.round(Double.parseDouble(spec.substring(0, spec.length() - 1)) / 100.0 * dim);
		else
			return Integer.parseInt(spec);
	}
	
	public int getTop() {
		return top;
	}

	public int getRight() {
		return right;
	}

	public int getBottom() {
		return bottom;
	}

	public int getLeft() {
		return left;
	}

	public Rectangle innerRect(Rectangle rect) 
	{
		Rectangle		inner = new Rectangle(rect);
		
		inner.x += left;
		inner.y += top;
		inner.width -= (left + right);
		inner.height -= (top + bottom);
		
		return inner;
	}
}
