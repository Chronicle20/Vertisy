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
package server.quest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.MapleQuestStatus.Status;
import client.MessageType;
import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.quest.actions.InfoAction;
import server.quest.actions.MapleQuestAction;
import server.quest.requirements.InfoExRequirement;
import server.quest.requirements.ItemRequirement;
import server.quest.requirements.MapleQuestRequirement;
import server.quest.requirements.MobRequirement;
import tools.MaplePacketCreator;
import tools.ObjectParser;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Matze
 */
public class MapleQuest{

	private static Map<Integer, MapleQuest> quests = new HashMap<>();
	protected short id;
	protected String questName, parentQuest;
	protected int timeLimit, timeLimit2;
	protected String infoex;
	private boolean autoStart;
	private boolean autoPreComplete, autoComplete;
	private boolean repeatable = false;
	public StartQuestData startQuestData = new StartQuestData();
	public CompleteQuestData completeQuestData = new CompleteQuestData();
	private static MapleDataProvider questData = null;
	private static MapleData questInfo;
	private static MapleData questAct;
	private static MapleData questReq;
	private static File base = new File("./wz/bin/Quests/");

	private MapleQuest(int id){
		this.id = (short) id;
		File binFile = new File(base, id + ".bin");
		if(binFile.exists() && !ServerConstants.WZ_LOADING){
			try{
				byte[] in = Files.readAllBytes(binFile.toPath());
				ByteArrayByteStream babs = new ByteArrayByteStream(in);
				GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
				timeLimit = glea.readInt();
				timeLimit2 = glea.readInt();
				autoStart = glea.readBoolean();
				autoPreComplete = glea.readBoolean();
				autoComplete = glea.readBoolean();
				//
				repeatable = glea.readBoolean();
				//
				startQuestData.decode(this, glea);
				completeQuestData.decode(this, glea);
			}catch(IOException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}else if(ServerConstants.WZ_LOADING){
			if(questInfo != null){
				timeLimit = MapleDataTool.getInt(id + "/timeLimit", questInfo, 0);
				timeLimit2 = MapleDataTool.getInt(id + "/timeLimit2", questInfo, 0);
				autoStart = MapleDataTool.getInt(id + "/autoStart", questInfo, 0) == 1;
				autoPreComplete = MapleDataTool.getInt(id + "/autoPreComplete", questInfo, 0) == 1;
				autoComplete = MapleDataTool.getInt(id + "/autoComplete", questInfo, 0) == 1;
				// questName = MapleDataTool.getString(id + "/name", questInfo);
				// parentQuest = MapleDataTool.getString(id + "/parent", questInfo);
			}
			MapleData reqData = questReq.getChildByPath(String.valueOf(id));
			if(reqData == null) return;// most likely infoEx
			MapleData actData = questAct.getChildByPath(String.valueOf(id));
			if(actData == null) return;
			MapleData startReqData = reqData.getChildByPath("0");
			MapleData startActData = actData.getChildByPath("0");
			startQuestData.read(this, startReqData, startActData);
			//
			MapleData completeReqData = reqData.getChildByPath("1");
			MapleData completeActData = actData.getChildByPath("1");
			completeQuestData.read(this, completeReqData, completeActData);
			try{
				if(ServerConstants.BIN_DUMPING){
					binFile.createNewFile();
					MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
					mplew.writeInt(timeLimit);
					mplew.writeInt(timeLimit2);
					mplew.writeBoolean(autoStart);
					mplew.writeBoolean(autoPreComplete);
					mplew.writeBoolean(autoComplete);
					// this is after startReq/ completeReq
					mplew.writeBoolean(repeatable);
					//
					startQuestData.encode(mplew);
					completeQuestData.encode(mplew);
					mplew.saveToFile(binFile);
				}
			}catch(IOException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}

	public String getQuestName(){
		return questName;
	}

	public String getParentQuest(){
		return parentQuest;
	}

	public boolean isAutoComplete(){
		return autoPreComplete || autoComplete;
	}

	public boolean isAutoStart(){
		return autoStart;
	}

	public static MapleQuest getInstance(int id){
		MapleQuest ret = quests.get(id);
		if(ServerConstants.WZ_LOADING && (questInfo == null || questReq == null || questAct == null)){
			questInfo = questData.getData("QuestInfo.img");
			questReq = questData.getData("Check.img");
			questAct = questData.getData("Act.img");
		}
		if(ret == null){
			ret = new MapleQuest(id);
			quests.put(id, ret);
		}
		return ret;
	}

	public boolean canStart(MapleCharacter c, int npcid){
		if(c.getQuest(this).getStatus() != Status.NOT_STARTED && !(c.getQuest(this).getStatus() == Status.COMPLETED && repeatable)) return false;
		for(MapleQuestRequirement r : startQuestData.startReqs.values()){
			if(!r.check(c, npcid)){
				if(c.getScriptDebug()) c.dropMessage(MessageType.MAPLETIP, "Failed requirement: " + r.toString());
				return false;
			}
		}
		return true;
	}

	public boolean canComplete(MapleCharacter c, Integer npcid){
		if(!c.getQuest(this).getStatus().equals(Status.STARTED)) return false;
		for(MapleQuestRequirement r : completeQuestData.completeReqs.values()){
			if(r == null || !r.check(c, npcid)) return false;
		}
		return true;
	}

	public void start(MapleCharacter c, int npc){
		if(autoStart || canStart(c, npc)){
			for(MapleQuestAction a : startQuestData.startActs.values()){
				if(!a.check(c, null)){ // would null be good ?
					return;
				}
			}
			forceStart(c, npc);
			for(MapleQuestAction a : startQuestData.startActs.values()){
				a.run(c, null);
			}
		}else{
			if(c.getScriptDebug()) c.dropMessage(MessageType.MAPLETIP, "Quest start(failed) ID: " + this.id + ", NPC: " + npc);
		}
	}

	public void complete(MapleCharacter c, int npc){
		complete(c, npc, null);
	}

	public void complete(MapleCharacter c, int npc, Integer selection){
		if(autoPreComplete || canComplete(c, npc)){
			for(MapleQuestAction a : completeQuestData.completeActs.values()){
				if(!a.check(c, selection)) return;
			}
			forceComplete(c, npc);
			for(MapleQuestAction a : completeQuestData.completeActs.values()){
				a.run(c, selection);
			}
		}else{
			if(c.getScriptDebug()) c.dropMessage(MessageType.MAPLETIP, "Quest start(failed) ID: " + this.id + ", NPC: " + npc);
		}
	}

	public void reset(MapleCharacter c){
		c.updateQuest(new MapleQuestStatus(this, MapleQuestStatus.Status.NOT_STARTED));
	}

	public void forfeit(MapleCharacter c){
		if(!c.getQuest(this).getStatus().equals(Status.STARTED)) return;
		if(timeLimit > 0){
			c.announce(MaplePacketCreator.removeQuestTimeLimit(id));
		}
		MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.NOT_STARTED);
		newStatus.setForfeited(c.getQuest(this).getForfeited() + 1);
		c.updateQuest(newStatus);
	}

	public boolean forceStart(MapleCharacter c, int npc){
		if(c.getScriptDebug()) c.dropMessage(MessageType.MAPLETIP, "Quest Force Start ID: " + this.id + ", NPC: " + npc);
		MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.STARTED, npc);
		newStatus.setForfeited(c.getQuest(this).getForfeited());
		if(startQuestData.startActs.containsKey(MapleQuestActionType.INFO)){
			newStatus.setInfo(((InfoAction) startQuestData.startActs.get(MapleQuestActionType.INFO)).getInfoValue());
		}
		if(timeLimit > 0){
			c.questTimeLimit(this, 30000);// timeLimit * 1000
		}
		if(timeLimit2 > 0){// =\
		}
		c.updateQuest(newStatus);
		Logger.log(LogType.INFO, LogFile.QUESTS, c.getClient().getAccountName() + ".txt", c.getName() + " Started quest " + id + " using npc " + npc);
		return true;
	}

	public boolean forceComplete(MapleCharacter c, int npc){
		return forceComplete(c, npc, true);
	}

	public boolean forceComplete(MapleCharacter c, int npc, boolean sendCompletePacket){
		MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.COMPLETED, npc);
		newStatus.setForfeited(c.getQuest(this).getForfeited());
		newStatus.setCompletionTime(System.currentTimeMillis());
		c.updateQuest(newStatus);
		if(sendCompletePacket) c.announce(MaplePacketCreator.getShowQuestCompletion(getId()));
		Logger.log(LogType.INFO, LogFile.QUESTS, c.getClient().getAccountName() + ".txt", c.getName() + " Finished quest " + id + " using npc: " + npc);
		return true;
	}

