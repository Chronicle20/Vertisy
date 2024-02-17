/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;
import tools.Triple;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author iPoopMagic (David)
 *         b/c screw you SQL! & I love Singleton
 */
public class MakerItemFactory{

	private static final MakerItemFactory instance = new MakerItemFactory();
	private Map<Integer, MakerItemCreateEntry> makerItemCreateCache = new HashMap<>(); // Item Folder, MICE!
	private Map<Integer, GemCreateEntry> gemCreateCache = new HashMap<>(); // Item Folder, GCE!

	public static MakerItemFactory getInstance(){
		return instance;
	}

	public MakerItemFactory(){ // public or protected?
		File binFile = new File(System.getProperty("wzpath") + "/bin/ItemMake.bin");
		if(ServerConstants.WZ_LOADING){
			final MapleData info = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz")).getData("ItemMake.img");
			for(MapleData dataType : info.getChildren()){
				int type = Integer.parseInt(dataType.getName());
				switch (type){
					case 0:{ // Gems (created separate class so it's not as confusing) and other items that aren't equips
						for(MapleData itemFolder : dataType.getChildren()){
							int itemid = Integer.parseInt(itemFolder.getName());
							if(itemid == 1112664 || itemid == 1112664 || itemid == 1113149){
								loadMakerItem(itemFolder);
							}else{
								int reqLevel = MapleDataTool.getInt("reqLevel", itemFolder, 0);
								int reqMakerLevel = MapleDataTool.getInt("reqSkillLevel", itemFolder, 0);
								int quantity = MapleDataTool.getInt("itemNum", itemFolder, 0);
								int totalUpgradeCount = MapleDataTool.getInt("tuc", itemFolder, 0);
								int costMeso = MapleDataTool.getInt("meso", itemFolder, 0);
								GemCreateEntry gce = new GemCreateEntry(reqLevel, reqMakerLevel, quantity, totalUpgradeCount, costMeso);
								for(MapleData rewardAndRecipe : itemFolder.getChildren()){
									for(MapleData ind : rewardAndRecipe.getChildren()){
										switch (rewardAndRecipe.getName()){
											case "randomReward":
												gce.addRandomReward(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("itemNum", ind, 0), MapleDataTool.getInt("prob", ind, 0));
												break;
											case "recipe":
												gce.addReqItems(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("count", ind, 0));
												break;
										}
									}
								}
								gemCreateCache.put(itemid, gce);
							}
						}
						break;
					}
					case 1: // Warrior
					case 2: // Magician
					case 4: // Bowman
					case 8: // Thief
					case 16:{ // Pirate
						for(MapleData itemFolder : dataType.getChildren()){
							loadMakerItem(itemFolder);
						}
						break;
					}
				}
			}
			if(ServerConstants.BIN_DUMPING && !binFile.exists()){
				try{
					MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
					mplew.writeInt(makerItemCreateCache.size());
					for(int i : makerItemCreateCache.keySet()){
						mplew.writeInt(i);
						makerItemCreateCache.get(i).save(mplew);
					}
					mplew.writeInt(gemCreateCache.size());
					for(int i : gemCreateCache.keySet()){
						mplew.writeInt(i);
						gemCreateCache.get(i).save(mplew);
					}
					binFile.createNewFile();
					mplew.saveToFile(binFile);
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
		}else{
			try{
				byte[] in = Files.readAllBytes(binFile.toPath());
				ByteArrayByteStream babs = new ByteArrayByteStream(in);
				GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
				int size = glea.readInt();
				for(int i = 0; i < size; i++){
					int id = glea.readInt();
					MakerItemCreateEntry mice = new MakerItemCreateEntry();
					mice.load(glea);
					makerItemCreateCache.put(id, mice);
				}
				size = glea.readInt();
				for(int i = 0; i < size; i++){
					int id = glea.readInt();
					GemCreateEntry gce = new GemCreateEntry();
					gce.load(glea);
					gemCreateCache.put(id, gce);
				}
				glea = null;
				babs = null;
				in = null;
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	private void loadMakerItem(MapleData itemFolder){
		int reqLevel, reqMakerLevel, quantity, costMeso, totalUpgradeCount, catalyst;
		reqLevel = MapleDataTool.getInt("reqLevel", itemFolder, 0);
		reqMakerLevel = MapleDataTool.getInt("reqSkillLevel", itemFolder, 0);
		quantity = MapleDataTool.getInt("itemNum", itemFolder, 0);
		totalUpgradeCount = MapleDataTool.getInt("tuc", itemFolder, 0);
		costMeso = MapleDataTool.getInt("meso", itemFolder, 0);
		catalyst = MapleDataTool.getInt("catalyst", itemFolder, 0);
		MakerItemCreateEntry mice = new MakerItemCreateEntry(reqLevel, reqMakerLevel, quantity, totalUpgradeCount, costMeso, catalyst);
		for(MapleData recipe : itemFolder.getChildren()){
			for(MapleData ind : recipe.getChildren()){
				if(recipe.getName().equals("recipe")){
					mice.addReqItem(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("count", ind, 0));
				}
			}
		}
		makerItemCreateCache.put(Integer.parseInt(itemFolder.getName()), mice);
	}

	public MakerItemCreateEntry getMakerItemCreateInfo(Integer itemid){
		return makerItemCreateCache.get(itemid);
	}

	public static class MakerItemCreateEntry{

		private int reqLevel, reqMakerLevel, itemNum, tuc, meso, catalyst;
		private final List<Pair<Integer, Integer>> reqItems = new ArrayList<>();

		private MakerItemCreateEntry(){
			super();
		}

		private MakerItemCreateEntry(int reqLevel, int reqMakerLevel, int itemNum, int tuc, int meso, int catalyst){
			this.reqLevel = reqLevel;
			this.reqMakerLevel = reqMakerLevel;
			this.tuc = tuc;
			this.itemNum = itemNum;
			this.meso = meso;
			this.catalyst = catalyst;
		}

		public int getReqLevel(){
			return reqLevel;
		}

		public int getReqSkillLevel(){
			return reqMakerLevel;
		}

		public int getQuantity(){
			return itemNum;
		}

		public int getTUC(){
			return tuc;
		}

		public int getCost(){
			return meso;
		}

		public int getCatalyst(){
			return catalyst;
		}

		public List<Pair<Integer, Integer>> getReqItems(){
			return reqItems;
		}

		protected void addReqItem(int itemId, int amount){
			reqItems.add(new Pair<>(itemId, amount));
		}

		public void save(LittleEndianWriter lew){
			lew.writeInt(reqLevel);
			lew.writeInt(reqMakerLevel);
			lew.writeInt(itemNum);
			lew.writeInt(tuc);
			lew.writeInt(meso);
			lew.writeInt(catalyst);
			lew.writeInt(reqItems.size());
			for(Pair<Integer, Integer> p : reqItems){
				lew.writeInt(p.left);
				lew.writeInt(p.right);
			}
		}

		public void load(LittleEndianAccessor lea){
			reqLevel = lea.readInt();
			reqMakerLevel = lea.readInt();
			itemNum = lea.readInt();
			tuc = lea.readInt();
			meso = lea.readInt();
			catalyst = lea.readInt();
			int size = lea.readInt();
			for(int i = 0; i < size; i++){
				reqItems.add(new Pair<Integer, Integer>(lea.readInt(), lea.readInt()));
			}
		}
	}

	public GemCreateEntry getGemCreateInfo(Integer itemid){
		return gemCreateCache.get(itemid);
	}

	public static class GemCreateEntry{ // Just to avoid confusion, Maker Items don't have catalyst

		private int reqLevel, reqMakerLevel, itemNum, tuc, meso;
		private final List<Triple<Integer, Integer, Integer>> randomReward = new ArrayList<>();
		private final List<Pair<Integer, Integer>> recipe = new ArrayList<>();

		private GemCreateEntry(){
			super();
		}

		private GemCreateEntry(int reqLevel, int reqMakerLevel, int itemNum, int tuc, int meso){
			this.reqLevel = reqLevel;
			this.reqMakerLevel = reqMakerLevel;
			this.tuc = tuc;
			this.itemNum = itemNum;
			this.meso = meso;
		}

		public int getReqLevel(){
			return reqLevel;
		}

		public int getReqSkillLevel(){
			return reqMakerLevel;
		}

		public int getQuantity(){
			return itemNum;
		}

		public int getTUC(){
			return tuc;
		}

		public int getCost(){
			return meso;
		}

		public List<Triple<Integer, Integer, Integer>> getRandomReward(){
			return randomReward;
		}

		public List<Pair<Integer, Integer>> getReqItems(){
			return recipe;
		}

		protected void addRandomReward(int itemId, int itemNum, int prob){
			randomReward.add(new Triple<>(itemId, itemNum, prob));
		}

		protected void addReqItems(int itemId, int count){
			recipe.add(new Pair<>(itemId, count));
		}

		public void save(LittleEndianWriter lew){
			lew.writeInt(reqLevel);
			lew.writeInt(reqMakerLevel);
			lew.writeInt(itemNum);
			lew.writeInt(tuc);
			lew.writeInt(meso);
			lew.writeInt(randomReward.size());
			for(Triple<Integer, Integer, Integer> t : randomReward){
				lew.writeInt(t.left);
				lew.writeInt(t.mid);
				lew.writeInt(t.right);
			}
			lew.writeInt(recipe.size());
			for(Pair<Integer, Integer> p : recipe){
				lew.writeInt(p.left);
				lew.writeInt(p.right);
			}
		}

		public void load(LittleEndianAccessor lea){
			reqLevel = lea.readInt();
			reqMakerLevel = lea.readInt();
			itemNum = lea.readInt();
			tuc = lea.readInt();
			meso = lea.readInt();
			int size = lea.readInt();
			for(int i = 0; i < size; i++){
				randomReward.add(new Triple<Integer, Integer, Integer>(lea.readInt(), lea.readInt(), lea.readInt()));
			}
			size = lea.readInt();
			for(int i = 0; i < size; i++){
				recipe.add(new Pair<Integer, Integer>(lea.readInt(), lea.readInt()));
			}
		}
	}
}
