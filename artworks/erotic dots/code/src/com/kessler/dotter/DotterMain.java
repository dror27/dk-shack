package com.kessler.dotter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.imageio.ImageIO;

public class DotterMain {
	
	private Properties		props;
	private File			propsFolder;
	private BufferedImage	outputImage;
	private Margin			outputMargin;
	private Graphics2D		outputGraphics;
	
	static public void main(String[] args) throws IOException
	{
		DotterMain			dotter = new DotterMain(args[0]);
		
		dotter.run();
	}
	
	static private void listFonts()
	{
		String	fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

	    for ( int i = 0; i < fonts.length; i++ )
	    {
	      System.out.println(fonts[i]);
	    }
	}
	
	public DotterMain(String propsFilename) throws IOException
	{
		File			propsFile = new File(propsFilename);
		System.out.println("reading props from: " + propsFile.getAbsolutePath());
		
		props = new Properties();		
		props.load(new FileInputStream(propsFile));
		propsFolder = propsFile.getParentFile();
		
		if ( Boolean.parseBoolean(props.getProperty("listFonts", "false")) )
			listFonts();
	}
	
	public void run() throws IOException
	{
		Date			startedAt = new Date();
		System.out.println("DotterMain, started at " + startedAt);
		
		// general
		String				dotMethod = props.getProperty("dotMethod", "block");
		
		// open master image
		File				masterImageFile = new File(expandFilename(props.getProperty("masterImage")));
		System.out.println("reading masterImage at: " + masterImageFile.getAbsolutePath());
		BufferedImage		masterImage = ImageIO.read(masterImageFile);
		System.out.println("masterImage: " + imageInfo(masterImage));
		
		// open dot image?
		BufferedImage		dotImage = null;
		double				dotFactor = 1.0;
		if ( dotMethod.equals("image") )
		{
			File				dotImageFile = new File(expandFilename(props.getProperty("dotImage")));
			System.out.println("reading dotImage at: " + dotImageFile.getAbsolutePath());
			dotImage = ImageIO.read(dotImageFile);
			System.out.println("dotImage: " + imageInfo(dotImage));			
			
			if ( dotImage.getWidth() != dotImage.getHeight() )
				fatal("dotImage should be square");
			
			dotFactor = Double.parseDouble(props.getProperty("dotFactor", "1.0"));
		}
		
		// create output image
		outputMargin = new Margin(props.getProperty("outputMargin", "0"), new Dimension(masterImage.getWidth(), masterImage.getHeight()));
		int					outputWidth = masterImage.getWidth() + outputMargin.getLeft() + outputMargin.getRight();
		int					outputHeight = masterImage.getHeight() + outputMargin.getTop() + outputMargin.getBottom();
		outputImage = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
		outputGraphics = outputImage.createGraphics();
		Color				backgroundColor = Color.decode(props.getProperty("backgroundColor", "#FFFFFF"));
		outputGraphics.setPaint(backgroundColor);
		outputGraphics.fillRect(0, 0, outputImage.getWidth(), outputImage.getHeight());
		
		// loop over dot regions
		int					dotSize = Integer.parseInt(props.getProperty("dotSize", "16"));
		System.out.println("dotSize: " + dotSize);
		int					row = 0;
		for ( int y = 0 ; y < masterImage.getHeight() - dotSize ; y += dotSize )
		{
			int			rowMod = row & 0x01;
			int			initialX = dotSize / 2 * rowMod; 
			for ( int x = initialX ; x < masterImage.getWidth() - dotSize ; x += dotSize )
			{
				double		intensity = calcDotIntensity(masterImage, x, y, dotSize);
				//System.out.println(String.format("intensity: %d:%d %dx%d %f", x, y, dotSize, dotSize, intensity));
				
				int			outputX = x + outputMargin.getLeft();
				int			outputY = y + outputMargin.getTop();
				
				if ( dotMethod.equals("block") )
					setBlockDotIntensity(outputX, outputY, dotSize, intensity);
				else if ( dotMethod.equals("circle") )
					setCircleDotIntensity(outputX, outputY, dotSize, intensity);
				else if ( dotMethod.equals("newspaper") )
					setNewspaperDotIntensity(outputX, outputY, dotSize, intensity);
				else if ( dotMethod.equals("image") )
					setImageDotIntensity(outputX, outputY, dotSize, intensity, dotImage, dotFactor);
			}
			row++;
		}
		
		// add caption
		addCaption();
		
		// write output image
		File				outputImageFile = new File(expandFilename(props.getProperty("outputImage")));
		String				nameToks[] = outputImageFile.getName().split("\\.");
		String				formatName = nameToks[nameToks.length - 1];
		System.out.println("writing outputImage at: " + outputImageFile.getAbsolutePath() + ", formatName: " + formatName);
		ImageIO.write(outputImage, formatName, outputImageFile);
		
		// finished
		Date				finishedAt = new Date();
		System.out.println(String.format("DotterMain, took %.3f seconds", (finishedAt.getTime() - startedAt.getTime()) / 1000.0));		
	}

