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

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author kevintjuh93
 * @modified iPoopMagic (David)
 */
public final class MonsterCarnivalHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		/* CPQ Messages
		 1: You don't have enough CP to continue.
		 2: You can no longer summon the monster.
		 3: You can no longer summon the being.
		 4: This being is already summoned.
		 5: This request has failed due to an unknown error.
		 */
		/*MapleCharacter chr = c.getPlayer();
		MonsterCarnivalParty carnivalParty = chr.getCarnivalParty();
		if(chr.getCarnivalParty() == null || chr.getMap().getMapData().getMCS() == null){
			c.announce(CWvsContext.enableActions());
			return;
		}
		int tab = slea.readByte();
		int number = slea.readShort();
		if(chr.getEventInstance() != null){
			if(carnivalParty.getTeam() != 0 && carnivalParty.getTeam() != 1){
				chr.getMap().broadcastMessage(MaplePacketCreator.leaveCPQ(chr));
				chr.changeMap(980000010);
			}
			if(chr.getCP() > getPrice(chr, tab, number)){
				if(tab == 0){ // SPAWNING
					if(carnivalParty.canSummon()){ // what is max in GMS?
						// TODO: Use Monster carnival settings
						int x = 0;
						int y = 162;
						int roomNum = chr.getMapId() % 1000 / 100;
						if(roomNum == 1 || roomNum == 2){
							x = 365;
						}else if(roomNum == 3 || roomNum == 4){
							x = 880;
						}else if(roomNum == 5 || roomNum == 6){
							x = 530;
						}
						chr.getMap().spawnCPQMonster(MapleLifeFactory.getMonster(getMonster(number)), new Point((carnivalParty.getTeam() == 0 ? x : -x), y), carnivalParty.getTeam(), true); // remember it's gotta spawn on the other team's side
						chr.getCarnivalParty().useCP(chr, getPrice(chr, tab, number));
						chr.getMap().broadcastMessage(MaplePacketCreator.updateCP(chr.getCP(), carnivalParty.getTotalCP()));
						chr.getMap().broadcastMessage(MaplePacketCreator.updatePartyCP(carnivalParty));
						// carnivalParty.onSummon();
					}else{
						chr.announce(MaplePacketCreator.CPQMessage((byte) 2));
					}
				}else if(tab == 1){ // SKILL (actually it's diseases for the other team)
					for(MapleCharacter player : chr.getMap().getCharacters()){
						if(player.getCarnivalParty().getTeam() != chr.getCarnivalParty().getTeam()){
							if(number == 8){
								player.dispel();
							}else{
								MobSkillFactory.getMobSkill(getMobSkillDiseaseID(number), 1).getEffect().applyTo(player);
							}
						}
					}
					chr.getCarnivalParty().useCP(chr, getPrice(chr, tab, number));
					chr.getMap().broadcastMessage(MaplePacketCreator.updateCP(chr.getCP(), carnivalParty.getTotalCP()));
					chr.getMap().broadcastMessage(MaplePacketCreator.updatePartyCP(carnivalParty));
				}else if(tab == 2){ // PROTECTION (reactor spawning)
					//old code
					/*int rid = 9980000 + chr.getCarnivalParty().getTeam();
					MapleReactor reactor = new MapleReactor(MapleReactorFactory.getReactor(rid), rid);
					reactor.setState((byte) 1);
					// For now, let's just spawn all the reactors in one spot. Make it easy.
					reactor.setPosition(new Point(900 * (chr.getCarnivalParty().getTeam() == 0 ? -1 : 1), -138));
					if (chr.getMap().getMobBuffs().isEmpty() || chr.getMap().getMobBuffs() == null || !chr.getMap().getMobBuffs().contains(MobSkillFactory.getMobSkill(getMobStatusSkillId(number), 1))) {
					    reactor.setName(getMobStatusSkillId(number) + "I.AM.CARNIVAL.REACTOR." + chr.getCarnivalParty().getTeam());
					    chr.getMap().addMobBuffToReactor(MobSkillFactory.getMobSkill(getMobStatusSkillId(number), 1)); // We need to store it so we can debuff later
					    System.out.println("Spawning reactor: " + reactor.getName() + " at position: " + reactor.getPosition() + ". It has: " + chr.getMap().reactorMobSkills + " as mobBuffs.");
					    chr.getMap().spawnReactor(reactor);
					} else {
					    chr.getMap().broadcastMessage(MaplePacketCreator.CPQMessage((byte) 4));
					    return;
					}*/
		/*for(MapleMonster mob : c.getPlayer().getEventInstance().getMapInstance(c.getPlayer().getMapId()).getMonsters()){
			if(mob.getTeam() < 0){
				System.out.println("Mob " + mob.getName() + " has no team; cannot give it buff. Team #: " + mob.getTeam());
				break;
			}
			if(mob.getTeam() != chr.getCarnivalParty().getTeam()){
				System.out.println("Applying effect to mob " + mob.getName() + " on Team " + mob.getTeam());
				mob.getMobSkill(getMobStatusSkillId(number)).applyEffect(null, mob, false);
			}
		}
		System.out.println("Finished applying effects to mobs.");
		chr.getCarnivalParty().useCP(chr, getPrice(chr, tab, number));
		chr.getMap().broadcastMessage(MaplePacketCreator.updateCP(chr.getCP(), carnivalParty.getTotalCP()));
		chr.getMap().broadcastMessage(MaplePacketCreator.updatePartyCP(carnivalParty));
		}
		}else{
		chr.getMap().broadcastMessage(MaplePacketCreator.CPQMessage((byte) 1));
		}
		}else{
		chr.announce(MaplePacketCreator.CPQMessage((byte) 5));
		}*/
		c.getPlayer().announce(CWvsContext.enableActions());
	}

	public int getMonster(int num){
		int mid = 0;
		num++;
		switch (num){
			case 1:
				mid = 9300127;
				break;
			case 2:
				mid = 9300128;
				break;
			case 3:
				mid = 9300129;
				break;
			case 4:
				mid = 9300130;
				break;
			case 5:
				mid = 9300131;
				break;
			case 6:
				mid = 9300132;
				break;
			case 7:
				mid = 9300133;
				break;
			case 8:
				mid = 9300134;
				break;
			case 9:
				mid = 9300135;
				break;
			case 10:
				mid = 9300136;
				break;
		}
		return mid;
	}

	public int getPrice(MapleCharacter chr, int tab, int selection){
		int price = 0;
		if(tab == 0){// Mobs
			chr.getMap().getMapData().getMCS().getMobInfo(selection).getRight();
		}else if(tab == 1){// skills
			switch (selection){
				case 1:
					price = 17;
					break;
				case 2:
				case 4:
					price = 19;
					break;
				case 3:
					price = 12;
					break;
				case 5:
					price = 16;
					break;
				case 6:
					price = 14;
					break;
				case 7:
					price = 22;
					break;
				case 8:
					price = 18;
					break;
			}
		}else{// guardians
			switch (selection){
				case 1:
				case 3:
					price = 17;
					break;
				case 2:
				case 4:
				case 6:
					price = 16;
					break;
				case 5:
					price = 13;
					break;
				case 7:
					price = 12;
					break;
				case 8:
				case 9:
					price = 35;
					break;
			}
		}
		return price;
	}

	public MapleBuffStat getDiseaseByNum(int num){
		num++;
		switch (num){
			case 1:
				return MapleBuffStat.DARKNESS;
			case 2:
				return MapleBuffStat.WEAKEN;
			case 3:
				return MapleBuffStat.CURSE;
			case 4:
				System.out.println("For sake of testing, we will avoid this disease.");
				// return MapleDisease.POISON;
			case 5:
				return MapleBuffStat.SLOW;
			case 6:
				return MapleBuffStat.SEAL;
			case 7:
				return MapleBuffStat.STUN;
		}
		return null;
	}

	public int getMobSkillDiseaseID(int num){
		num++;
		switch (num){
			case 1:
				return 121;
			case 2:
				return 122;
			case 3:
				return 124;
			case 4:
				System.out.println("For sake of testing, we will avoid this disease.");
				// return 125;
			case 5:
				return 126;
			case 6:
				return 120;
			case 7:
				return 123;
		}
		return 0;
	}

	public int getMobStatusSkillId(int num){
		num++;
		switch (num){
			case 1:
				return 150;
			// return MonsterStatus.WEAPON_ATTACK_UP;
			case 2:
				return 151;
			// return MonsterStatus.WEAPON_DEFENSE_UP;
			case 3:
				return 152;
			// return MonsterStatus.MAGIC_ATTACK_UP;
			case 4:
				return 153;
			// return MonsterStatus.MAGIC_DEFENSE_UP;
			case 5:
				return 154;
			// return MonsterStatus.ACC;
			case 6:
				return 155;
			// return MonsterStatus.AVOID;
			case 7:
				return 156;
			// return MonsterStatus.SPEED;
			case 8:
				return 140;
			// return MonsterStatus.WEAPON_IMMUNITY;
			case 9:
				return 141;
			// return MonsterStatus.MAGIC_IMMUNITY;
		}
		return 0;
	}
}
