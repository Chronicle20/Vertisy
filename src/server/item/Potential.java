package server.item;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import client.inventory.Item;
import provider.MapleData;
import provider.MapleDataTool;
import server.ItemInformationProvider;
import tools.ObjectParser;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 27, 2017
 */
public class Potential{

	public short id;
	// info
	public int optionType, reqLevel;
	public Map<Integer, PotentialLevelData> levelData = new HashMap<>();

	public void load(MapleData data){
		id = ObjectParser.isShort(data.getName()).shortValue();
		MapleData info = data.getChildByPath("info");
		if(info != null){
			optionType = MapleDataTool.getInt("optionType", info, 0);// Default?
			reqLevel = MapleDataTool.getInt("reqLevel", info, 0);
		}
		MapleData level = data.getChildByPath("level");
		for(MapleData l : level.getChildren()){
			PotentialLevelData pld = new PotentialLevelData();
			pld.load(l);
			levelData.put(ObjectParser.isInt(l.getName()).intValue(), pld);
		}
	}

	public void load(LittleEndianAccessor lea){
		id = lea.readShort();
		optionType = lea.readInt();
		reqLevel = lea.readInt();
		int size = lea.readInt();
		for(int i = 0; i < size; i++){
			int level = lea.readInt();
			PotentialLevelData pld = new PotentialLevelData();
			pld.load(lea);
			levelData.put(level, pld);
		}
	}

	public void save(LittleEndianWriter lew){
		lew.writeShort(id);
		lew.writeInt(optionType);
		lew.writeInt(reqLevel);
		lew.writeInt(levelData.size());
		for(Entry<Integer, PotentialLevelData> entry : levelData.entrySet()){
			lew.writeInt(entry.getKey());
			entry.getValue().save(lew);
		}
	}

	public PotentialLevelData getLevelData(Item item){
		double reqLevel = ItemInformationProvider.getInstance().getItemData(item.getItemId()).reqLevel;
		int level = (int) (reqLevel / 10);
		if(reqLevel == 0) level = 1;
		return levelData.get(level);
	}
}