	private String expandFilename(String path) 
	{
		if ( path.startsWith("/") )
			return path;
		else
			return propsFolder.getAbsolutePath() + "/" + path;
	}

	private void addCaption() 
	{
		// get text
		String		text = props.getProperty("captionText");
		if ( text == null )
			return;
		
		// calc text area
		Rectangle	area = new Rectangle(0, outputImage.getHeight() - outputMargin.getBottom(), outputImage.getWidth(), outputMargin.getBottom());
		Margin		margin = new Margin(props.getProperty("captionMargin", "0"), area.getSize());
		area = margin.innerRect(area);
		
		// get font
		String		fontName = props.getProperty("captionFont", Font.SANS_SERIF);
		Font		font = new Font(fontName, Font.PLAIN, (int)area.getHeight());
		
		// get color
		Color		color = Color.BLACK;
		String		colorName = props.getProperty("captionColor");
		if ( colorName != null )
			color = Color.decode(colorName);
		outputGraphics.setPaint(color);
		
		// draw
		drawCenteredString(outputGraphics, text, area, font);
	}
	
	// http://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
	public void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font) 
	{
	    // get the FontMetrics
	    FontMetrics 		metrics = g.getFontMetrics(font);
	    
	    // determine the X coordinate for the text
	    int x = (rect.width - metrics.stringWidth(text)) / 2;
	    
	    // determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
	    int y = ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
	    
	    // set the font
	    g.setFont(font);
	    
	    // draw the String
	    g.drawString(text, rect.x + x, rect.y + y);
	}

	private void fatal(String msg) 
	{
		System.err.println("FATAL: " + msg);
		
		System.exit(-1);
	}

	private double calcDotIntensity(BufferedImage img, int x, int y, int dotSize) 
	{
		int[]		pixels = new int[dotSize * dotSize];
		int			pixelsCount = pixels.length;
		double		accumulator = 0;
		
		img.getRGB(x, y, dotSize, dotSize, pixels, 0, dotSize);
		for ( int n = 0 ; n < pixelsCount ; n++ )
		{
			int				pixel = pixels[n];
			
			int				blue = pixel & 0xFF;
			int				green = (pixel >> 8) & 0xFF;
			int				red = (pixel >> 16) & 0xFF;
			
			accumulator += (blue / 255.0);
			accumulator += (green / 255.0);
			accumulator += (red / 255.0);
		}
		
		return accumulator / (pixelsCount * 3);
	}

	private void setBlockDotIntensity(int x, int y, int dotSize, double intensity) 
	{
		// make up pixel value
		int			value = ((int)Math.round(intensity * 255.0) & 0xFF);
		int			pixel = value + (value << 8) + (value << 16);
		//System.out.println(String.format("value pixel: %d %08x", value, pixel));
		int[]		pixels = new int[dotSize * dotSize];
		
		Arrays.fill(pixels, pixel);
		outputImage.setRGB(x, y, dotSize, dotSize, pixels, 0, dotSize);
	}

	private void setCircleDotIntensity(int x, int y, int dotSize, double intensity) 
	{
		// make up pixel value
		int			value = ((int)Math.round(intensity * 255.0) & 0xFF);
		int			pixel = value + (value << 8) + (value << 16);
		Color		color = new Color(pixel);
		
		outputGraphics.setPaint(color);
		outputGraphics.fillOval(x, y, dotSize, dotSize);
	}
	
	private void setNewspaperDotIntensity(int x, int y, int dotSize, double intensity) 
	{
		// calc radius;
		int			circleRadius = (int)Math.round((1 - intensity) * dotSize / 2);
		if ( circleRadius <= 0 )
			return;
		
		int			xc = x + dotSize / 2;
		int			yc = y + dotSize / 2;
		
		outputGraphics.setPaint(Color.BLACK);
		outputGraphics.fillOval(xc - circleRadius, yc - circleRadius, circleRadius * 2, circleRadius * 2); 
	}
	
	private void setImageDotIntensity(int x, int y, int dotSize, double intensity, BufferedImage dotImage, double factor) 
	{
		// calc radius;
		int			circleRadius = (int)Math.round((1 - intensity) * dotSize / 2 * factor);
		if ( circleRadius <= 0 )
			return;
		
		int			xc = x + dotSize / 2;
		int			yc = y + dotSize / 2;
		
		outputGraphics.drawImage(dotImage, 
				xc - circleRadius, yc - circleRadius, xc + circleRadius, yc + circleRadius
				,0, 0, dotImage.getWidth(), dotImage.getHeight() 
				,null);
	}
	
	private String imageInfo(Image image) 
	{
		StringBuilder		sb = new StringBuilder();
		
		sb.append(String.format("%dx%d", image.getWidth(null), image.getHeight(null)));
		
		if ( image instanceof BufferedImage )
		{
			BufferedImage		bufimg = (BufferedImage)image;
			
			sb.append(String.format(" %d:%d", bufimg.getMinX(), bufimg.getMinY()));
		}
		
		return sb.toString();
	}

}
