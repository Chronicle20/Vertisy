package tools.packets;

import client.MapleCharacter;
import client.inventory.MaplePet;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 27, 2017
 */
public class PetPacket{
	// CUserPool::OnUserCommonPacket
	// decode 4(chr id)
	// CUser::OnPetPacket
	// decode 1(pet index)

	public static byte[] petExceptionListResult(MapleCharacter chr, MaplePet pet){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PET_EXCEPTION_LIST_RESULT.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getPetIndex(pet));
		mplew.writeLong(pet.getUniqueId());// liPetSN
		mplew.write(pet.getExceptionList().size());
		for(int itemid : pet.getExceptionList()){
			mplew.writeInt(itemid);
		}
		return mplew.getPacket();
	}
}
