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
package scripting.npc;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import scripting.AbstractScriptManager;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

/**
 * @author Matze
 */
public class NPCScriptManager extends AbstractScriptManager{

	private Map<Integer, NPCConversationManager> cms = new HashMap<>();
	private Map<Integer, Invocable> scripts = new HashMap<>();
	private static NPCScriptManager instance = new NPCScriptManager();

	public synchronized static NPCScriptManager getInstance(){
		return instance;
	}

	public void start(MapleClient c, int npc, MapleCharacter chr){
		start(c, npc, null, chr);
	}

	public void start(MapleClient c, int npc, String fileName, MapleCharacter chr){
		start(c, npc, fileName, "start", chr);
	}

	public void start(MapleClient c, int npc, String fileName, String startFunction, Object... args){
		try{
			if(c == null || c.getPlayer() == null){
				c.disconnectFully();
			}
			NPCConversationManager cm = new NPCConversationManager(c, npc, fileName);
			if(cms.containsKey(c.getAccID())){
				dispose(c);
			}
			if(c.canClickNPC()){
				String info = "";
				cms.put(c.getAccID(), cm);
				ScriptEngine se = null;
				if(fileName != null){
					se = getInvocable("npc/world" + c.getWorld() + "/" + fileName + ".js", c);
					if(se == null) se = getInvocable("npc/" + fileName + ".js", c);
					if(se != null) info += fileName;
				}
				if(se == null){
					se = getInvocable("npc/world" + c.getWorld() + "/" + npc + ".js", c);
					if(se != null) info += npc;
				}
				if(se == null){
					se = getInvocable("npc/" + npc + ".js", c);
					if(se != null) info += npc;
				}
				if(se == null){
					info += npc;
					if(fileName != null) info += ", " + fileName;
					Logger.log(LogType.INFO, LogFile.UNCODED, null, "NPC %d, %s is not coded. Player: %s", npc, fileName, c.getPlayer().getName());
				}
				if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "NPC Script: " + info);
				if(se == null || NPCScriptManager.getInstance() == null){
					dispose(c);
					return;
				}
				se.put("cm", cm);
				Invocable iv = (Invocable) se;
				scripts.put(c.getAccID(), iv);
				c.setClickedNPC();
				try{
					if(args != null && args.length > 0) iv.invokeFunction(startFunction, args);
					else iv.invokeFunction(startFunction);
				}catch(final NoSuchMethodException nsme){
					try{
						iv.invokeFunction(startFunction, args);
					}catch(final NoSuchMethodException nsma){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, nsma, "Unable to " + startFunction + " NPC script: " + npc + " - " + fileName);
					}
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Unable to " + startFunction + " NPC script: " + npc + " - " + fileName);
				}
			}else{
				c.announce(CWvsContext.enableActions());
			}
		}catch(final UndeclaredThrowableException | ScriptException ute){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ute, "NPC: " + npc + " FileName: " + fileName + " Account: " + c.getAccountName());
			dispose(c);
		}catch(final Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "NPC: " + npc + " FileName: " + fileName + " Account: " + c.getAccountName());
			dispose(c);
		}
	}

	public boolean scriptExist(int npc, MapleClient c){
		if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "NPC: " + npc);
		return this.scriptExist("scripts/npc/world" + c.getWorld() + "/" + npc + ".js") || scriptExist("scripts/npc/" + npc + ".js");
	}

	public void action(MapleClient c, byte mode, byte type, int selection){
		Invocable iv = scripts.get(c.getAccID());
		if(iv != null){
			try{
				c.setClickedNPC();
				iv.invokeFunction("action", mode, type, selection);
			}catch(ScriptException | NoSuchMethodException t){
				if(getCM(c) != null){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, "NPC: " + getCM(c).getNpc());
				}
				dispose(c);
			}
		}
	}

	public void dispose(NPCConversationManager cm){
		MapleClient c = cm.getClient();
		cms.remove(c.getAccID());
		scripts.remove(c.getAccID());
		if(cm.getScriptName() != null){
			resetContext("npc/world" + c.getWorld() + "/" + cm.getScriptName() + ".js", c);
			resetContext("npc/" + cm.getScriptName() + ".js", c);
		}else{
			resetContext("npc/world" + c.getWorld() + "/" + cm.getNpc() + ".js", c);
			resetContext("npc/" + cm.getNpc() + ".js", c);
		}
	}

	public void dispose(MapleClient c){
		if(cms.get(c.getAccID()) != null){
			dispose(cms.get(c.getAccID()));
		}
	}

	public NPCConversationManager getCM(MapleClient c){
		return cms.get(c.getAccID());
	}
}
