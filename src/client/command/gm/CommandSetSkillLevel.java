package client.command.gm;

import client.*;
import client.command.Command;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 28, 2016
 */
public class CommandSetSkillLevel extends Command{

	public CommandSetSkillLevel(){
		super("SetSkilllevel", "Set the level of a specific skill.", "!SetSkilllevel <skill> <level>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 1){
			Byte amount = ObjectParser.isByte(args[1]);
			if(amount == null){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}
			Integer skillid = ObjectParser.isInt(args[0]);
			if(skillid == null){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}
			Skill skill = SkillFactory.getSkill(skillid);
			if(skill == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown skill id");
				return false;
			}
			c.getPlayer().changeSkillLevel(skill, amount, skill.getMaxLevel(), -1);
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
