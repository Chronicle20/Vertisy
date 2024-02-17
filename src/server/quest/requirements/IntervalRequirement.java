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
package server.quest.requirements;

import client.MapleCharacter;
import client.MapleQuestStatus;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @author Tyler (Twdtwd)
 */
public class IntervalRequirement extends MapleQuestRequirement{

	private int interval = -1;
	private int questID;

	public IntervalRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.INTERVAL);
		processData(data);
		questID = quest.getId();
	}

	public IntervalRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.INTERVAL);
		processData(lea);
		questID = quest.getId();
	}

	@Override
	public void processData(MapleData data){
		interval = MapleDataTool.getInt(data) * 60 * 1000;
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		interval = lea.readInt();
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(interval);
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		boolean check = !chr.getQuest(MapleQuest.getInstance(questID)).getStatus().equals(MapleQuestStatus.Status.COMPLETED);
		boolean check2 = chr.getQuest(MapleQuest.getInstance(questID)).getCompletionTime() <= System.currentTimeMillis() - interval;
		if(!check2 && !check){
			chr.dropMessage(1, "You can not start this quest again yet.");
		}
		return check || check2;
	}
}
