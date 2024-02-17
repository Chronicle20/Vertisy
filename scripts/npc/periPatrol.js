/* 
	East Rocky Mountain I
	Used for Evan Quests
 */

var status;

var monsters = "None";
var comments = "None";

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
	
	var wrong = false;
	var checked = isChecked();
	switch(cm.getPlayer().getMapId()){
		case 101030400:
			monsters = "Red Snails, Stumps";
			comments = "Located at the junction on the east side of Rocky Mountain, Rocky Road, and Perion Street Corner.";
			break;
		case 101030300:
			monsters = "Stumps, Dark Stumps";
			comments = "None";
			break;
		case 101030200:
			monsters = "Stumps, Dark Stumps" + (checked ? "" : ", Ghost Stumps");
			comments = "None";
			wrong = true;
			break;
		case 101030100:
			monsters = "Dark Stumps";
			comments = "Located at the junction of Excavation Site I, Rocky Road II, and " + (selection == 2 || checked ? "East Domain of Perion" : "East Rocky Mountain");
			wrong = true;
			break;
		case 101030000:
			monsters = "Stumps, Dark Stumps";
			comments = "Located at the border of Ellinia";
			break;
	}
	if(cm.isQuestCompleted(22530))wrong = false;
	var original = "#m" + cm.getPlayer().getMapId() + "# Warning Post\r\n\r\nMonsters found: " + monsters + ".\r\nComments: " + comments + "\r\nConfirm: ";
	if(checked)original += "O";
	//original += "\r\n\r\n" + cm.getPlayer().getProgressValue("warningpost");
	if(status == 0){
		if(cm.isQuestStarted(22530)){
			cm.sendSay(original, false, true);
		}else{
			cm.sendSay(original, false, true);
			cm.dispose();
		}
	}else if(status == 1){
		if(checked){
			if(wrong){
				cm.sendSay("It seems I have already corrected this Warning Post.", false, false);
			}else{
				cm.sendSay("It seems I have already confirmed this Warning Post.", false, false);
			}
			cm.dispose();
		}else if(wrong){
			if(cm.getPlayer().getMapId() == 101030200){//Rocky Road II
				var erase = "#bYou noticed that a monster listed in the Warning Post isn't actually in the area. Erase any monsters that shouldn't have been listed.)\r\n\r\n";
				erase += "#L0#Stumps#l\r\n";
				erase += "#L1#Dark Stumps#l\r\n";
				erase += "#L2#Ghost Stumps#l\r\n";
				cm.sendSimple(erase);
			}else if(cm.getPlayer().getMapId() == 101030100){//Rocky Road III
				var incorrect = "#b(The areas indicated on the Warning Post are incorrect. You should correct the area name.)\r\n\r\n";
				incorrect += "#L0#Excavation Site I#l\r\n";
				incorrect += "#L1#Rocky Road II#l\r\n";
				incorrect += "#L2#East Rocky Mountain#l\r\n";
				cm.sendSimple(incorrect);
			}
		}else{
			cm.sendYesNo("#b(You don't think there's a mistake on the Warning Post. Mark the Confirm button with an O and be on your way.)");
		}
	}else if(status == 2){
		if(wrong){
			var proper = false;
			if(selection == 2 && cm.getPlayer().getMapId() == 101030200)proper = true;
			else if(selection == 2 && cm.getPlayer().getMapId() == 101030100)proper = true;
			if(proper){
				if(cm.getPlayer().getMapId() == 101030200){
					var erased = "#b(You erased the monster name that was incorrectly added to the Warning Post and marked the confirm button with an O.)#k\r\n\r\n";
					erased += original;
					erased += "O";
					addChecked();
					cm.sendSay(erased, false, true);
				}else if(cm.getPlayer().getMapId() == 101030100){
					var fixed = "#b(You fixed the area name incorrectly written on the Warning Post and marked the confirm button with an O.)#k\r\n\r\n";
					fixed += original;
					fixed += "O";
					addChecked();
					cm.sendSay(fixed, false, true);
				}
			}else{
				cm.sendSay("#b(I think that mob is on this map.)", false, false);
				cm.dispose();
			}
		}else{
			var text = "#b(You marked the Warning Post with an O.)#k\r\n\r\n";
			text += original;
			text += "O";
			addChecked();
			cm.sendSay(text, false, true);
		}
	}else if(status == 3){
		var checked = getChecked();
		cm.sendSay("You confirmed " + checked + " Perion Warning Posts. Confirm all 5, then go report it to Mike.", false, false);
		cm.getPlayer().updateQuestInfo(22530, "" + checked);
		cm.dispose();
	}
}

function addChecked(){
	if(cm.getPlayer().isProgressValueSet("warningpost")){
		cm.getPlayer().setProgressValue("warningpost", cm.getPlayer().getProgressValue("warningpost") + ";" + cm.getPlayer().getMapId());
	}else{
		cm.getPlayer().setProgressValue("warningpost", cm.getPlayer().getMapId());
	}
}

function getChecked(){
	if(!cm.getPlayer().isProgressValueSet("warningpost"))return 0;
	return cm.getPlayer().getProgressValue("warningpost").split(";").length;
}

function isChecked(){
	if(!cm.getPlayer().isProgressValueSet("warningpost"))return false;
	var split = cm.getPlayer().getProgressValue("warningpost").split(";");
	for(mapidss in split){
		if(parseInt(split[mapidss]) == cm.getPlayer().getMapId())return true;
	}
	return false;
}