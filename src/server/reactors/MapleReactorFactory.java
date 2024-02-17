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
package server.reactors;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import constants.ServerConstants;
import provider.*;
import server.reactors.actions.BreakAction;
import server.reactors.actions.MapleReactorEvent;
import server.reactors.actions.TimeOutAction;
import tools.ObjectParser;
import tools.StringUtil;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class MapleReactorFactory{

	private static MapleDataProvider data = null;
	private static Map<Integer, MapleReactor> reactorTemplates = new HashMap<>();

	public static MapleReactor getReactor(int rid){
		MapleReactor reactor = reactorTemplates.get(rid);
		if(reactor == null){
			if(ServerConstants.WZ_LOADING){
				if(data == null) data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Reactor.wz"));
				MapleData reactorData = data.getData(StringUtil.getLeftPaddedStr(rid + ".img", '0', 11));
				// String action = MapleDataTool.getString("action", reactorData);
				MapleData link = reactorData.getChildByPath("info/link");
				reactor = loadReactor(reactorData, rid);
				if(link != null){
					int linkID = MapleDataTool.getIntConvert(link);
					MapleReactor reactorLinked = reactorTemplates.get(linkID);
					if(reactorLinked == null){
						reactorLinked = loadReactor(linkID);
						if(ServerConstants.BIN_DUMPING){
							File bin = new File(System.getProperty("wzpath") + "/bin/Reactors/" + linkID + ".bin");
							if(!bin.exists()){
								try{
									bin.getParentFile().mkdirs();
									bin.createNewFile();
									MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
									reactorLinked.save(mplew);
									mplew.saveToFile(bin);
									mplew = null;
								}catch(Exception ex){
									Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
								}
							}
						}
						reactorTemplates.put(linkID, reactorLinked);
					}
					reactor.setLink(reactorLinked);
				}
				if(ServerConstants.BIN_DUMPING){
					File bin = new File(System.getProperty("wzpath") + "/bin/Reactors/" + rid + ".bin");
					if(!bin.exists()){
						try{
							bin.getParentFile().mkdirs();
							bin.createNewFile();
							MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
							reactor.save(mplew);
							mplew.saveToFile(bin);
							mplew = null;
						}catch(Exception ex){
							Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
						}
					}
				}
				reactorTemplates.put(rid, reactor);
			}else{
				try{
					File bin = new File(System.getProperty("wzpath") + "/bin/Reactors/" + rid + ".bin");
					if(bin.exists()){
						reactor = new MapleReactor(rid);
						byte[] in = Files.readAllBytes(bin.toPath());
						ByteArrayByteStream babs = new ByteArrayByteStream(in);
						GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
						reactor.load(glea);
						glea = null;
						babs = null;
						in = null;
						reactorTemplates.put(rid, reactor);
					}
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
		}
		return reactor;
	}

	private static final MapleReactor loadReactor(int rid){
		return loadReactor(data.getData(StringUtil.getLeftPaddedStr(rid + ".img", '0', 11)), rid);
	}

	private static final MapleReactor loadReactor(MapleData reactorData, int rid){
		MapleReactor reactor = new MapleReactor(rid);
		for(MapleData stateData : reactorData.getChildren()){
			if(stateData.getName().equals("action")){
				reactor.setAction(MapleDataTool.getString(stateData));
				continue;
			}
			if(stateData.getName().equals("quest")){
				reactor.setQuest(MapleDataTool.getInt(stateData));
				continue;
			}
			if(stateData.getName().equals("info")){
				continue;
			}
			ReactorState state = new ReactorState();
			Integer stateId = ObjectParser.isInt(stateData.getName());
			if(stateId != null) reactor.addState(stateId, state);
			state.setCanRepeat(MapleDataTool.getInt("repeat", stateData, 0) == 1);
			MapleData events = stateData.getChildByPath("event");
			if(events == null){
				state.addEvent(new BreakAction());
				continue;
			}
			for(MapleData eventData : events.getChildren()){
				if(eventData.getName().equalsIgnoreCase("timeOut")) continue;
				int typeAsInt = MapleDataTool.getInt("type", eventData);
				ReactorActionType type = ReactorActionType.valueOf(typeAsInt);
				try{
					if(type != null){
						MapleReactorEvent event = (MapleReactorEvent) type.getClassType().getConstructor(MapleData.class).newInstance(eventData);
						state.addEvent(event);
						if(type == ReactorActionType.TIME_OUT){
							TimeOutAction timeOutEvent = (TimeOutAction) event;
							timeOutEvent.setTimeout(MapleDataTool.getInt(events.getChildByPath("timeOut"), 0));
						}
					}/*else{
					 if(typeAsInt == 13) System.out.println("Reactor: " + rid + " Event - " + eventData.getName() + ": " + typeAsInt);
					 }*/
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
		}
		return reactor;
	}

	public static void reloadReactors(){
		reactorTemplates.clear();
	}

	public static void loadAllReactors(){
		if(data == null) data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Reactor.wz"));
		for(MapleDataFileEntry mdfe : data.getRoot().getFiles()){
			getReactor(Integer.parseInt(mdfe.getName().substring(0, mdfe.getName().length() - 4)));
		}
		//
	}
}