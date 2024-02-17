package client.command.gm;

import client.*;
import client.command.Command;
import server.MapleCharacterInfo;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandFace extends Command{

	public CommandFace(){
		super("Face", "Change your face", "!face <face> <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleCharacter target = c.getPlayer();
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		Integer face = ObjectParser.isInt(args[0]);
		if(face == null){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		if(MapleCharacterInfo.getInstance().getFaces().get(face) == null){
			c.getPlayer().dropMessage(MessageType.ERROR, "Unknown face");
			return false;
		}
		if(args.length == 2){
			MapleCharacter victim = c.getChannelServer().getChannelServer().getCharacterByName(args[1]);
			if(victim == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown player.");
				return false;
			}else{
				target = victim;
			}
		}
		target.updateSingleStat(MapleStat.FACE, face);
		target.setFace(face);
		target.equipChanged();
		return false;
	}
}
