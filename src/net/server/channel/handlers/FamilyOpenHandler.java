package net.server.channel.handlers;

import java.rmi.RemoteException;

import client.MapleClient;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.world.family.Family;
import net.world.family.FamilyCharacter;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.FamilyPackets;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 28, 2017
 */
public class FamilyOpenHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!FeatureSettings.FAMILY){
			c.announce(CWvsContext.enableActions());
			return;
		}
		if(c.getPlayer().getFamilyId() >= 0){
			try{
				System.out.println("A");
				Family family = ChannelServer.getInstance().getWorldInterface().getFamily(c.getPlayer().getFamilyId());
				c.announce(FamilyPackets.priviliegeList(c.getPlayer()));// should be grabbing shit from ^
				c.announce(FamilyPackets.getFamilyInfo(family, family.members.get(c.getPlayer().getId())));
				c.announce(FamilyPackets.showPedigree(c.getPlayer().getId(), family));
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}else{
			System.out.println("B");
			Family fake = new Family();
			fake.bossID = c.getPlayer().getId();
			fake.familyName = "";
			FamilyCharacter fc = new FamilyCharacter();
			fc.characterID = fake.bossID;
			c.announce(FamilyPackets.priviliegeList(c.getPlayer()));
			c.announce(FamilyPackets.getFamilyInfo(fake, fc));
		}
	}
}