	public short getId(){
		return id;
	}

	public List<Integer> getRelevantMobs(){
		return completeQuestData.relevantMobs;
	}

	public int getItemAmountNeeded(int itemid){
		MapleQuestRequirement req = completeQuestData.completeReqs.get(MapleQuestRequirementType.ITEM);
		if(req == null) return 0;
		ItemRequirement ireq = (ItemRequirement) req;
		return ireq.getItemAmountNeeded(itemid);
	}

	public int getMobAmountNeeded(int mid){
		MapleQuestRequirement req = completeQuestData.completeReqs.get(MapleQuestRequirementType.MOB);
		if(req == null) return 0;
		MobRequirement mreq = (MobRequirement) req;
		return mreq.getRequiredMobCount(mid);
	}

	/*public short getInfoNumber(){
		return infoNumber;
	}*/
	public String getInfoEx(){
		MapleQuestRequirement req = startQuestData.startReqs.get(MapleQuestRequirementType.INFO_EX);
		String ret = "";
		if(req != null){
			InfoExRequirement ireq = (InfoExRequirement) req;
			ret = ireq.getFirstInfo();
		}else{ // Check complete requirements.
			req = completeQuestData.completeReqs.get(MapleQuestRequirementType.INFO_EX);
			if(req != null){
				InfoExRequirement ireq = (InfoExRequirement) req;
				ret = ireq.getFirstInfo();
			}
		}
		return ret;
	}

