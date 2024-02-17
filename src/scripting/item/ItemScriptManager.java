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
package scripting.item;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.*;

import client.MapleClient;
import client.MessageType;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public class ItemScriptManager{

	private static ItemScriptManager instance = new ItemScriptManager();
	private Map<String, Invocable> scripts = new HashMap<>();
	private ScriptEngineFactory sef;

	private ItemScriptManager(){
		ScriptEngineManager sem = new ScriptEngineManager();
		sef = sem.getEngineByName("javascript").getFactory();
	}

	public static ItemScriptManager getInstance(){
		return instance;
	}

	public boolean scriptExists(String scriptName){
		File scriptFile = new File("scripts/item/" + scriptName + ".js");
		return scriptFile.exists();
	}

	public void clearScripts(){
		scripts.clear();
	}

	public void getItemScript(MapleClient c, String scriptName){
		if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Item Script: " + scriptName);
		if(scripts.containsKey(scriptName)){
			try{
				scripts.get(scriptName).invokeFunction("start", new ItemScriptMethods(c));
			}catch(ScriptException | NoSuchMethodException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Item Script: " + scriptName);
			}
			return;
		}
		File scriptFile = new File("scripts/item/" + scriptName + ".js");
		if(!scriptFile.exists()){
			c.announce(CWvsContext.enableActions());
			return;
		}
		ScriptEngine portal = sef.getScriptEngine();
		try(FileReader fr = new FileReader(scriptFile)){
			CompiledScript compiled = ((Compilable) portal).compile(fr);
			compiled.eval();
			final Invocable script = ((Invocable) portal);
			scripts.put(scriptName, script);
			script.invokeFunction("start", new ItemScriptMethods(c));
		}catch(final Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Item Script: " + scriptName);
		}
	}
}