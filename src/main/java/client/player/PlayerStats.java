package client.player;

import java.util.Calendar;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleJob;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConstants;
import net.channel.ChannelServer;
import net.server.world.MaplePartyCharacter;
import server.ItemInformationProvider;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 22, 2016
 */
public class PlayerStats{

	private ItemInformationProvider ii = ItemInformationProvider.getInstance();
	private double expBuff, questExpBuff, dropBuff, mesoBuff, expMod, questExpMod, dropMod, mesoMod;
	private double bonusExpBuff, partyExpBuff;
	private int attackSpeed;
	private boolean isMage = false;

	// Nexon does server rate * card rate then everything else is += (1.5 - 1.0) = 0.5 increase.
	// But we do server rate + (card rate - 1) then everything else is += (1.5 - 1.0) = 0.5 increase.
	public void recalcLocalStats(MapleCharacter chr){
		if(chr == null) return;
		resetLocalStats(chr);
		// expMod += 1000;
		Integer holySymbol = chr.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
		if(holySymbol != null){
			bonusExpBuff += holySymbol.doubleValue();
			// expMod *= 1.0 + (holySymbol.doubleValue() / 100);
		}
		Integer mesoup = chr.getBuffedValue(MapleBuffStat.MESOUP);
		if(mesoup != null){
			mesoMod *= 1.0 + (mesoup.doubleValue() / 100.0);
		}
		/*boolean expCard = false, dropCard = false;
		for(NXCard card : NXCard.values()){
			if(card.getEffect().equals(NXCardEffect.DROP) && card.isValid() && !dropCard && chr.haveItem(card.getItemID())){
				dropMod += (card.getMultiplier() - 1);
				mesoMod += (card.getMultiplier() - 1);
				dropCard = true;
			}else if(card.getEffect().equals(NXCardEffect.EXP) && card.isValid() && !expCard && chr.haveItem(card.getItemID())){
				expMod += (card.getMultiplier() - 1);
				expCard = true;
			}
		}*/
		if(chr.getBuffedValue(MapleBuffStat.MESO_UP_BY_ITEM) != null){
			mesoBuff += chr.getBuffEffect(MapleBuffStat.MESO_UP_BY_ITEM).getProb();
		}
		if(chr.getBuffedValue(MapleBuffStat.ITEM_UP_BY_ITEM) != null){
			dropBuff += chr.getBuffEffect(MapleBuffStat.ITEM_UP_BY_ITEM).getProb();
		}
		if(chr.isProgressValueSet("lastrune")){
			String val = (String) chr.getProgressValue("lastrune");
			if(val.length() > 0){
				Long time = ObjectParser.isLong(val);
				if(time != null){
					long runeExpBonusEnd = time += (5 * 60 * 1000L);
					if(System.currentTimeMillis() < runeExpBonusEnd){
						expMod += 1;
					}
				}
			}
		}
		if(chr.getItemQuantity(4030002, false) > 0) expMod += chr.getItemQuantity(4030002, false);
		int rank = 0;
		double expRank = 20;
		for(int guildID : ChannelServer.getInstance().getTopGuilds()){
			if(++rank <= 10){
				if(chr.getGuildId() == guildID){
					bonusExpBuff += expRank;
				}
				if(rank < 4) expRank -= 5;
			}else break;
		}
		Integer booster = chr.getBuffedValue(MapleBuffStat.BOOSTER);
		Integer speedInfusion = chr.getBuffedValue(MapleBuffStat.SPEED_INFUSION);
		if(speedInfusion != null && !isMage){
			attackSpeed -= 2;
		}
		if(booster != null){
			attackSpeed -= 2;
		}
		attackSpeed = Math.max(2, attackSpeed);
		if(ServerConstants.expEventEnd >= Calendar.getInstance().getTimeInMillis()){
			expMod += ServerConstants.expEvent;
		}
		if(GameConstants.isWeekend()){
			bonusExpBuff += 20;
		}
		if(chr.getEventInstance() != null && chr.isInParty()){
			int partyMembers = 0;
			for(MaplePartyCharacter member : chr.getParty().getMembers()){
				if(member != null && member.isOnline()){
					MapleCharacter mc = member.getPlayerInChannel();
					if(mc != null && mc.getEventInstance() != null && mc.getEventInstance().getName().equals(chr.getEventInstance().getName())){
						partyMembers++;
					}
				}
			}
			if(partyMembers > 3){
				partyExpBuff += (partyMembers - 3) * 10;// 10% per member after 3
			}
		}
	}

	public void resetLocalStats(MapleCharacter chr){
		if(chr != null && chr.getClient() != null){
			expBuff = 0;
			bonusExpBuff = 0;
			partyExpBuff = 0;
			dropBuff = 0;
			mesoBuff = 0;
			if(chr.isHardMode()){
				expMod = 1;
			}else{
				expMod = chr.getLevel() < 10 ? 1 : chr.getClient().getChannelServer().getChannelServer().getExpRate();
			}
			expMod -= chr.getReincarnations() * 0.5;
			expMod = Math.max(1, expMod);
			questExpMod = chr.isHardMode() || chr.getLevel() < 10 ? 1 : chr.getClient().getChannelServer().getChannelServer().getQuestExpRate();
			dropMod = chr.getClient().getChannelServer().getChannelServer().getDropRate();
			mesoMod = chr.getClient().getChannelServer().getChannelServer().getMesoRate();
			Item wep = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
			isMage = chr.getJob().isA(MapleJob.MAGICIAN) || chr.getJob().isA(MapleJob.BLAZEWIZARD1);
			if(wep != null && !isMage){
				attackSpeed = ii.getItemData(wep.getItemId()).attackSpeed;
			}else{
				attackSpeed = chr.getJob().isA(MapleJob.PIRATE) ? 3 : 6;
			}
		}
	}

	public double getExpRate(){
		return (expBuff > 0 ? (expBuff / 100) : 0) + expMod;
	}

	public double getBonusExpBuff(){
		return bonusExpBuff;
	}

	public double getPartyExpBuff(){
		return partyExpBuff;
	}

	public double getQuestExpRate(){
		return (questExpBuff > 0 ? (questExpBuff / 100) : 0) + questExpMod;
	}

	public double getDropRate(){
		return (dropBuff > 0 ? (dropBuff / 100) : 0) + dropMod;
	}

	public double getMesoRate(){
		return (mesoBuff > 0 ? (mesoBuff / 100) : 0) + mesoMod;
	}

	public int getAttackSpeed(){
		return attackSpeed;
	}
}
