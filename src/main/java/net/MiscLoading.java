package net;

import java.io.File;
import java.nio.file.Files;

import client.command.normal.CommandMusic;
import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Apr 10, 2017
 */
public class MiscLoading{

	public static void load(){
		//
		File binFolder = new File(System.getProperty("wzpath") + "/bin/");
		File music = new File(binFolder, "Music.bin");
		if(ServerConstants.WZ_LOADING){
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			MapleDataProvider soundData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Sound.wz"));
			for(MapleDataFileEntry mdfe : soundData.getRoot().getFiles()){
				if(mdfe.getName().equals("Weapon.img") || mdfe.getName().equals("CashEffect.img") || mdfe.getName().equals("Pet.img") || mdfe.getName().equals("Mob.img") || mdfe.getName().equals("Item.img") || mdfe.getName().equals("Skill.img") || mdfe.getName().equals("Reactor.img")){
					continue;
				}
				MapleData data = soundData.getData(mdfe.getName());
				for(MapleData option : data.getChildren()){
					CommandMusic.sound.add(mdfe.getName() + "/" + option.getName());
				}
			}
			if(ServerConstants.BIN_DUMPING){
				mplew.writeInt(CommandMusic.sound.size());
				for(String path : CommandMusic.sound){
					mplew.writeMapleAsciiString(path);
				}
				if(!music.exists()){
					try{
						music.createNewFile();
						mplew.saveToFile(music);
						mplew = null;
					}catch(Exception e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					}
				}
			}
		}else{
			try{
				if(music.exists()){
					byte[] in = Files.readAllBytes(music.toPath());
					ByteArrayByteStream babs = new ByteArrayByteStream(in);
					GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
					int size = glea.readInt();
					for(int i = 0; i < size; i++){
						CommandMusic.sound.add(glea.readMapleAsciiString());
					}
					glea = null;
					babs = null;
					in = null;
				}else{
					System.out.println("No music to load, skipping.");
				}
			}catch(Exception e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}
}
