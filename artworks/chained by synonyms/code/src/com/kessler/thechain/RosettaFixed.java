package com.kessler.thechain;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class RosettaFixed {
	
	List<List<String>>		paths = new LinkedList<List<String>>();
	int						pathCountGoal = 100;
	int						desiredWidth = 101;
	TheRosetta				tr;
	
	static public void main(String[] args) throws IOException
	{
		final TheRosetta	tr = new TheRosetta(new FileInputStream(args[0]));
		final RosettaFixed	rf = new RosettaFixed(tr);
		
		tr.density = 0.01;
		rf.app(args[1], args[2]);
		
		/*
		for ( List<String> path : rf.paths )
			System.out.println(rf.getPathLine(path));
		*/
	}
	
	public RosettaFixed(TheRosetta tr)
	{
		this.tr = tr;
	}
	
	public void app(String poll1, String poll2) throws IOException
	{
		paths.clear();
		
		tr.app(poll1, poll2, new IPathCollector() {
			
			int				count;
			int				positive;
			Set<Integer>	widths = new LinkedHashSet<Integer>();
			
			@Override
			public boolean addPath(List<String> path) {
				
				count++;
				int			width = getPathWidth(path);
				widths.add(width);
				if ( width == desiredWidth )
				{
					paths.add(path);
					System.out.println(getPathLine(path) + " " + paths.size());
					positive++;
				}
				
				if ( (count % 10) == 0 )
					System.out.println("count: " + count + ", positive: " + positive + ", widths: " + widths);
				
				return paths.size() < pathCountGoal;
			}
		});
	}
	
	private int getPathWidth(List<String> path)
	{
		int			width = 0;
		
		// collect letters
		for ( String word : path )
			width += word.length();
		
		// add separators
		width += (path.size() - 1);
		
		return width;
	}
	
	private String getPathLine(List<String> path)
	{
		return StringUtils.join(path, '/');
	}
}
