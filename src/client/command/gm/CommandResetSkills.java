package client.command.gm;

import java.util.Map.Entry;

import client.MapleCharacter.SkillEntry;
import client.MapleClient;
import client.PlayerGMRank;
import client.Skill;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 24, 2017
 */
public class CommandResetSkills extends Command{

	public CommandResetSkills(){
		super("ResetSkills", "Clear all your skills.", "!ResetSkills", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		for(Entry<Skill, SkillEntry> data : c.getPlayer().getSkills().entrySet()){
			c.getPlayer().changeSkillLevel(data.getKey(), (byte) -1, data.getValue().masterlevel, -1);
		}
		return false;
	}
}
