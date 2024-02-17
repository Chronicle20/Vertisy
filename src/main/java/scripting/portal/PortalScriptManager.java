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
package scripting.portal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import javax.script.*;

import client.MapleClient;
import client.MessageType;
import server.MaplePortal;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class PortalScriptManager{

	private static PortalScriptManager instance = new PortalScriptManager();
	private Map<String, PortalScript> scripts = new HashMap<>();
	private ScriptEngineFactory sef;

	private PortalScriptManager(){
		ScriptEngineManager sem = new ScriptEngineManager();
		sef = sem.getEngineByName("javascript").getFactory();
	}

	public static PortalScriptManager getInstance(){
		return instance;
	}

	private PortalScript getPortalScript(MapleClient c, String scriptName){
		if(scripts.containsKey(scriptName)) return scripts.get(scriptName);
		File scriptFile = new File("scripts/portal/" + scriptName + ".js");
		if(!scriptFile.exists()){
			Logger.log(LogType.INFO, LogFile.UNCODED, null, "Portal script %s in map %s does not exist. Player: %s", scriptName, c.getPlayer().getMapId(), c.getPlayer().getName());
			scripts.put(scriptName, null);
			return null;
		}
		ScriptEngine portal = sef.getScriptEngine();
		try(FileReader fr = new FileReader(scriptFile)){
			((Compilable) portal).compile(fr).eval();
		}catch(ScriptException | IOException | UndeclaredThrowableException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Broken Portal: " + scriptName);
		}
		PortalScript script = ((Invocable) portal).getInterface(PortalScript.class);
		scripts.put(scriptName, script);
		return script;
	}

	public boolean executePortalScript(MaplePortal portal, MapleClient c){
		try{
			if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Portal Script: " + portal.getScriptName());
			PortalScript script = getPortalScript(c, portal.getScriptName());
			if(script != null) return script.enter(new PortalPlayerInteraction(c, portal));
		}catch(UndeclaredThrowableException ute){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ute, "Broken Portal: " + portal.getScriptName());
		}catch(final Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Broken Portal: " + portal.getScriptName());
		}
		return false;
	}

	public void reloadPortalScripts(){
		scripts.clear();
	}
}