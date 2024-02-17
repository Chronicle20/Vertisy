//PQ Shop NPC
//PQ Currency: 4031682

var status;
var Elite;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
		var text = "                                         #d#ePQ Shop#n#k          \r\n";
		   text += "#e========================================#n\r\n";
		   text += "                                #v4031682#   #r#e#c4031682##n#k   #v4031682#\r\n";
		   text += "#e========================================#n\r\n";
		   text += "                           \r\n";
		   text += "#e#r10#k#n                  #L0##v5220000##l #L1##v5050001##l #L2##v5050002##l\r\n";
		   text += "                           \r\n";
		   text += "#e#r15#k#n                                 #L3##v1072369##l\r\n";
		   text += "                           \r\n";
		   text += "#e#r30#k#n                 #L4##v1122007##l #L5##v1122058##l #L6##v5050003##l\r\n";
		   text += "                           \r\n";
		   text += "#e#r50#k#n                      #L7##v4031531##l #L8##v5050004##l\r\n";
		   text += "                           \r\n";
		   text += "#e#r80#k#n                      #L9##v2022450##l #L10##v1003057##l\r\n";
		   text += "                           \r\n";
			cm.sendSimple(text);
	} else if (status == 1) {
		if (selection == 0) { //Gachapon stuff
			if (cm.haveItem(4031682, 10)) {
				cm.gainItem(5220000, 3);
				cm.gainItem(4031682, -10);
			cm.sendOk("Good luck!");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 1) {
			if (cm.haveItem(4031682, 10)) {
				cm.gainItem(5050001, 5);
				cm.gainItem(4031682, -10);
			cm.sendOk("Enjoy your #v5050001#.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 2) {
			if (cm.haveItem(4031682, 10)) {
				cm.gainItem(5050002, 3);
				cm.gainItem(4031682, -10);
			cm.sendOk("Enjoy your #v5050002.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 3) {
			if (cm.haveItem(4031682, 15)) {
				cm.gainItem(1072369, 1);
				cm.gainItem(4031682, -15);
			cm.sendOk("Enjoy your #v1072369#.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 4) {
			if (cm.haveItem(4031682, 30)) {
				cm.gainItem(1122007, 1);
				cm.gainItem(4031682, -30);
			cm.sendOk("Enjoy your #v1122007#.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 5) {
			if (cm.haveItem(4031682, 30)) {
				cm.gainItem(1122058, 1);
				cm.gainItem(4031682, -30);
			cm.sendOk("Enjoy your #v1122058#.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 6) {
			if (cm.haveItem(4031682, 30)) {
				cm.gainItem(5050003, 2);
				cm.gainItem(4031682, -30);
			cm.sendOk("Enjoy you #v5050003#.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 7) {
			if (cm.haveItem(4031682, 50)) {
				cm.gainItem(4031682, -50);
				cm.gainCash(4, 10000);
			cm.sendOk("Enjoy your #b10,000#k NX.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 8) {
			if (cm.haveItem(4031682, 50)) {
				cm.gainItem(4031682, -50);
				cm.gainItem(5050004, 1);
			cm.sendOk("Enjoy your #v5050004#.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if (selection == 9) {
			if (cm.haveItem(4031682, 80)) {
				cm.gainItem(2022450, 1, false, false, 1800000);
				cm.gainItem(4031682, -80);
			cm.sendOk("Enoy your #v2022450#.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		} else if(selection == 10) {// Elite
			if (cm.haveItem(4031682, 80)) {
				cm.gainItem(4031682, -80);
				cm.getClient().addEliteHours(4);
			Elite = cm.getClient().getEliteTimeLeft();
			cm.sendOk("Enjoy your #bElite Account Service#k.\r\n You now have " + Elite + " of Elite Account Service.");
			cm.dispose();
			} else {
			cm.sendOk("You don't seem to have enough #v4031682#.");
			cm.dispose();
			}
		}		
	}
}