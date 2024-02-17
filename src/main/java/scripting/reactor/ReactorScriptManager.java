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
package scripting.reactor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import client.MapleClient;
import client.MessageType;
import scripting.AbstractScriptManager;
import server.maps.ReactorDropEntry;
import server.reactors.MapleReactor;
import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Lerk
 */
public class ReactorScriptManager extends AbstractScriptManager{

	private static ReactorScriptManager instance = new ReactorScriptManager();
	private Map<Integer, List<ReactorDropEntry>> drops = new HashMap<>();
	private Map<String, List<ReactorDropEntry>> dropsByAction = new HashMap<>();

	public synchronized static ReactorScriptManager getInstance(){
		return instance;
	}

	public void act(MapleClient c, MapleReactor reactor){
		try{
			if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Reactor ID: " + reactor.getId() + " Action: " + reactor.getAction() + " Link: " + reactor.getLink());
			ReactorActionManager rm = new ReactorActionManager(c, reactor);
			ScriptEngine se = null;
			if(reactor.getAction() != null) se = getInvocable("reactor/" + reactor.getAction() + ".js", c);// can it be null?
			if(se == null) se = getInvocable("reactor/" + reactor.getId() + ".js", c);
			if(se == null){
				if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Uncoded act Reactor ID: " + reactor.getId() + " Action: " + reactor.getAction() + " Link: " + reactor.getLink());
				Logger.log(LogType.INFO, LogFile.UNCODED, null, "Uncoded act reactor %s with action %s. Player: %s", reactor.getId(), reactor.getAction(), c.getPlayer().getName());
				return;
			}
			se.put("rm", rm);
			((Invocable) se).invokeFunction("act");
		}catch(final ScriptException | NoSuchMethodException | NullPointerException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Reactor ID: " + reactor.getId() + " Action: " + reactor.getAction() + " Link: " + reactor.getLink());
		}
	}

	public List<ReactorDropEntry> getDrops(String action){
		List<ReactorDropEntry> ret = dropsByAction.get(action);
		if(ret == null){
			ret = new LinkedList<>();
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT itemid, chance, questid FROM reactordrops WHERE reactorAction = ? AND chance >= 0")){
				ps.setString(1, action);
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()){
						ret.add(new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid")));
					}
				}
			}catch(Throwable e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Reactor: " + action);
			}
			dropsByAction.put(action, ret);
		}
		return ret;
	}

	public List<ReactorDropEntry> getDrops(int rid){
		List<ReactorDropEntry> ret = drops.get(rid);
		if(ret == null){
			ret = new LinkedList<>();
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT itemid, chance, questid FROM reactordrops WHERE reactorid = ? AND chance >= 0")){
				ps.setInt(1, rid);
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()){
						ret.add(new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid")));
					}
				}
			}catch(Throwable e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Reactor: " + rid);
			}
			drops.put(rid, ret);
		}
		return ret;
	}

	public void clearDrops(){
		drops.clear();
		dropsByAction.clear();
	}

	public void touch(MapleClient c, MapleReactor reactor){
		touching(c, reactor, true);
	}

	public void untouch(MapleClient c, MapleReactor reactor){
		touching(c, reactor, false);
	}

	public void touching(MapleClient c, MapleReactor reactor, boolean touching){
		try{
			ReactorActionManager rm = new ReactorActionManager(c, reactor);
			ScriptEngine se = null;
			if(reactor.getAction() != null) se = getInvocable("reactor/" + reactor.getAction() + ".js", c);// can it be null?
			if(se == null) se = getInvocable("reactor/" + reactor.getId() + ".js", c);
			if(se == null){
				Logger.log(LogType.INFO, LogFile.UNCODED, null, "Uncoded touch reactor %s. Player: %s", reactor.getId(), c.getPlayer().getName());
				return;
			}
			se.put("rm", rm);
			if(touching){
				((Invocable) se).invokeFunction("touch");
			}else{
				((Invocable) se).invokeFunction("untouch");
			}
		}catch(final ScriptException | NoSuchMethodException | NullPointerException ute){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ute, "Reactor: " + reactor.getId());
		}
	}

	@Override
	protected void resetContext(String path, MapleClient c){
		super.resetContext("reactor/" + path, c);
	}
}
