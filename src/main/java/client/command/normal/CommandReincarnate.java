package client.command.normal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import client.*;
import client.command.Command;
import server.MapleCharacterInfo;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packets.CWvsContext;
import tools.packets.field.userpool.UserRemote;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Apr 4, 2016
 */
public class CommandReincarnate extends Command{

	public CommandReincarnate(){
		super("Reincarnate", "Become a better you.", "", "");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleCharacter mc = c.getPlayer();
		if(mc.getLevel() >= mc.getMaxLevel()){
			Random rand = new Random();
			List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
			mc.addReincarnation();
			mc.setLevel(1);
			mc.setHighestLevel(1);
			mc.setExp(0);
			statup.add(new Pair<>(MapleStat.LEVEL, mc.getLevel()));
			statup.add(new Pair<>(MapleStat.EXP, mc.getExp()));
			mc.setSkinColor(MapleSkinColor.values()[rand.nextInt(MapleSkinColor.values().length)]);
			Object[] hairs = MapleCharacterInfo.getInstance().getHairs().keySet().toArray();
			Object randomHair = hairs[rand.nextInt(hairs.length)];
			Object[] faces = MapleCharacterInfo.getInstance().getFaces().keySet().toArray();
			Object randomFace = faces[rand.nextInt(faces.length)];
			mc.setHair((int) randomHair);
			statup.add(new Pair<>(MapleStat.HAIR, mc.getHair()));
			mc.setFace((int) randomFace);
			statup.add(new Pair<>(MapleStat.FACE, mc.getFace()));
			statup.add(new Pair<>(MapleStat.SKIN, mc.getSkinColor().getId()));
			mc.announce(CWvsContext.updatePlayerStats(statup, mc));
			mc.getMap().broadcastMessage(mc, UserRemote.updateCharLook(mc), false);
			c.announce(MaplePacketCreator.showEffect("5th/reincarnate"));
		}else{
			mc.dropMessage(MessageType.ERROR, "You are not powerful enough to reincarnate.");
		}
		return false;
	}
}
