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
package scripting;

import java.io.File;
import java.io.FileReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import client.MapleClient;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Matze
 */
public abstract class AbstractScriptManager{

	private ScriptEngineManager sem;

	protected AbstractScriptManager(){
		sem = new ScriptEngineManager();
	}

	protected ScriptEngine getInvocable(String path, MapleClient c){
		path = "scripts/" + path;
		File scriptFile = new File(path);
		if(!scriptFile.exists()) return null;
		ScriptEngine engine = null;
		if(c != null) engine = c.getScriptEngine(path);
		if(engine == null){
			engine = sem.getEngineByName("nashorn");
			if(c != null) c.setScriptEngine(path, engine);
			try(FileReader fr = new FileReader(scriptFile)){
				engine.eval(fr);
			}catch(Exception t){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, t, path.substring(12, path.length()) + "\r\n" + path);
				return null;
			}
		}
		return engine;
	}

	protected boolean scriptExist(String path){
		File scriptFile = new File(path);
		return scriptFile.exists();
	}

	protected void resetContext(String path, MapleClient c){
		c.removeScriptEngine("scripts/" + path);
	}
}
