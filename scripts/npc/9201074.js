/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

var status;

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
			var missing = 0;
			var text = "I see you would like to make an #eEye of Fire#n, Let me see.#e";
			if (cm.haveItem(4005004, 10)){
				text += "#v4005004# x 10 acquired.\r\n";
			}else{
				text += "#v4005004# x 10 missing.\r\n";
				missing++;
			}
			if (cm.haveItem(4005000, 10)){
				text += "#v4005000# x 10 acquired.\r\n";
			}else{
				text += "#v4005000# x 10 missing.\r\n";
				missing++;
			}
			if (cm.haveItem(4011007)){
				text += "#v4011007# x 1 acquired.\r\n";
			}else{
				text += "#v4011007# x 1 missing.\r\n";
				missing++;
			}
			if (cm.haveItem(4001006)){
				text += "#v4001006# x 1 acquired.\r\n";
			}else{
				text += "#v4001006# x 1 missing.\r\n";
				missing++;
			}/* else {
				cm.sendOk("You are missing some items needed to create an #eEye of Fire#n.\r\nThe items you need are listed below.\r\n#e#v4005004# x 10\r\n #v4005000# x 10\r\n #v4011007# x 1\r\n #v4001006# x 1");
				cm.dispose();
			}*/
			if(missing == 0){
				text += "#n\r\nYou have brought everything that is required.\r\n";
				text += "Would you like me to create your #eEye of Fire#n?";
				cm.sendYesNo(text);
			}else{
				//text += "You are missing some items needed to create an #eEye of Fire#n.\r\n";
				cm.sendOk(text);
				cm.dispose();
			}
	} else if (status == 1) {
		if (cm.canHold(4001017)) {
			cm.gainItem(4011007, -1);
			cm.gainItem(4001006, -1);
			cm.gainItem(4005004, -10);
			cm.gainItem(4005000, -10);
			cm.gainItem(4001017, 1);
			cm.sendOk("Good luck against #eThe Mighty Zakum#n.");
			cm.dispose();
		}else{
			cm.sendOk("Please make sure you have enough room to hold the Eye #eEye of Fire#n");
			cm.dispose();
		}
	} else if (status == -1) {
		cm.dispose();
	}
}