package com.kessler.thechain;


public class Monitor {
	
	static boolean 	globalSilence = true;
	
	long			startedAt = System.currentTimeMillis();
	String			msg;
	
	public Monitor()
	{
		
	}
	
	public Monitor(String msg)
	{
		this.msg = msg;
	}
	
	public long done()
	{
		return done(globalSilence, msg);
	}
	
	public long done(boolean silence)
	{
		return done(silence, msg);
	}
	
	public long done(boolean silence, String msg)
	{
		long		milli = System.currentTimeMillis() - startedAt;
		
		if ( !silence && (msg != null) )
			System.out.println(msg + ": " + milli + "ms");
		
		return milli;
	}
}
