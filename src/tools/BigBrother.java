package tools;

import java.util.Arrays;
import java.util.List;

import client.MapleCharacter;
import client.inventory.Item;
import net.server.guild.MapleGuild;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.ItemInformationProvider;
import server.MapleTrade;
import server.expeditions.MapleExpedition;
import server.maps.objects.MapleMapObject;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class BigBrother{

	// TODO:
	public static void general(String message, String source, List<MapleMapObject> recipents){
		Logger.log(LogType.INFO, LogFile.GENERAL_CHAT, source, message.replace("%", "%%"));
		/*for(Channel ch : Server.getInstance().getAllChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.isGM() && player.bigBrother()){
					if(player.getClient().getChannel() == source.getClient().getChannel() && player.getMapId() == source.getMapId()) return;// If GM and source is on same map AND same channel, ignore to avoid double messages
					if(player.getBigBrotherMonitors().size() > 0){
						boolean found = false;
						for(MapleMapObject mpo : recipents){
							MapleCharacter chr = (MapleCharacter) mpo;
							if(player.isBigBrotherMonitoring(chr.getId())) found = true;
						}
						if(player.isBigBrotherMonitoring(source.getId())) found = true;
						if(!found) return;
					}
					player.announce(MaplePacketCreator.serverNotice(5, "[" + source.getMap().getMapData().getMapName() + "] " + source.getName() + ":" + message));
				}
			}
		}*/
	}

	public static void whisper(String message, String source, String destination){
		Logger.log(LogType.INFO, LogFile.WHISPER, source, "Whisper(" + destination + "): " + message.replace("%", "%%"));
		/*for(Channel ch : Server.getInstance().getAllChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.isGM() && player.bigBrother()){
					if(player.equals(destination) || player.equals(source)) return; // To avoid double whispers
					if(player.getBigBrotherMonitors().size() > 0){
						if(!player.isBigBrotherMonitoring(source.getId()) && !player.isBigBrotherMonitoring(destination.getId())) return;
					}
					player.announce(MaplePacketCreator.getWhisper(source.getName(), source.getClient().getChannel(), message));
				}
			}
		}*/
	}

	public static void buddy(int[] recipientCharacterIds, String source, String chattext){
		Logger.log(LogType.INFO, LogFile.BUDDY_CHAT, source, "Recipients: " + Arrays.toString(recipientCharacterIds) + "\r\n" + chattext.replace("%", "%%"));
		/*for(Channel ch : Server.getInstance().getAllChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.isGM() && player.bigBrother()){
					boolean monitored = false;
					if(source.getId() == player.getId()) return; // To avoid double buddy chats
					for(int id : recipientCharacterIds){
						if(id == player.getId()) return; // To avoid double buddy chats
						if(player.getBigBrotherMonitors().size() > 0){
							if(player.isBigBrotherMonitoring(id)) monitored = true;
						}
					}
					if(player.getBigBrotherMonitors().size() > 0){
						if(player.isBigBrotherMonitoring(source.getId())) monitored = true;
						if(!monitored) return;
					}
					player.announce(MaplePacketCreator.multiChat(source.getName(), "[BB]: " + chattext, 0));
				}
			}
		}*/
	}

	public static void guild(MapleGuild guild, String source, String message){
		Logger.log(LogType.INFO, LogFile.GUILD_CHAT, guild.getName(), source + ":" + message.replace("%", "%%"));
		/*for(Channel ch : Server.getInstance().getAllChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.isGM() && player.bigBrother()){
					if(player.getGuild() != null && player.getGuild().getName().equalsIgnoreCase(guild.getName())) return; // Avoid same-guild messages
					if(player.getBigBrotherMonitors().size() > 0){
						boolean monitored = false;
						if(player.isBigBrotherMonitoring(source.getId())) monitored = true;
						for(MapleGuildCharacter mgc : guild.getMembers()){
							if(mgc.isOnline() && player.isBigBrotherMonitoring(mgc.getId())) monitored = true;
						}
						if(!monitored) return;
					}
					player.announce(MaplePacketCreator.serverNotice(5, "[" + guild.getName() + "] " + source.getName() + ":" + message));
				}
			}
		}*/
	}

	public static void party(MapleParty party, String chattext, String source){
		String partyMembers = "";
		for(MaplePartyCharacter mpc : party.getMembers()){
			partyMembers += mpc.getName() + ", ";
		}
		Logger.log(LogType.INFO, LogFile.PARTY_CHAT, source, "Party: " + chattext.replace("%", "%%") + "\r\n" + partyMembers);
		/*for(Channel ch : Server.getInstance().getAllChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.isGM() && player.bigBrother()){
					boolean monitored = false;
					for(MaplePartyCharacter partyMember : party.getMembers()){
						if(partyMember.getPlayer().equals(player)){ // Prevent same-party messages
							return;
						}
						if(player.getBigBrotherMonitors().size() > 0){
							if(partyMember.isOnline() && player.isBigBrotherMonitoring(partyMember.getId())) monitored = true;
						}
					}
					if(player.getBigBrotherMonitors().size() > 0 && !monitored) return;
					player.announce(MaplePacketCreator.multiChat(source.getName(), "[PB]: " + chattext, 1));
				}
			}
		}*/
	}

	public static void trade(MapleCharacter chr, MapleCharacter chr2, String message){
		Logger.log(LogType.INFO, LogFile.TRADE_CHAT, chr.getName(), "Trade: [" + chr.getName() + ">" + chr2.getName() + "]: " + message.replace("%", "%%"));
		/*for(Channel ch : Server.getInstance().getAllChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.isGM() && player.bigBrother()){
					if(player.equals(chr) || player.equals(chr2)) return; // You're in the trade itself
					if(player.getBigBrotherMonitors().size() > 0){
						if(!player.isBigBrotherMonitoring(chr.getId()) && !player.isBigBrotherMonitoring(chr2.getId())) return;
					}
					player.announce(MaplePacketCreator.serverNotice(5, "[TRADE]: [" + chr.getName() + ">" + chr2.getName() + "]: " + message));
				}
			}
		}*/
	}

	public static void messenger(String source, String to1, String to2, String message){
		if(message.equals(source + "0") || message.equals(source + "1")) return;
		Logger.log(LogType.INFO, LogFile.MESSENGER_CHAT, source, "Messenger: [" + to1 + "|" + to2 + "]: " + message.replace("%", "%%"));
		/*for(Channel ch : Server.getInstance().getAllChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.isGM() && player.bigBrother()){
					if(player.getName().equals(source.getName()) || player.getName().equals(to1) || player.getName().equals(to2)) return; // You're in the chat itself
					player.announce(MaplePacketCreator.serverNotice(5, "[MESSENGER]: [" + to1 + "|" + to2 + "]: " + message));
				}
			}
		}*/
	}

	public static void logTrade(MapleTrade trade1, MapleTrade trade2){
		String name1 = trade1.getChr().getName();
		String name2 = trade2.getChr().getName();
		String log = "TRADE BETWEEN " + name1 + " AND " + name2 + "\r\n";
		// Trade 1 to trade 2
		log += trade1.getExchangeMesos() + " mesos from " + name1 + " to " + name2 + " \r\n";
		for(Item item : trade1.getItems()){
			log += item.getQuantity() + " " + item.getItemId() + " from " + name1 + " to " + name2 + " \r\n";
		}
		// Trade 2 to trade 1
		log += trade2.getExchangeMesos() + " mesos from " + name2 + " to " + name1 + " \r\n";
		for(Item item : trade2.getItems()){
			log += item.getQuantity() + " " + item.getItemId() + " from " + name2 + " to " + name1 + " \r\n";
		}
		log += "\r\n\r\n";
		Logger.log(LogType.INFO, LogFile.TRADE, name1, log);
		Logger.log(LogType.INFO, LogFile.TRADE, name2, log);
	}

	public static void logExpedition(MapleExpedition expedition){
		// Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, expedition.getType().toString() + " Expedition with leader " + expedition.getLeader().getName() + " finished after " + getTimeString(expedition.getStartTime())));
		String log = expedition.getType().toString() + " EXPEDITION\r\n";
		log += getTimeString(expedition.getStartTime()) + "\r\n";
		for(MapleCharacter member : expedition.getMembers()){
			log += ">>" + member.getName() + "\r\n";
		}
		log += "BOSS KILLS\r\n";
		for(String message : expedition.getBossLogs()){
			log += message;
		}
		log += "\r\n\r\n";
		Logger.log(LogType.INFO, LogFile.EXPEDITIONS, log);
	}

	public static String getTimeString(long then){
		long duration = System.currentTimeMillis() - then;
		int seconds = (int) (duration / 1000) % 60;
		int minutes = (int) ((duration / (1000 * 60)) % 60);
		return minutes + " Minutes and " + seconds + " Seconds";
	}

	public static void logGacha(MapleCharacter player, int itemid, String map){
		String itemName = ItemInformationProvider.getInstance().getItemData(itemid).name.replace("%", "%%");
		Logger.log(LogType.INFO, LogFile.ITEM_GAIN_GACH, player.getClient().getAccountName(), "%s got a %s (%s) from the %s gachapon.", player.getName(), itemName, itemid, map);
	}
}
