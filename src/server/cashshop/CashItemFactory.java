package server.cashshop;

import java.io.File;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.inventory.Item;
import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import tools.DatabaseConnection;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 13, 2017
 */
public class CashItemFactory{

	private static final Map<Integer, CashItemData> items = new HashMap<>();
	private static final Map<Integer, List<Integer>> packages = new HashMap<>();
	private static final List<CashItemData> modifiedItems = new ArrayList<>();
	private static final List<Integer> permanentItems = new ArrayList<>();
	public static final List<LimitedGood> limitedGoods = new ArrayList<>();
	public static final List<CategoryDiscount> categoryDiscount = new ArrayList<>();
	public static final Map<Integer, BestItem> bestItems = new HashMap<>();
	static{
		try{
			File binBase = new File(System.getProperty("wzpath") + "/bin");
			binBase.mkdirs();
			File commodity = new File(binBase, "/Commodity.bin");
			if(ServerConstants.WZ_LOADING){
				MapleDataProvider etc = MapleDataProviderFactory.getDataProvider(new File("wz/Etc.wz"));
				MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
				for(MapleData item : etc.getData("Commodity.img").getChildren()){
					CashItemData data = new CashItemData();
					for(MapleData id : item.getChildren()){
						data.load(id);
					}
					items.put(data.sn, data);
					if(data.nPeriod == 0){
						data.bOnSale = false;
						data.modifier |= CashModification.ONSALE;
						modifiedItems.add(data);
						permanentItems.add(data.sn);
					}
					if(ServerConstants.BIN_DUMPING) data.saveToBin(mplew);
				}
				if(ServerConstants.BIN_DUMPING){
					commodity.createNewFile();
					mplew.saveToFile(commodity);
				}
				for(MapleData cashPackage : etc.getData("CashPackage.img").getChildren()){
					List<Integer> cPackage = new ArrayList<>();
					for(MapleData item : cashPackage.getChildByPath("SN").getChildren()){
						cPackage.add(Integer.parseInt(item.getData().toString()));
					}
					packages.put(Integer.parseInt(cashPackage.getName()), cPackage);
				}
				if(ServerConstants.BIN_DUMPING){
					File binFile = new File(binBase, "/CashPackage.bin");
					if(!binFile.exists()){
						mplew = new MaplePacketLittleEndianWriter();
						mplew.writeInt(packages.size());
						for(int id : packages.keySet()){
							mplew.writeInt(id);
							List<Integer> ids = packages.get(id);
							mplew.writeInt(ids.size());
							for(int i : ids){
								mplew.writeInt(i);
							}
						}
						binFile.createNewFile();
						mplew.saveToFile(binFile);
					}
				}
			}else{
				if(commodity.exists()){
					byte[] in = Files.readAllBytes(commodity.toPath());
					ByteArrayByteStream babs = new ByteArrayByteStream(in);
					GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
					while(glea.available() > 0){
						CashItemData cashItem = new CashItemData();
						cashItem.load(glea);
						items.put(cashItem.sn, cashItem);
						if(cashItem.nPeriod == 0){
							cashItem.bOnSale = false;
							cashItem.modifier |= CashModification.ONSALE;
							modifiedItems.add(cashItem);
							permanentItems.add(cashItem.sn);
						}
					}
				}
				File binFile = new File(binBase, "/CashPackage.bin");
				if(binFile.exists()){
					byte[] in = Files.readAllBytes(binFile.toPath());
					ByteArrayByteStream babs = new ByteArrayByteStream(in);
					GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
					int packageSize = glea.readInt();
					for(int i = 0; i < packageSize; i++){
						int id = glea.readInt();
						int size = glea.readInt();
						List<Integer> cPackage = new ArrayList<>();
						for(int ii = 0; ii < size; ii++){
							cPackage.add(glea.readInt());
						}
						packages.put(id, cPackage);
					}
				}
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		reloadModifiedCashItems();
	}

	public static List<CashItemData> getCashItems(){
		return new ArrayList<>(items.values());
	}

	public static CashItemData getItem(int sn){
		return items.get(sn);
	}

	public static List<Item> getPackage(int itemId){
		List<Item> cashPackage = new ArrayList<>();
		for(int sn : packages.get(itemId)){
			cashPackage.add(getItem(sn).toItem());
		}
		return cashPackage;
	}

	public static boolean isPackage(int itemId){
		return packages.containsKey(itemId);
	}

	public static List<CashItemData> getModifiedCommodity(){
		return modifiedItems;
	}

	public static LimitedGood getGoodFromSN(int targetSN){
		for(LimitedGood lg : CashItemFactory.limitedGoods){
			for(int sn : lg.nSN){
				if(lg.getState() == 0){
					if(sn == targetSN) return lg;
				}
			}
		}
		return null;
	}

	public static void addBestItem(CashItemData item){
		synchronized(bestItems){
			BestItem bItem = bestItems.get(item.sn);
			if(bItem == null){
				bItem = new BestItem();
				bItem.sn = item.sn;
				bItem.nCount = item.nCount;
				bItem.nCommodityGender = item.nCommodityGender;
			}
			bItem.nCount += item.nCount;
			bestItems.put(item.sn, bItem);
		}
	}

	public static void reloadModifiedCashItems(){
		bestItems.clear();
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM bestitems")){
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					BestItem bi = new BestItem();
					bi.load(rs);
					bestItems.put(bi.sn, bi);
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		modifiedItems.clear();
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM modifiedcashitems")){
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					CashItemData cmd = new CashItemData();
					cmd.load(rs);
					modifiedItems.add(cmd);
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		for(int sn : permanentItems){
			CashItemData data = getItem(sn);
			data.bOnSale = false;
			data.modifier |= CashModification.ONSALE;
			modifiedItems.add(data);
		}
		limitedGoods.clear();
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM limitedgoods")){
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					LimitedGood lg = new LimitedGood();
					lg.load(rs);
					limitedGoods.add(lg);
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		categoryDiscount.clear();
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM cs_categorydiscount")){
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					CategoryDiscount cd = new CategoryDiscount();
					cd.load(rs);
					categoryDiscount.add(cd);
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public static int get_category_from_SN(int nSN){
		return nSN / 10000000 % 10;
	}

	public static int get_categorysub_from_SN(int nSN){
		return nSN / 100000 % 100;
	}
}