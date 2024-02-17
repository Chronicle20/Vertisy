package client.command.gm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import client.*;
import client.MapleCharacter.SkillEntry;
import client.command.Command;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packets.CWvsContext;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 30, 2017
 */
public class CommandCopyChr extends Command{

	public CommandCopyChr(){
		super("CopyChr", "Resets skills, sp, ap, inventory and copies target.", "!copychr <name>", "copyplayer");
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(args[0]);
			if(target == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unable to find player.");
				return false;
			}
			MapleCharacter player = c.getPlayer();
			List<Pair<MapleStat, Integer>> statup = new ArrayList<>();
			player.changeJob(target.getJob());
			for(Entry<Skill, SkillEntry> data : player.getSkills().entrySet()){
				player.changeSkillLevel(data.getKey(), (byte) -1, data.getValue().masterlevel, -1);
			}
			for(Entry<Skill, SkillEntry> data : target.getSkills().entrySet()){
				player.changeSkillLevel(data.getKey(), (byte) data.getValue().skillevel, data.getValue().masterlevel, -1);
			}
			player.setLevel(target.getLevel());
			statup.add(new Pair<>(MapleStat.LEVEL, player.getLevel()));
			player.setExp(target.getExp());
			statup.add(new Pair<>(MapleStat.EXP, player.getExp()));
			player.setStr(target.getStr());
			statup.add(new Pair<>(MapleStat.STR, player.getStr()));
			player.setDex(target.getDex());
			statup.add(new Pair<>(MapleStat.DEX, player.getDex()));
			player.setInt(target.getInt());
			statup.add(new Pair<>(MapleStat.INT, player.getInt()));
			player.setLuk(target.getLuk());
			statup.add(new Pair<>(MapleStat.LUK, player.getLuk()));
			player.setMaxHp(target.getMaxHp());
			statup.add(new Pair<>(MapleStat.MAXHP, player.getMaxHp()));
			player.setHp(target.getHp());
			statup.add(new Pair<>(MapleStat.HP, player.getHp()));
			player.setMaxMp(target.getMaxMp());
			statup.add(new Pair<>(MapleStat.MAXMP, player.getMaxMp()));
			player.setMp(target.getMp());
			statup.add(new Pair<>(MapleStat.MP, player.getMp()));
			player.setRemainingAp(target.getRemainingAp());
			statup.add(new Pair<>(MapleStat.AVAILABLEAP, player.getRemainingAp()));
			int index = 0;
			for(int sp : target.getRemainingSps()){
				player.setRemainingSp(sp, index++);
			}
			statup.add(new Pair<>(MapleStat.AVAILABLESP, player.getRemainingSp()));
			c.announce(CWvsContext.updatePlayerStats(statup, player));
			List<ModifyInventory> mods = new ArrayList<>();
			List<Short> posRemove = new ArrayList<>();
			for(Item item : player.getInventory(MapleInventoryType.EQUIPPED).list()){
				mods.add(new ModifyInventory(3, item));
				posRemove.add(item.getPosition());
			}
			posRemove.forEach(s-> player.getInventory(MapleInventoryType.EQUIPPED).removeSlot(s));
			for(Item item : target.getInventory(MapleInventoryType.EQUIPPED).list()){
				Equip equip = ((Equip) item).copy();
				player.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
				mods.add(new ModifyInventory(0, equip));
			}
			c.announce(MaplePacketCreator.modifyInventory(true, mods));
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
