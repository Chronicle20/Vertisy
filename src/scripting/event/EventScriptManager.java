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
package scripting.event;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import net.server.channel.Channel;
import scripting.AbstractScriptManager;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Matze
 */
public class EventScriptManager extends AbstractScriptManager{

	public class EventEntry{

		public EventEntry(ScriptEngine se, EventManager em){
			this.se = se;
			this.em = em;
		}

		public ScriptEngine se;
		public EventManager em;
	}

	private Map<String, EventEntry> events = new LinkedHashMap<>();

	public EventScriptManager(){
		super();
	}

	public void load(Channel cserv, String[] scripts){
		for(String script : scripts){
			if(!script.equals("")){
				// long start = System.currentTimeMillis();
				ScriptEngine se = getInvocable("event/" + script + ".js", null);
				events.put(script, new EventEntry(se, new EventManager(cserv, se, script)));
				// System.out.println(script + " took " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");
			}
		}
	}

	public EventManager getEventManager(String event){
		EventEntry entry = events.get(event);
		if(entry == null) return null;
		return entry.em;
	}

	public Collection<EventEntry> getEventEntryList(){
		return events.values();
	}

	public void init(){
		for(EventEntry entry : events.values()){
			try{
				entry.se.put("em", entry.em);
				((Invocable) entry.se).invokeFunction("init", (Object) null);
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + entry.em.getName() + ", methodName: init");
			}
		}
	}

	public void reload(){
		cancel();
		init();
	}

	public void cancel(){
		for(EventEntry entry : events.values()){
			entry.em.cancel();
		}
	}
}