var status;
var sel;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
		cm.getPlayer().setHardMode(-1);//Hard-mode Here
		cm.getPlayer().setIronMan(-1);
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		cm.sendSimple("Hello #h #, would you like to take on the challenge of Iron Man or enable Hard-Mode?\r\n\r\n#L0##bWhat is Iron Man?#n#l\r\n#L3##bWhat is Hard-Mode?#n#l\r\n#L1##bI would like to be an Iron Man#n#l\r\n#L4##bI would like to enable Hard-Mode#n#l\r\n#L5##bI would like to be a Hard-Mode IronMan#n#l\r\n#L2##bI don't think I'm up for the challenge.#n#l");
	}else if(status == 1){
		sel = selection;
		if(sel == 0){
			var info = "#e#rIron Man Challenge#k#n\r\nIron Man is a Solo-Only challenge. Meaning you are not allowed to do anything that involves another player.";
			info += "\r\nEX: Party Quests, Etc.\r\nList of things Iron Man characters cannot access.\r\n\r\n";
			info += "1.) Trading other Players\r\n";
			info += "2.) Accessing Player FM Stores.\r\n";
			info += "3.) Looting items dropped by players.\r\n";
			info += "4.) Looting items from monster a player has killed.\r\n";
			info += "5.) Accessing account storage.\r\n";
			info += "6.) Partying with other players.\r\n";
			info += "7.) Joining others in a Boss Expedition.\r\n";
			info += "8.) Accessing the account NX.";
			cm.sendPrev(info);
		}else if(sel == 1){ //Ironman accept
			cm.sendOk("You have chosen to accept the challenge.\r\nI have infused your heart with loneliness.\r\nThere's no turning back now.\r\nWelcome to #b#eVertisy#k#n.\r\nGood luck.");
			cm.getPlayer().setIronMan(1);
			cm.dispose();
		}else if(sel == 2){ //Decline
			cm.sendOk("I understand, Not everyone is up for the challenge.\r\nEnjoy your time on #bVertisy#k.");
			cm.getPlayer().setIronMan(-1);
			cm.getPlayer().setHardMode(-1);
			cm.dispose();
		}else if(sel == 3){ //Hard-Mode Info
			var HInfo = "#e#rHard-Mode#k#n\r\nHard-Mode is an increased difficulty compared to others.";
				HInfo += "Your EXP rate has dropped to the minimum amount. (1x)";
				HInfo += "Death is.. Death, You will be reset down to #bLevel 10#k and lose everything your holding in your inventory.";
				HInfo += "You will be placed back at your first job class, and your hotkeys will be wiped clean.";
				HInfo += "Quests will be reset and available to do again.";
				HInfo += "Your NX Cash will be kept.";
				HInfo += "However, your soul may not still be intact.";
				HInfo += "This is only meant for the people looking for a true challenge.";
				HInfo += "Are you up for it?";
			cm.sendPrev(HInfo);
		}else if(sel == 4) { //Hard-mode Enabled
			cm.sendOk("Your body has been embraced by the darkness.\r\nWelcome to #b#eVertisy#k#n #e#rHard-Mode#k#n.\r\nThere's no turning back.");
			cm.getPlayer().setHardMode(1);//Hard-mode Here
			cm.getPlayer().setIronMan(-1);
			cm.dispose();
		}else if(sel == 5) { //Hard-Mode Iron Man Enabled
			cm.sendOk("Your body has been surrounded by darkness, you can't seem to reach anyone for assistance.\r\nWelcome to #b#eVertisy#k#n.\r\nThere's no turning back.");
			cm.getPlayer().setIronMan(1);
			cm.getPlayer().setHardMode(1);
			cm.dispose();
		}
	}
}