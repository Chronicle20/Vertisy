package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import constants.ServerConstants;
import provider.*;
import tools.ObjectParser;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Apr 4, 2016
 */
public class MapleCharacterInfo{

	private static MapleCharacterInfo instance;
	protected MapleDataProvider character;
	protected MapleDataProvider stringData;
	private static Map<Integer, String> hairs = new HashMap<>();
	private static Map<Integer, String> faces = new HashMap<>();

	private MapleCharacterInfo(){
		//
		File hairDat = new File(System.getProperty("wzpath") + "/bin/Hair.bin");
		File faceDat = new File(System.getProperty("wzpath") + "/bin/Face.bin");
		try{
			if((!hairDat.exists() || !faceDat.exists()) && ServerConstants.WZ_LOADING){
				character = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
				stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
				MapleData itemsData = stringData.getData("Eqp.img").getChildByPath("Eqp");
				MaplePacketLittleEndianWriter faceBin = new MaplePacketLittleEndianWriter();
				MaplePacketLittleEndianWriter hairBin = new MaplePacketLittleEndianWriter();
				for(MapleData eqpType : itemsData.getChildren()){
					if(eqpType.getName().equalsIgnoreCase("Face")){
						if(!faceDat.exists()){
							for(MapleData itemFolder : eqpType.getChildren()){
								int faceID = Integer.parseInt(itemFolder.getName());
								String faceName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
								faces.put(faceID, faceName);
								faceBin.writeInt(faceID);
								faceBin.writeMapleAsciiString(faceName);
							}
						}
					}else if(eqpType.getName().equalsIgnoreCase("Hair")){
						if(!hairDat.exists()){
							for(MapleData itemFolder : eqpType.getChildren()){
								int hairID = Integer.parseInt(itemFolder.getName());
								String hairName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
								hairs.put(hairID, hairName);
								hairBin.writeInt(hairID);
								hairBin.writeMapleAsciiString(hairName);
							}
						}
					}
				}
				if(!hairDat.exists()){
					for(MapleDataDirectoryEntry mdde : character.getRoot().getSubdirectories()){
						if(mdde.getName().equals("Face") && !faceDat.exists()){
							for(MapleDataFileEntry mdfe : mdde.getFiles()){
								Integer faceID = ObjectParser.isInt(mdfe.getName().substring(0, mdfe.getName().length() - 4));
								if(!faces.containsKey(faceID.intValue())){
									// System.out.println("adding face: " + faceID);
									faces.put(faceID.intValue(), "NO-NAME");
									faceBin.writeInt(faceID);
									faceBin.writeMapleAsciiString("NO-NAME");
								}
							}
						}else if(mdde.getName().equals("Hair") && !hairDat.exists()){
							for(MapleDataFileEntry mdfe : mdde.getFiles()){
								Integer hairID = ObjectParser.isInt(mdfe.getName().substring(0, mdfe.getName().length() - 4));
								if(!hairs.containsKey(hairID.intValue())){
									// System.out.println("adding hair: " + hairID);
									hairs.put(hairID.intValue(), "NO-NAME");
									hairBin.writeInt(hairID);
									hairBin.writeMapleAsciiString("NO-NAME");
								}
							}
						}
					}
				}
				if(ServerConstants.BIN_DUMPING){
					faceDat.createNewFile();
					faceBin.saveToFile(faceDat);
					hairDat.createNewFile();
					hairBin.saveToFile(hairDat);
				}
				stringData = null;
			}else{
				if(faceDat.exists()){
					byte[] in = Files.readAllBytes(faceDat.toPath());
					ByteArrayByteStream babs = new ByteArrayByteStream(in);
					GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
					while(glea.available() > 0){
						int faceID = glea.readInt();
						String faceName = glea.readMapleAsciiString();
						faces.put(faceID, faceName);
					}
					glea = null;
					babs = null;
					in = null;
				}
				if(hairDat.exists()){
					try{
						byte[] in = Files.readAllBytes(hairDat.toPath());
						ByteArrayByteStream babs = new ByteArrayByteStream(in);
						GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
						while(glea.available() > 0){
							int hairID = glea.readInt();
							String hairName = glea.readMapleAsciiString();
							hairs.put(hairID, hairName);
						}
						glea = null;
						babs = null;
						in = null;
					}catch(IOException e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					}
				}
			}
		}catch(IOException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		System.out.println("Loaded " + hairs.size() + " hairs");
		System.out.println("Loaded " + faces.size() + " faces");
	}

	public static MapleCharacterInfo getInstance(){
		if(instance == null){
			instance = new MapleCharacterInfo();
		}
		return instance;
	}

	public Map<Integer, String> getHairs(){
		return hairs;
	}

	public Collection<String> getHairNames(){
		return hairs.values();
	}

	public String getHairNameById(int hairid){
		return hairs.get(hairid);
	}

	public Integer getHairIdByName(String hairName){
		for(int id : hairs.keySet()){
			if(hairs.get(id).equalsIgnoreCase(hairName)) return id;
		}
		return null;
	}

	public Map<Integer, String> getFaces(){
		return faces;
	}
}