	public int getTimeLimit(){
		return timeLimit;
	}

	public static void clearCache(int quest){
		if(quests.containsKey(quest)){
			quests.remove(quest);
		}
	}

	public static void clearCache(){
		quests.clear();
	}

	/**
	 * Gets the {@link MapleQuestRequirement} based on the {@link MapleQuestRequirementType}
	 * provided. Also initializes the action from the data provided.
	 * 
	 * @param type The {@link MapleQuestReqirementType} to create a class from.
	 * @param data The {@link MapleData} used to initialize the class.
	 * @return {@link MapleQuestRequirement} The initialized class for the requirement.
	 */
	public MapleQuestRequirement getRequirement(MapleQuestRequirementType type, MapleData data){
		try{
			if(type != null && type.getClassType() != null){
				MapleQuestRequirement req = type.getClassType().getConstructor(MapleQuest.class, MapleData.class).newInstance(this, data);
				return req;
			}else{
				return null;
			}
		}catch(NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex){
			Logger.log(LogType.ERROR, LogFile.QUESTS, id + ".txt", ex, null);
		}
		return null;
	}

	public MapleQuestRequirement getRequirement(MapleQuestRequirementType type, LittleEndianAccessor lea){
		try{
			if(type != null && type.getClassType() != null){
				MapleQuestRequirement req = type.getClassType().getConstructor(MapleQuest.class, LittleEndianAccessor.class).newInstance(this, lea);
				return req;
			}else{
				return null;
			}
		}catch(NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex){
			Logger.log(LogType.ERROR, LogFile.QUESTS, id + ".txt", ex, null);
		}
		return null;
	}

	/**
	 * Gets a {@link MapleQuestAction} based on the {@link MapleQuestActionType}
	 * provided. Also initializes the action from the data provided.
	 * 
	 * @param type The {@link MapleQuestActionType} to create.
	 * @param data The {@link MapleData} to initialize the quest with.
	 * @return MapleQuestAction
	 */
	public MapleQuestAction getAction(MapleQuestActionType type, MapleData data){
		try{
			if(type != null && type.getClassType() != null){
				MapleQuestAction req = type.getClassType().getConstructor(MapleQuest.class, MapleData.class).newInstance(this, data);
				return req;
			}else{
				return null;
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.QUESTS, id + ".txt", ex, null);
		}
		return null;
	}

	public MapleQuestAction getAction(MapleQuestActionType type, LittleEndianAccessor lea){
		try{
			if(type != null && type.getClassType() != null){
				MapleQuestAction req = type.getClassType().getConstructor(MapleQuest.class, LittleEndianAccessor.class).newInstance(this, lea);
				return req;
			}else{
				return null;
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.QUESTS, id + ".txt", ex, null);
		}
		return null;
	}

	public static void loadAllQuest(){
		base.mkdirs();
		if(ServerConstants.WZ_LOADING){
			questData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Quest.wz"));
			questInfo = questData.getData("QuestInfo.img");
			questReq = questData.getData("Check.img");
			questAct = questData.getData("Act.img");
			try{
				for(MapleData questData : questInfo.getChildren()){
					int questID = Integer.parseInt(questData.getName());
					quests.put(questID, new MapleQuest(questID));
				}
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}else{
			try{
				for(File f : base.listFiles()){
					String q = f.getName().replace(".bin", "");
					Integer questID = ObjectParser.isInt(q);
					if(questID == null){
						System.out.println("Invalid questid: " + q);
						continue;
					}
					quests.put(questID.intValue(), new MapleQuest(questID.intValue()));
				}
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
		System.out.println("Loaded " + quests.size() + " quests.");
	}
}
