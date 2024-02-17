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
package client.inventory;

import java.util.ArrayList;
import java.util.List;

import server.ItemData;
import server.ItemInformationProvider;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @author Arnah
 */
public class PetDataFactory{

	public static PetData getData(int petid, int skillid){
		ItemData data = ItemInformationProvider.getInstance().getItemData(petid);
		return data.petData.get("" + skillid);
	}

	public static class PetData{

		public String command = "";
		public int inc, prob;
		public int l0, l1;
		public PetAction fail = new PetAction();
		public PetAction success = new PetAction();

		public void save(LittleEndianWriter lew){
			lew.writeMapleAsciiString(command);
			lew.writeInt(inc);
			lew.writeInt(prob);
			lew.writeInt(l0);
			lew.writeInt(l1);
			fail.save(lew);
			success.save(lew);
		}

		public void load(LittleEndianAccessor lea){
			command = lea.readMapleAsciiString();
			inc = lea.readInt();
			prob = lea.readInt();
			l0 = lea.readInt();
			l1 = lea.readInt();
			fail.load(lea);
			success.load(lea);
		}

		public static class PetAction{

			public List<String> response = new ArrayList<>();
			public String act = "";

			public void save(LittleEndianWriter lew){
				lew.writeInt(response.size());
				for(String r : response){
					lew.writeMapleAsciiString(r);
				}
				lew.writeMapleAsciiString(act);
			}

			public void load(LittleEndianAccessor lea){
				int size = lea.readInt();
				for(int i = 0; i < size; i++){
					response.add(lea.readMapleAsciiString());
				}
				act = lea.readMapleAsciiString();
			}
		}
	}
}
