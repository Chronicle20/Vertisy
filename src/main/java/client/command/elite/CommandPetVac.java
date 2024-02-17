package client.command.elite;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 25, 2016
 */
public class CommandPetVac extends Command{

	public CommandPetVac(){
		super("PetVac", "", "", null);
		setGMLevel(PlayerGMRank.ELITE);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.setPetVac(!c.getPetVac());
		c.getPlayer().dropMessage(MessageType.SYSTEM, "Pet Vac " + (c.getPetVac() ? "Enabled" : "Disabled") + ".");
		return false;
	}
}
