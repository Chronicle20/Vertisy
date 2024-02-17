/**
 * @author: Eric
 * @npc: Red Sign
 * @map: 101st Floor Eos Tower (221024500)
 * @func: Ludi PQ
 */

var status = 0;
var minLevel = 35; // according to Nexon it's 30, but it's actually a 35 requirement.
//var maxLevel = 200; //well we dont need this, since 200 is the max lvl for v83
//var minPartySize = 5;
var minPartySize = 1;
//var maxPartySize = 6; //don't need this either.
var brokenGlasses = 20; //glasses can be obtained every n times.
//var brokenGlassesCount = 0; // code custom quest data is on the todo list
var lpqBalance; //the balance of completed lpq count
var questId = 1202; //lpq rank quest

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    }

    if (mode === 1) {
        status++;
    } else if (mode === 0) {
        if (status === 1) {
            cm.sendNext((cm.getParty() === null ?
                    "Remember that using the Party Search function (Hotkey O) will allow you to find a party anytime, anywhere."
                    : "Send an invite to friends nearby. Remember that using the Party Search function (Hotkey O) will allow you to find a party anytime, anywhere."));
            cm.dispose();
            return;
        } else {
            status--;
        }
    }
    if (status === 0) {
        cm.sendSimple("#e<Party Quest: Dimensional Schism>#n\r\n\r\nYou can't go any higher because of the extremely dangerous creatures above. Would you like to collaborate with party members to complete the quest? If so, please have your #bparty leader#k talk to me.#b\r\n\
#L0#I want to participate in the party quest.\r\n\
#L2#I want to receive the Broken Glasses.\r\n\
#L3#I would like to hear more details.");
    } else if (status === 1) {
        if (selection === 0) {
            if (cm.getParty() === null) { //if no party
                cm.sendOk("You can participate in the party quest only if you are in a party.");
                cm.dispose();
                return;
            } else if (/*!cm.getPlayer().isGM() && */(cm.getParty().getMembers().size() < minPartySize || !cm.isLeader())) {
                //whats with the gm check?
                //if party size is too small or not a leader
                cm.sendOk("You cannot participate in the quest, because you do not have at least 3 party members.");
                cm.dispose();
                return;
            } else { //if is leader and valid party size for lpq
                // Check if all party members are within PQ levels
                var party = cm.getParty().getMembers();
                var mapId = cm.getMapId();
                var next = true; //this means the party is qualified for pq
//                var levelValid = 0;
                var inMap = 0;
                var it = party.iterator();

//                while (it.hasNext()) { //ori
//                    var cPlayer = it.next();
//                    if ((cPlayer.getLevel() >= minLevel)) {
//                        levelValid += 1;
//                    } else {
//                        next = false;
//                    }
//                    if (cPlayer.getMapId() === mapId) {
//                        inMap += (cPlayer.getJobId() === 910 ? 6 : 1);
//                    }
//                }
                while (it.hasNext()) {
                    var cPlayer = it.next();
                    if (cPlayer.getMapId() !== mapId || cPlayer.getLevel() < minLevel) {
                        next = false;
                        break;
                    }
                    inMap++;
                }
                if (/*party.size() > maxPartySize || */inMap < minPartySize) { //if party size is too small
                    next = false;
                }
                if (cm.getPlayer().isGM()) //ori, GM ftw?
                    next = true;

                if (next) {
                    var em = cm.getEventManager("LudiPQ");
                    if (em === null) {
                        cm.sendOk("The Ludibrium PQ has encountered an error. Please report this on the forums, and with a screenshot.");
                    } else {
                        if (em.getProperty("LPQOpen").equals("true")) {
//                            em.startInstance(cm.getParty(), cm.getPlayer().getMap());
                            cm.removePartyItems(4001022);
                            cm.removePartyItems(4001023);
                            em.startPQ("LPQ", cm.getParty(), cm.getPlayer().getMap());
                            cm.dispose();
                            return;
                        } else { //if other party already inside
                            cm.sendOk("Another party has already entered the #rParty Quest#k in this channel. Please try another channel, or wait for the current party to finish.");
                        }
                    }
                } else {
                    cm.sendYesNo("You cannot participate in the quest, because you do not have at least "
                            + minPartySize + " party members which are at least level " + minLevel
                            + ".\r\nPlease make sure all your members are present and qualified to participate in this quest. \r\nIf you're having trouble finding party members, try Party Search.");
                }
            }
        } else if (selection === 2) { // todo, broken glasses
            var npcText = "I am offering 1 #i1022073:# #bBroken Glasses#k for every "
                    + brokenGlasses + " times you help me.\r\n\r\n";
            lpqBalance = cm.getPlayer().getQuestProgress(questId, 69); //progress 69 = completed lpq count balance
            var unclaimed = lpqBalance / brokenGlasses; //number of broken glasses that can be claimed
            if (unclaimed > 0) {
//                status++; //a hack so that status = 3 means getting glasses cuz status = 2 is taken by some other msg.
                cm.sendYesNo(npcText
                        + "I see that you are eligible to get " + unclaimed
                        + " #i1022073:# #bBroken Glasses#k, would you like to get 1 now?");
            } else {
                var balance = lpqBalance % brokenGlasses; //how many more lpq needed.
                cm.sendOk(npcText + "If you help me #b#e"
                        + (balance > 0 ? balance : brokenGlasses + balance)
                        + "#n more times, you can receive #eBroken Glasses#k.");
                cm.dispose();
            }
        } else {
            cm.sendOk("#e<Party Quest: Dimensional Crack>#n\r\n\
A Dimensional Crack has appeared in #b#m220000000#!#k We desperately need brave adventurers who can defeat the intruding monsters.\r\n\
Please, party with some dependable allies to save\r\n#m220000000#!\r\n\
You must pass through various stages by defeating monsters and solving quizzes, and ultimately defeat #r#o9300012##k.\r\n\
 - #eLevel#n: " + minLevel + " or above #k\r\n - #eTime Limit#n: 60 min\r\n - #eNumber of Players#n: "
                    + minPartySize + " to 6\r\n - #eReward#n: #i1022073:# Broken Glasses #b(obtained every "
                    + brokenGlasses + " time(s) you participate)#k\r\n                      Various Use, Etc, and Equip items");
            cm.dispose();
        }
//    } else if (status === 2) {
////        if (mode > 0) { //ori
////            //cm.findParty();
////        }
//        cm.dispose();
//    } else if (status === 3) {
    } else {
        if (cm.canHold(1022073)) {
            var brokenGlassesObtained = cm.getPlayer().getQuestProgress(questId, 1); //progress 1 = item qty

            if (cm.getPlayer().getQuestProgress(questId) === 0) { //if the player didnt have it before, record it. (hadItem)
                cm.getPlayer().updateQuestProgress(questId, 1); //progress 0 = an indicator if the player has the item. (hadItem)
            }

            //if (brokenGlassesObtained > 4 && cm.getPlayer().checkEquippedFor(1022073)) { //shhh :p
                //randomize glasses if the player already had/have 5 glasses before = completed at least 100 LPQ (5*20)
                //and is wearing the glasses.
            //    cm.gainItem(false, 1022073, 1, true, true);
            //} else {
                cm.gainItem(false, 1022073, 1);
            //}

            //update progress id 1 to record the item qty the player has gotten.
            cm.getPlayer().updateQuestProgress(questId, 1, brokenGlassesObtained + 1);
            //deduct progress id 69 (completed lpq count balance) by n
            cm.getPlayer().updateQuestProgress(questId, 69, lpqBalance - brokenGlasses);

            cm.sendNext("It's my token of appreciation, hope you like it!");
        }
    }
}