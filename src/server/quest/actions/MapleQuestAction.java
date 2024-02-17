/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.quest.actions;

import client.MapleCharacter;
import provider.MapleData;
import server.quest.MapleQuest;
import server.quest.MapleQuestActionType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @author Tyler (Twdtwd)
 */
public abstract class MapleQuestAction{

	private final MapleQuestActionType type;
	protected int questID;

	public MapleQuestAction(MapleQuestActionType action, MapleQuest quest){
		this.type = action;
		this.questID = quest.getId();
	}

	public abstract void run(MapleCharacter chr, Integer extSelection);

	public abstract void processData(MapleData data);

	public abstract void processData(LittleEndianAccessor lea);

	public abstract void writeData(LittleEndianWriter lew);

	public boolean check(MapleCharacter chr, Integer extSelection){
		return true;
	}

	public MapleQuestActionType getType(){
		return type;
	}
}
