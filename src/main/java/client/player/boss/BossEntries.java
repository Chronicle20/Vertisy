package client.player.boss;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 18, 2017
 */
public class BossEntries{

	private Map<BossEntryType, List<Long>> bossEntries = new HashMap<>();

	/**
	 * @return Index of the next available entry, -1 if no entry index is available..
	 */
	public byte getEntryIndex(BossEntryType type){
		List<Long> entries = bossEntries.get(type);
		if(entries == null || entries.isEmpty()) return 0;
		long current = System.currentTimeMillis();
		byte pos = 0;
		for(long end : entries){
			if(end + (type.getReset() * 60 * 60 * 1000L) <= current) return pos;
			pos++;
		}
		if(entries.size() < type.getEntries()) return pos;
		return -1;
	}

	/**
	 * @return If an available entry exists.
	 */
	public boolean hasAvailableEntry(BossEntryType type){
		return getEntryIndex(type) >= 0;
	}

	/**
	 * @return Earliest time an entry will be available.
	 */
	public long getNextEntryTime(BossEntryType type){
		List<Long> entries = bossEntries.get(type);
		if(entries == null || entries.isEmpty()) return -1;
		long shortest = -1;
		for(long time : bossEntries.get(type)){
			if(shortest == -1 || time < shortest) shortest = time;
		}
		return shortest;
	}

	/**
	 * Adds an entry. Index is grabbed using {@link BossEntries#getEntryIndex(BossEntryType)}
	 * 
	 * @return If the entry was successfully added
	 */
	public boolean addEntry(BossEntryType type){
		byte index = getEntryIndex(type);
		if(index == -1) return false;
		else{
			addEntry(type, index);
			return true;
		}
	}

	public void addEntry(BossEntryType type, byte index){
		List<Long> entries = bossEntries.get(type);
		if(entries == null) entries = new LinkedList<>();
		// System.out.println(entries.size() + " < " + type.getEntries());
		if(entries.size() < type.getEntries()) entries.add(index, System.currentTimeMillis());
		else entries.set(index, System.currentTimeMillis());
		bossEntries.put(type, entries);
	}

	public void loadFromTable(ResultSet rs) throws SQLException{
		String bossEntryString = rs.getString("bossEntries");
		if(bossEntryString != null && !bossEntryString.isEmpty()){
			for(String bossEntry : bossEntryString.split(",")){
				String[] split = bossEntry.split("=");
				List<Long> entries = new LinkedList<>();
				for(String entryTimeS : split[1].split("\\.")){
					if(entryTimeS != null && !entryTimeS.isEmpty()) entries.add(ObjectParser.toLong(entryTimeS));
				}
				bossEntries.put(BossEntryType.valueOf(split[0]), entries);
			}
		}
	}

	public void saveToTable(PreparedStatement ps, int index) throws SQLException{
		String bossEntryString = "";
		for(BossEntryType type : bossEntries.keySet()){
			bossEntryString += type.name();
			bossEntryString += "=";
			for(long entryTime : bossEntries.get(type)){
				bossEntryString += entryTime;
				bossEntryString += ".";
			}
			bossEntryString += ",";
		}
		ps.setString(index, bossEntryString);
	}
}
