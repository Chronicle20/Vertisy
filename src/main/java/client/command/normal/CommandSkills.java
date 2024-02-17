package client.command.normal;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.RSSkill;
import client.command.Command;
import constants.ExpTable;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 14, 2016
 */
public class CommandSkills extends Command{

	public CommandSkills(){
		super("Skills", "Get your current skill progress", "", "skill");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		StringBuilder sb = new StringBuilder("");
		int z = 0;
		for(RSSkill skill : RSSkill.values()){
			sb.append(skill.name());
			sb.append(": ");
			sb.append(c.getPlayer().getRSSkillLevel(skill));
			sb.append(" (");
			sb.append(c.getPlayer().getRSSkillExp(skill));
			sb.append("/");
			sb.append(ExpTable.getRSSkillExpNeededForLevel(c.getPlayer().getRSSkillLevel(skill) + 1));
			sb.append(") ");
			if(++z == 3){
				sb.append("-");
				z = 0;
			}
		}
		for(String s : sb.toString().split("-")){
			c.getPlayer().dropMessage(MessageType.SYSTEM, s);
		}
		return false;
	}
}