package client.command.gm;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 7, 2017
 */
public class CommandDebuff extends Command{

	public CommandDebuff(){
		super("Debuff", "", "!Debuff <debuff> <level>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 1){
			MapleCharacter target = c.getPlayer();
			if(args.length > 2){
				MapleCharacter newTarget = c.getChannelInstance().getPlayerStorage().getCharacterByName(args[0]);
				if(newTarget != null){
					target = newTarget;
				}else{
					c.getPlayer().dropMessage(MessageType.ERROR, "Unknown target: " + args[0]);
					return false;
				}
			}
			Integer skillid = ObjectParser.isInt(args[args.length - 2]);
			Integer level = ObjectParser.isInt(args[args.length - 1]);
			if(skillid == null || level == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Please enter an integer for skillid and level");
				return false;
			}
			MobSkill skill = MobSkillFactory.getMobSkill(skillid, level);
			if(skill != null){
				skill.getEffect().applyTo(target);
				c.getPlayer().dropMessage(MessageType.SYSTEM, "Applied");
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Invalid mobskill: " + skillid + " with level: " + level);
			}
		}
		return false;
	}
}
