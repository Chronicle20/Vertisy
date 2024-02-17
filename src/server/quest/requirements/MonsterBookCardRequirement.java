package server.quest.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import client.MapleCharacter;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import tools.Pair;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class MonsterBookCardRequirement extends MapleQuestRequirement{

	List<Pair<Integer, Integer>> cards = new ArrayList<>();

	public MonsterBookCardRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.MB_CARD);
		processData(data);
	}

	public MonsterBookCardRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.MB_CARD);
		processData(lea);
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		for(MapleData entry : data.getChildren()){
			cards.add(new Pair<Integer, Integer>(MapleDataTool.getInt("id", entry), MapleDataTool.getInt("min", entry)));
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int total = lea.readInt();
		for(int i = 0; i < total; i++){
			cards.add(new Pair<Integer, Integer>(lea.readInt(), lea.readInt()));
		}
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(cards.size());
		for(Pair<Integer, Integer> p : cards){
			lew.writeInt(p.left);
			lew.writeInt(p.right);
		}
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		boolean good = true;
		for(Pair<Integer, Integer> reqCard : cards){
			boolean g = false;
			for(Entry<Integer, Integer> all : chr.getMonsterBook().getCards().entrySet()){
				if(all.getKey() == reqCard.getLeft()){
					if(all.getValue() >= reqCard.getRight()){
						g = true;
					}
				}
			}
			if(!g) good = false;
		}
		return good;
	}
}
