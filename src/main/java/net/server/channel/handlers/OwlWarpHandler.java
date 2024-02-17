package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.maps.objects.HiredMerchant;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.field.MiniRoomBase;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 9, 2017
 */
public class OwlWarpHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		int ownerid = slea.readInt();
		int mapid = slea.readInt();
		if(mapid >= 910000001 && mapid <= 910000022){
			HiredMerchant merchant = chr.getClient().getChannelServer().getHiredMerchants().get(ownerid);
			if(merchant != null){
				chr.changeMap(mapid);
				if(merchant.isOwner(c.getPlayer())){
					merchant.setOpen(false);
					merchant.removeAllVisitors("");
					c.announce(MiniRoomBase.EntrustedShop.getHiredMerchant(chr, merchant, false));
				}else if(!merchant.isOpen()){
					chr.dropMessage(1, "This shop is in maintenance, please come by later.");
					return;
				}else if(merchant.getFreeSlot() == -1){
					chr.dropMessage(1, "This shop has reached it's maximum capacity, please come by later.");
					return;
				}else{
					merchant.addVisitor(c.getPlayer());
					c.announce(MiniRoomBase.EntrustedShop.getHiredMerchant(c.getPlayer(), merchant, false));
				}
			}
		}
	}
}
