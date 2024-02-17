/**
 * @Name: Mark of the Squad
 * @Map: Entrance to Horntail's Cave
 * @Description: Horntail Entrance 
 * @Author: iPoopMagic (David)
 */

var status = -1;
var expedition;
var em;
var amtFought;

function start() {
	expedition = cm.getExpedition(Packages.server.expeditions.MapleExpeditionType.HORNTAIL);	
	em = cm.getEventManager("HorntailFight");

	if (cm.getPlayer().getLevel() < 80) {
		cm.sendOk("There is a level requirement of 80 to attempt the Horntail.");
		cm.dispose();
		return;
	}
	if (cm.getPlayer().getClient().getChannel() != 1) {
		cm.sendOk("Horntail may only be attempted on Channel 1.");
		cm.dispose();
		return;
	}
	if (em == null) {
		cm.sendOk("The event hasn't started, please contact a GM.");
		cm.dispose();
		return;
	}
	var bossQuest = cm.getQuestRecord(30000);
	var bossQuest1 = cm.getQuestRecord(30001);
    var timeData = bossQuest.getTimeData();
	var customData = bossQuest1.getCustomData();
    if (timeData == null) {
        bossQuest.setTimeData("0");
        timeData = "0";
    }
    var time = parseInt(timeData);
	if (customData == null) {
		bossQuest1.setCustomData("0");
        customData = "0";
	}
	amtFought = parseInt(customData);
	if (time + (24 * 60 * 60 * 1000) <= cm.getCurrentTime()) {
		amtFought = 0;
	}
	if (amtFought > 1 && time + (24 * 60 * 60 * 1000) >= cm.getCurrentTime() /*&& !cm.getPlayer().isGM()*/) {
		cm.sendOk("You may only attempt the Horntail Battle twice a day. Time left until allowed re-entry: " + cm.getReadableMillis(cm.getCurrentTime(), time + (12 * 60 * 60 * 1000)));
		cm.dispose();
		return;
	}
	var eim_status = em.getProperty("state");
	if (eim_status == null || eim_status.equals("0")) {
		if (expedition == null) { // Start expedition
			status = 0;
			cm.sendYesNo("Are you interested in becoming the leader of the expedition?");
		} else if (expedition.isLeader(cm.getPlayer())) { // Only the leader sees this
			status = 1;
			cm.sendSimple("What do you want to do, expedition leader? \r\n#b#L0#View current expedition members.#l\r\n#L1#Start the fight!#l\r\n#L2#Stop the expedition.#l");
		} else if (expedition.isRegistering()) { // Current expedition is registering
			status = 2;
			cm.sendSimple("What would you like to do? " + (expedition.contains(cm.getPlayer()) ? "\r\n#b#L1#Leave the expedition.#l " : "\r\n#b#L0#Join the expedition.#l") + "\r\n#b#L2#See the list of members in the expedition.#l");
		} else if (expedition.isInProgress()) { // This should never happen
			cm.sendOk("I'm afraid another expedition is attempting to defeat Horntail. Please wait until they have finished trying.");
			cm.dispose();
		}
	} else if (expedition.isInProgress() || eim_status.equals("1")) {
		if (expedition.contains(cm.getPlayer())) { //If you're registered, warp you in
			status = 3;
			cm.sendNext("The expedition has begun! You will now be escorted into the #bCave of Trial I#k. Good luck.");
		} else {
			cm.sendOk("I'm afraid another expedition is attempting to defeat Horntail. Please wait until they have finished trying.");
			cm.dispose();
			return;
		}
	}
}

function action(mode, type, selection) {
    switch (status) {
        case 0: // Register leader and expedition
            if (mode == 1) {
				cm.createExpedition(MapleExpeditionType.HORNTAIL);
            }
            cm.dispose();
            break;
        case 1:
			if (selection == 0) { // View current members
				if (expedition == null) {
					cm.sendOk("The expedition could not be loaded.");
					cm.dispose();
					return;
				}
				var size = expedition.getMembers().size();
				if (size == 1) {
					cm.sendOk("You are the only member of the expedition.");
					cm.dispose();
					return;
				}
				var text = "The following members make up your expedition (click on them to expel them):\r\n";
				text += "\r\n\t\t1." + expedition.getLeader().getName();
				for (var i = 1; i < size; i++) {
					text += "\r\n#b#L" + (i + 1) + "#" + (i + 1) + ". " + expedition.getMembers().get(i).getName() + "#l\r\n";
				}
				cm.sendSimple(text);
				status = 5;
			} else if (selection == 1) { // Start the fight
				cm.sendOk("The expedition will begin and you will now be escorted into the #bCave of Trial I#k.");
				status = 6;
			} else if (selection == 2) { // Stop the expedition
				cm.getPlayer().getMap().broadcastMessage(Packages.tools.MaplePacketCreator.removeClock());
				cm.getPlayer().getMap().broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(6, expedition.getLeader().getName() + " has ended the expedition."));
				cm.endExpedition(expedition);
				cm.sendOk("The expedition has now ended. Too scared to fight?");
				cm.dispose();
				return;
			}
			break;
		case 2:
			if (selection == 0) { // Add player to expedition
				if (expedition.contains(cm.getPlayer())) { // Player is already registered & needs to wait
					cm.sendOk("You have already registered for the expedition. Please wait for " + expedition.getLeader().getName() + " to begin the expedition.");
					cm.dispose();
					return;
				}
				cm.sendOk(expedition.addMember(cm.getPlayer()));
				cm.dispose();
			} else if (selection == 1) { // Remove player from expedition
				if (expedition.removeMember(cm.getPlayer())) {
					cm.sendOk("You have been removed from the expedition successfully.");
				}
				cm.dispose();
			} else if (selection == 2) { // Display list of expedition members
				var size = expedition.getMembers().size();
				var text = "The following members make up your expedition:\r\n";
				text += "\r\n\t\t#b1." + expedition.getLeader().getName();
				for (var i = 1; i < size; i++) {
					text += "\r\n/t/t" + (i + 1) + ". " + expedition.getMembers().get(i).getName() + "\r\n";
				}
				cm.sendOk(text);
				cm.dispose();
			}
			break;
        case 3: // Player is in expedition: warp them back in
			em.getInstance("HorntailFight_" + cm.getPlayer().getClient().getChannel()).registerPlayer(cm.getPlayer());
			cm.dispose();
            break;
		case 5: // Leader has banned expedition member
			if (selection > 0) {
				var banned = expedition.getMembers().get(selection);
				expedition.ban(banned);
				cm.sendOk("You have banned " + banned.getName() + " from the expedition.");
				cm.dispose();
			} else {
				cm.sendSimple("What do you want to do, expedition leader? \r\n#b#L0#View current expedition members.#l\r\n#L1#Start the fight!#l\r\n#L2#Stop the expedition.#l");
				status = 1;
			}
			break;
        case 6: // Leader has started the expedition
            var min = 1;
			var size = expedition.getMembers().size();
			if (size < min) {
				cm.sendOk("You need at least " + min + " players registered in your expedition.");
				cm.dispose();
				break;
			}
			if (em == null) {
				cm.sendOk("The event could not be found, please report this on the forum.");
				cm.dispose();
				break;
			}
			em.setProperty("leader", cm.getPlayer().getName());
			em.setProperty("channel", cm.getPlayer().getClient().getChannel());
			// Think we need to warp the party in?
			em.startInstance(expedition, 30000, amtFought + 1);
			cm.dispose();
			break;
		default:
			cm.sendOk("Well, this is embarrassing.");
			cm.dispose();
			break;
    }
}