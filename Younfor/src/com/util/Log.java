package com.util;

import java.io.FileWriter;
import java.io.IOException;

public class Log {
			public static Log log=null;
			public static String id;
			public static boolean debug=true;
			public static Log getIns(String id)
			{
				if(log==null)
				{
					Log.id=id;
					log=new Log();
				}
				return log;
			}
			public void log(String s) throws IOException
			{
				if(debug)
				{
					FileWriter fileWriter=new FileWriter("log"+Log.id+".txt",true);
					fileWriter.write(s+'\n');
					fileWriter.flush();
					fileWriter.close();
				}
			}
}
