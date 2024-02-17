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
package scripting.map;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import javax.script.*;

import client.MapleClient;
import client.MessageType;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class MapScriptManager{

	private static MapScriptManager instance = new MapScriptManager();
	private Map<String, Invocable> scripts = new HashMap<>();
	private ScriptEngineFactory sef;

	private MapScriptManager(){
		ScriptEngineManager sem = new ScriptEngineManager();
		sef = sem.getEngineByName("javascript").getFactory();
	}

	public static MapScriptManager getInstance(){
		return instance;
	}

	public void reloadScripts(){
		scripts.clear();
	}

	public boolean scriptExists(String scriptName, boolean firstUser){
		File scriptFile = new File("scripts/map/" + (firstUser ? "onFirstUserEnter/" : "onUserEnter/") + scriptName + ".js");
		return scriptFile.exists();
	}

	public void getMapScript(MapleClient c, String scriptName, boolean firstUser){
		if(c == null || c.getPlayer() == null) return;
		if(scripts.containsKey(scriptName)){
			try{
				if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Map Script: " + scriptName);
				scripts.get(scriptName).invokeFunction("start", new MapScriptMethods(c));
			}catch(final ScriptException | NoSuchMethodException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
			return;
		}
		String type = firstUser ? "onFirstUserEnter/" : "onUserEnter/";
		File scriptFile = new File("scripts/map/" + type + scriptName + ".js");
		if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Map Script: " + scriptName + ", Type: " + type);
		if(!scriptExists(scriptName, firstUser)) return;
		ScriptEngine portal = sef.getScriptEngine();
		try(FileReader fr = new FileReader(scriptFile)){
			CompiledScript compiled = ((Compilable) portal).compile(fr);
			compiled.eval();
			final Invocable script = ((Invocable) portal);
			scripts.put(scriptName, script);
			script.invokeFunction("start", new MapScriptMethods(c));
		}catch(final UndeclaredThrowableException | ScriptException ute){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ute, "Map Script: " + scriptName);
		}catch(final Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Map Script: " + scriptName);
		}
	}
}