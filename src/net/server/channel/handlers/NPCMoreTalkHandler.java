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
package net.server.channel.handlers;

import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.npc.NpcTalkData;
import scripting.npc.ScriptMessageType;
import scripting.quest.QuestScriptManager;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Matze
 */
public final class NPCMoreTalkHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		try{
			NPCConversationManager manager = c.getQM() != null ? c.getQM() : c.getCM();
			byte lastMsg = slea.readByte(); // 00 (last msg type I think)
			if(lastMsg == 6){
				String text = slea.readMapleAsciiString();
				if(manager != null){
					manager.setGetText(text);
					NPCScriptManager.getInstance().action(c, (byte) 1, lastMsg, -1);
				}
			}else if(lastMsg == 3){
				byte action = slea.readByte(); // 00 = end chat, 01 == follow
				if(action != 0){
					String returnText = slea.readMapleAsciiString();
					if(c.getQM() != null){
						c.getQM().setGetText(returnText);
						if(c.getQM().isStart()){
							QuestScriptManager.getInstance().start(c, action, lastMsg, -1);
						}else{
							QuestScriptManager.getInstance().end(c, action, lastMsg, -1);
						}
					}else{
						manager.setGetText(returnText);
						NPCScriptManager.getInstance().action(c, action, lastMsg, -1);
					}
				}else if(c.getQM() != null){
					c.getPlayer().dispose();
				}else{
					c.getPlayer().dispose();
				}
			}else{
				byte mode = slea.readByte(); // 00 = end chat, 01 == follow
				int sel = 0;
				if(slea.available() >= 4){
					sel = slea.readInt();
				}else if(slea.available() > 0){
					sel = slea.readByte();
				}
				int selection = -1;
				String data = "";
				if(c.getQM() != null){
					data += "Quest: " + c.getQM().getQuest();
				}else if(c.getCM() != null){
					data += "NPC: " + c.getCM().getNpc() + "(" + c.getCM().getScriptName() + ") Text: " + c.getCM().getText();
				}
				if(mode == -1){
					c.getPlayer().dispose();
					return;
				}
				if(manager != null){
					NpcTalkData talkData = manager.getTalkData();
					if(talkData != null){
						ScriptMessageType messageType = talkData.messageType;
						if(messageType != null){
							if(messageType.getMsgType() != lastMsg){
								AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Last message type doesn't match. Sent: " + ScriptMessageType.getType(lastMsg) + " Expected: " + messageType + " Npc: " + c.getCM().getNpc());
							}
							if(mode == 1){
								if(!talkData.next && messageType.equals(ScriptMessageType.Say)){
									c.getPlayer().dispose();
									return;
								}
							}else if(mode == 0 && messageType.equals(ScriptMessageType.AskMenu)){
								if(manager.getScriptName() != null && manager.getScriptName().equals("ironman")){
									if(c.getPlayer().getHardMode() == 0 && c.getPlayer().getIronMan() == 0){
										c.getPlayer().setHardMode(-1);
										c.getPlayer().setIronMan(-1);
									}
								}
								c.getPlayer().dispose();
								return;
							}
						}
						if(talkData.max != 0 && sel > talkData.max){
							AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Invalid selection max: " + talkData.max + " input: " + sel + " Npc: " + c.getCM().getNpc());
							sel = talkData.max;
						}
						if(sel < talkData.min){
							AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Invalid selection min: " + talkData.min + " input: " + sel + " Npc: " + c.getCM().getNpc());
							sel = talkData.min;
						}
						if(!talkData.validSelections.isEmpty()){
							if(!talkData.validSelections.contains(sel)){
								AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Invalid selection: " + sel + " Valid: " + talkData.validSelections.toString() + " Npc: " + c.getCM().getNpc());
							}
						}
					}
				}
				if(lastMsg == ScriptMessageType.AskNumber.getMsgType()){
					/*if(sel == 0){
						AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to input invalid selection 0");
						// Logger.log(LogType.INFO, LogFile.ANTICHEAT, c.getPlayer().getName() + " tried to input invalid selection 0");
						selection = 1;
					}*/
					if(c.getCM() != null){
						if(c.getCM().getTalkData() != null){
							NpcTalkData talkData = c.getCM().getTalkData();
							if(sel < talkData.min || sel > talkData.max){
								AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to input invalid selection " + sel + " when min is: " + talkData.min + " and max is: " + talkData.max + " Npc: " + c.getCM().getNpc());
								// Logger.log(LogType.INFO, LogFile.ANTICHEAT, c.getPlayer().getName() + " tried to input invalid selection " + sel + " when min is: " + talkData.min + " and max is: " + talkData.max);
								selection = talkData.def;
							}
						}
					}
				}
				if(sel < 0){
					// Logger.log(LogType.INFO, LogFile.ANTICHEAT, c.getPlayer().getName() + " tried negative selection: " + sel);
					AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried negative selection: " + sel + " Npc: " + c.getCM().getNpc());
					selection = 0;
				}else selection = sel;
				data += " Selection: " + selection;
				if(c.getQM() != null){
					if(c.getQM().isStart()){// lastMsg = type
						QuestScriptManager.getInstance().start(c, mode, lastMsg, selection);
					}else{
						QuestScriptManager.getInstance().end(c, mode, lastMsg, selection);
					}
				}else if(c.getCM() != null){
					NPCScriptManager.getInstance().action(c, mode, lastMsg, selection);
				}
			}
		}catch(Exception ex){
		}
	}
}