/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package scripting.quest;

import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import client.MapleClient;
import client.MapleQuestStatus;
import client.MessageType;
import scripting.AbstractScriptManager;
import server.quest.MapleQuest;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author RMZero213
 */
public class QuestScriptManager extends AbstractScriptManager{

	private Map<Integer, QuestActionManager> qms = new HashMap<>();
	private Map<Integer, Invocable> scripts = new HashMap<>();
	private static QuestScriptManager instance = new QuestScriptManager();

	public synchronized static QuestScriptManager getInstance(){
		return instance;
	}

	public void start(MapleClient c, short questid, int npc){
		MapleQuest quest = MapleQuest.getInstance(questid);
		if(!c.getPlayer().getQuest(quest).getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)){
			dispose(c);
			return;
		}
		try{
			if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Running script: " + quest.startQuestData.script + " Quest ID: " + questid + ", NPC: " + npc);
			QuestActionManager qm = new QuestActionManager(c, questid, npc, true);
			if(qms.containsKey(c)) return;
			if(c.canClickNPC()){
				qms.put(c.getAccID(), qm);
				ScriptEngine se = getInvocable("quest/" + questid + ".js", c);
				if(se == null){
					se = getInvocable("quest/" + quest.startQuestData.script + ".js", c);
				}
				if(se == null){
					se = getInvocable("quest/" + c.getWorld() + "/" + questid + ".js", c);
				}
				if(se == null){
					se = getInvocable("quest/" + c.getWorld() + "/" + quest.startQuestData.script + ".js", c);
				}
				if(se == null){
					Logger.log(LogType.INFO, LogFile.UNCODED, null, "Quest %s is not coded. Player: %s", questid, c.getPlayer().getName());
				}
				if(se == null || QuestScriptManager.getInstance() == null){
					qm.dispose();
					return;
				}
				se.put("qm", qm);
				Invocable iv = (Invocable) se;
				scripts.put(c.getAccID(), iv);
				c.setClickedNPC();
				iv.invokeFunction("start", (byte) 1, (byte) 0, 0);
			}
		}catch(final Throwable t){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, "Quest: " + getQM(c).getQuest());
			dispose(c);
		}
	}

	public void start(MapleClient c, byte mode, byte type, int selection){
		Invocable iv = scripts.get(c.getAccID());
		if(iv != null){
			try{
				c.setClickedNPC();
				iv.invokeFunction("start", mode, type, selection);
			}catch(final Throwable t){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, "Quest: " + getQM(c).getQuest());
				dispose(c);
			}
		}
	}

	public void end(MapleClient c, short questid, int npc){
		MapleQuest quest = MapleQuest.getInstance(questid);
		if(!c.getPlayer().getQuest(quest).getStatus().equals(MapleQuestStatus.Status.STARTED) || !c.getPlayer().getMap().containsNPC(npc)){
			dispose(c);
			return;
		}
		try{
			if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Running script: " + quest.completeQuestData.script + " Quest ID: " + questid + ", NPC: " + npc);
			QuestActionManager qm = new QuestActionManager(c, questid, npc, false);
			if(qms.containsKey(c)) return;
			if(c.canClickNPC()){
				qms.put(c.getAccID(), qm);
				ScriptEngine se = getInvocable("quest/" + questid + ".js", c);
				if(se == null) se = getInvocable("quest/" + quest.completeQuestData.script + ".js", c);
				if(se == null) se = getInvocable("quest/world" + c.getWorld() + "/" + questid + ".js", c);
				if(se == null) se = getInvocable("quest/world" + c.getWorld() + "/" + quest.completeQuestData.script + ".js", c);
				if(se == null){
					qm.dispose();
					return;
				}
				c.setClickedNPC();
				se.put("qm", qm);
				Invocable iv = (Invocable) se;
				scripts.put(c.getAccID(), iv);
				iv.invokeFunction("end", (byte) 1, (byte) 0, 0);
			}
		}catch(final Throwable t){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, "Quest: " + questid);
			dispose(c);
		}
	}

	public void end(MapleClient c, byte mode, byte type, int selection){
		Invocable iv = scripts.get(c.getAccID());
		if(iv != null){
			try{
				c.setClickedNPC();
				iv.invokeFunction("end", mode, type, selection);
			}catch(final Throwable t){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, "Quest: " + getQM(c).getQuest());
				dispose(c);
			}
		}
	}

	public void dispose(QuestActionManager qm, MapleClient c){
		qms.remove(c.getAccID());
		scripts.remove(c.getAccID());
		resetContext("quest/" + qm.getQuest() + ".js", c);
		resetContext("quest/" + c.getWorld() + "/" + qm.getQuest() + ".js", c);
		MapleQuest quest = MapleQuest.getInstance(qm.getQuest());
		if(quest.startQuestData.script != null){
			resetContext("quest/" + quest.startQuestData.script + ".js", c);
			resetContext("quest/" + c.getWorld() + "/" + quest.startQuestData.script + ".js", c);
		}
		if(quest.completeQuestData.script != null){
			resetContext("quest/" + quest.completeQuestData.script + ".js", c);
			resetContext("quest/" + c.getWorld() + "/" + quest.completeQuestData.script + ".js", c);
		}
	}

	public void dispose(MapleClient c){
		QuestActionManager qm = getQM(c);
		if(qm != null){
			dispose(qm, c);
		}
	}

	public QuestActionManager getQM(MapleClient c){
		return qms.get(c.getAccID());
	}
}
