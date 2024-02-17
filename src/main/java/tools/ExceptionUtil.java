package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 24, 2016
 */
public class ExceptionUtil{

	private static List<String> packageNames = Arrays.asList("ca", "client", "constants", "crypto", "dropspider", "net", "provider", "scripting", "server", "tools");
	private static List<String> ignoredPackageNames = Arrays.asList("tools.thread", "net.MapleServerHandler", "tools.ExceptionUtil");

	public static String buildException(){
		return buildExceptionWithFilter(new Throwable());// faster methods exist, but they only give class names
	}

	public static String buildExceptionWithFilter(Throwable throwable){
		return buildExceptionWithFilter(throwable.getStackTrace());
	}

	public static String buildExceptionWithFilter(StackTraceElement[] elements){
		StringBuilder sb = new StringBuilder();
		ste: for(StackTraceElement e : elements){
			for(String s : ignoredPackageNames){
				if(e.getClassName().startsWith(s)) continue ste;
			}
			for(String packageName : packageNames){
				if(e.getClassName().startsWith(packageName)){
					sb.append("at ");
					sb.append(e.getClassName() + "." + e.getMethodName() + "(" + e.getFileName() + ":" + e.getLineNumber() + ")");
					sb.append("\r\n");
					break;
				}
			}
		}
		return sb.toString();
	}

	public static String getStringFromThrowable(final Throwable e){
		try(StringWriter sw = new StringWriter()){
			try(PrintWriter pw = new PrintWriter(sw)){
				e.printStackTrace(pw);
				return sw.toString();
			}
		}catch(IOException e1){
			e1.printStackTrace();
		}
		return null;
	}
}
