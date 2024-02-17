package tools.packets;

import java.util.Map;

import client.MapleKeyBinding;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 28, 2017
 */
public class FuncKeyMappedMan{

	public static byte[] getKeymap(Map<Integer, MapleKeyBinding> keybindings){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.KEYMAP.getValue());
		mplew.write(0);
		for(int x = 0; x < 90; x++){
			MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
			if(binding != null){
				mplew.write(binding.getType());
				mplew.writeInt(binding.getAction());
			}else{
				mplew.write(0);
				mplew.writeInt(0);
			}
		}
		return mplew.getPacket();
	}

	public static byte[] sendAutoHpPot(int itemId){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.AUTO_HP_POT.getValue());
		mplew.writeInt(itemId);
		return mplew.getPacket();
	}

	public static byte[] sendAutoMpPot(int itemId){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.AUTO_MP_POT.getValue());
		mplew.writeInt(itemId);
		return mplew.getPacket();
	}

	public static byte[] getQuickSlots(Map<Integer, MapleKeyBinding> keybindings, boolean defaultKeys){// CQuickslotKeyMappedMan::OnInit
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.QUICKSLOT_SET.getValue());
		mplew.writeBoolean(!defaultKeys);
		if(!defaultKeys){
			for(int x = 93; x <= 100; x++){
				MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
				if(binding != null){
					mplew.writeInt(binding.getAction());
				}else{
					mplew.writeInt(0);
				}
			}
		}
		return mplew.getPacket();
	}
}
