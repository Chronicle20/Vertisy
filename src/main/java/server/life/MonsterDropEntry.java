/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package server.life;

import client.inventory.MapleInventoryType;
import server.ItemInformationProvider;

/**
 * @author LightPepsi
 */
public class MonsterDropEntry{

	public enum DropCategory{
	    // meso, basically it's item type undefined as well.
		UNDEFINED,
	    // inventory types
		EQUIP,
		USE,
		ETC,
		SETUP,
		CASH,
	    // crafting items
		ORE,
		MOBETC,
		SIMULATOR,
		MAGICPOWDER,
		MANUAL,
	    // alt weapons (stars, arrows, bullets...)
		STAR,
		CAPSULE,
		ARROW,
		BULLET,
	    // scrolls
		SCROLL,
		CHAOS, // clean slates are here too.
	    // pots, refer to some examples of them below, or you can use a wz tool to view them.
		POTS1,
		POTS2,
		POTS3,
		POTS4,
		TRANSFORM,
	    // books
		SKILLBOOK,
		MASTERYBOOK,
		MONSTERBOOK,
	    // misc
		GAME,
		NLC,
	    // item id and item id groups
		DRAGONETC,
		ARWENGLASSHOE;

		public static DropCategory getCategory(String category){
			category = category.toUpperCase();
			for(DropCategory cat : DropCategory.values()){
				if(cat.name().equals(category)){ return cat; }
			}
			return null;
		}

		public static DropCategory getDefaultCategory(int item_id){
			MapleInventoryType mit = ItemInformationProvider.getInstance().getInventoryType(item_id);
			// based on item id and item id groups (how you put them :D)
			switch (item_id){
				case 4000030: // dragon skin
				case 4000244: // dragon spirit
				case 4000245: // dragon scale
					return DRAGONETC;
				case 4001000: // arwen's glass shoe
					return ARWENGLASSHOE;
			}
			// based on groups (how nexon puts them)
			int number = (item_id / 1000) % 1000;
			switch (mit){
				case UNDEFINED: // meso, itemid: 0
					return UNDEFINED;
				case EQUIP:
					return EQUIP;
				case USE:
					switch (number){
						case 0: // normal potions
							return POTS1;
						case 1: // watermelons, ice cream pop, sundae
						case 2: // speed potions, ginger ale, etc
							return POTS2;
						// case 3: // advanced potions from crafting (should not drop) //irelevant to v83
						// case 4: // same thing //irelevant to v83
						// case 11: // poison mushroom
						// case 28: // cool items
						// case 30: // return scrolls
						// case 31: // weird invitation return scrolls
						// case 46: // gallant scrolls
						// return 0;
						case 10: // strange potions like apples, eggs
						case 20: // salad, fried chicken, dews, food stuffs
						case 22: // pure water, cider, unagi, air bubbles, korean, jap, sg, msia and event food, mansion drops, king pepe's box and lots of stuff. ALSO nependeath honey but oh well
							return POTS3;
						case 12: // drakes blood, sap of ancient tree (rare use)
						case 50: // antidotes (debuff healing stuffs) and stuff
							return POTS4;
						case 40: // Scrolls
						case 41: // Scrolls
						case 43: // Scrolls
						case 44: // Scrolls
						case 48: // pet scrolls
							return SCROLL;
						// case 83: //heart mega
						// case 84: //skull mega
						// case 100: // summon bags
						// case 101: // summon bags
						// case 102: // summon bags
						// case 109: // summon bags
						// case 120: // pet food
						// case 211: // cliffs special potion
						// case 240: // rings (proposal stuffs)
						// case 270: // pheromone, additional weird stuff
						// case 310: // teleport rock
						// case 320: // weird drops
						// case 340: // white scroll
						// case 390: // weird
						// case 430: // Scripted items
						// case 440: // jukebox
						// case 460: // magnifying glass
						// case 470: // golden hammer
						// case 490: // crystanol
						// case 500: // sp reset
						// return 0;
						// case 47: // tablets from dragon rider //irelevant to v83
						// return 220000;
						case 49: // clean slates, potential scroll, ees, chaos, chaos-like stuffs
							return CHAOS;
						case 70: // throwing stars
							return STAR;
						case 331: // blaze capsule
						case 332: // glaze capsule
							return CAPSULE;
						case 60: // bow arrows
						case 61: // crossbow arrows
							return ARROW;
						case 210: // transform pots, rare monster piece drops
							// case 213: // boss transfrom //irelevant to v83
							return TRANSFORM;
						case 280: // skill books
							return SKILLBOOK;
						case 290: // mastery books
							return MASTERYBOOK;
						case 330: // bullets
							return BULLET;
						case 381: // monster book things
						case 382:
						case 383:
						case 384:
						case 385:
						case 386:
						case 387:
						case 388:
							return MONSTERBOOK;
						// case 510: // recipes //irelevant to v83
						// case 511:
						// case 512:
						// return 10000;
						default:
							return USE;
					}
				case ETC:
					switch (number){
						case 0: // monster pieces
							return MOBETC;
						case 4: // crystal ores
						case 10: // mineral ores
						case 20: // jewel ores
							return ORE;
						case 7: // magic powders
							return MAGICPOWDER;
						case 130: // simulators
							return SIMULATOR;
						case 131: // manuals
							return MANUAL;
						case 30: // game pieces
							return GAME;
						case 32: // nlc outside of mansion etc, misc items
							return NLC;
						default:
							return ETC;
					}
				case SETUP:
					return SETUP;
				case CASH:
					return CASH;
				default:
					return null;
			}
		}
	}

	public MonsterDropEntry(int itemId, int chance, byte dropType, int Minimum, int Maximum, short questid){
		this.itemId = itemId;
		this.chance = chance;
		this.dropType = dropType;
		this.questid = questid;
		this.Minimum = Minimum;
		this.Maximum = Maximum;
	}

	public byte dropType;
	public short questid;
	public int itemId, chance, Minimum, Maximum;
}