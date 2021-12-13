package eu.crydee.syllablecounter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class SyllableCounterMain {
	
	static public void main(String args[]) throws IOException
	{
		BufferedReader		reader = new BufferedReader(new FileReader(args[0]));
		String				line;
		SyllableCounter		sc = new SyllableCounter();
		PrintWriter			pw = new PrintWriter(args[1]);
		
		while ( (line = reader.readLine()) != null )
		{
			StringBuffer		sb = new StringBuffer();
			int					count;
			int					total = 0;
			
			for ( String word : line.split("/") )
			{
				count = sc.count(word);
				total += count;
				
				sb.append(' ');
				sb.append(Integer.toString(total));
			}
			
			pw.print(total);
			pw.println(sb);
		}
		
		reader.close();
		pw.close();
	}

}
