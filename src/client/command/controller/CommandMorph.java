package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.Skill;
import client.SkillFactory;
import client.command.Command;
import server.MapleStatEffect;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 20, 2016
 */
public class CommandMorph extends Command{

	public CommandMorph(){
		super("Morph", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			int morphid = Integer.parseInt(args[0]);
			for(Skill s : SkillFactory.getSkills()){
				MapleStatEffect mse = s.getEffect(s.getMaxLevel());
				if(mse != null){
					if(mse.getMorph() == morphid){
						mse.applyTo(c.getPlayer());
						break;
					}
				}
			}
		}
		return false;
	}
}
