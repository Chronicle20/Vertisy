package server.quest.requirements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import client.MapleCharacter;
import provider.MapleData;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class DayOfWeekRequirement extends MapleQuestRequirement{

	List<String> days = new ArrayList<>();

	public DayOfWeekRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.DAY_OF_WEEK);
		processData(data);
	}

	public DayOfWeekRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.DAY_OF_WEEK);
		processData(lea);
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		for(MapleData weekEntry : data.getChildren()){
			days.add(weekEntry.getName());
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int total = lea.readInt();
		for(int i = 0; i < total; i++){
			days.add(lea.readMapleAsciiString());
		}
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(days.size());
		for(String day : days){
			lew.writeMapleAsciiString(day);
		}
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		String day = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
		for(String d : days){
			if(d.toLowerCase().equals(day)) return true;
		}
		return false;
	}
}
