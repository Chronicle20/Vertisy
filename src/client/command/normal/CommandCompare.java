package client.command.normal;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Apr 29, 2016
 */
public class CommandCompare extends Command{

	public CommandCompare(){
		super("Compare", "", "@Compare <target>", "");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		/*if(args.length > 0){
			MapleCharacter mc = c.getWorldServer().getCharacterByName(args[0]);
			if(mc != null){
				StringBuilder sb = new StringBuilder("");
				for(RSSkill skill : RSSkill.values()){
					sb.append(c.getPlayer().getRSSkillExp(skill));
					sb.append(" - ");
					sb.append(c.getPlayer().getRSSkillLevel(skill));
					sb.append(" - ");
					sb.append(skill.name());
					sb.append(" - ");
					sb.append(mc.getRSSkillLevel(skill));
					sb.append(" - ");
					sb.append(mc.getRSSkillExp(skill));
					sb.append("\r\n");
				}
				c.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, sb.toString(), "00 00", (byte) 0));
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown player " + args[0] + " are they online?");
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsuage());
		}*/
		return false;
	}
}
