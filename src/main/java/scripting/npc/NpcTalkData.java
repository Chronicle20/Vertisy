package scripting.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 1, 2017
 */
public class NpcTalkData{

	public ScriptMessageType messageType;
	public byte speakerTypeID, param;
	public boolean prev, next;// Say
	public int lenMin, lenMax;// AskText
	public short col, line;// AskBoxText
	public int def, min, max;// AskNumber
	public int[] canadite;// AskAvatar, AskAvatar, AskMemberShopAvatar
	public int speakerTemplateID;
	public PetTalkData[] petData;// AskPet, AskPetAll
	public String msg, msgDefault = "";
	public List<Integer> validSelections = new ArrayList<>();
	public static Pattern pattern = Pattern.compile("\\#(.[0-9])\\#");

	public static class PetTalkData{

		public byte pos;
		public long uniqueid;
	}

	public void parseText(String text){
		Matcher matcher = pattern.matcher(text);
		while(matcher.find()){
			String m = matcher.group();// this gave "h0"
			if(m.toLowerCase().contains("l")){
				m = m.replace("#", "");
				m = m.replace("l", "");
				m = m.replace("L", "");
				Integer sel = ObjectParser.isInt(m);
				if(sel != null) validSelections.add(sel);
			}
		}
		/*String lower = text.toLowerCase();
		int start = -1;
		while((start = lower.indexOf("#l")) != -1){
			int selEnd = lower.indexOf("#", start + 1);
			if(selEnd == -1) break;
			selEnd = selEnd < lower.length() ? selEnd + 1 : lower.length();
			String sel = lower.substring(start + 2, selEnd - 1);
			// System.out.println("sel: " + sel);
			int restStart = -1;
			int end = lower.indexOf("#l", selEnd);
			if(end == -1){
				end = lower.length();
				restStart = lower.length();
			}else{
				int nextSel = lower.indexOf("#l", end + 1);
				if(nextSel != -1 && isSelectionStart(lower, end + 2, nextSel)){
					// System.out.println("Found sel start instead of end");
					end = end - 1;
					restStart = end;
				}else{
					restStart = end + 2;
				}
			}
			// System.out.println("Test: " + lower.substring(selEnd));
			// System.out.println(end + ", " + lower.length());
			String selText = lower.substring(selEnd, end);
			// System.out.println("selText: " + selText);
			validSelections.add(ObjectParser.isInt(sel));
			text = text.substring(0, start) + "<a href=\"" + sel + "\">" + selText + "</a>" + text.substring(restStart, text.length());
			// System.out.println(text);
			lower = text.toLowerCase();
			start = -1;
		}*/
	}

	private boolean isSelectionStart(String text, int start, int end){
		String sel = text.substring(start, end);
		if(ObjectParser.isInt(sel) != null) return true;
		// System.out.println("test: " + sel);
		return false;
	}
}
