/* NPC Base
	Pet-Walking Road
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
	if(cm.isQuestCompleted(4646) && !cm.isQuestCompleted(4647) && !cm.haveItem(4031921)){
		if(status == 0){
			cm.sendYesNo("#bI can see something covered in grass. Should I pull it out?");
		}else if(status == 1){
			cm.sendNext("I found the item that Pet Trainer Bartos hid..");
			cm.gainItem(4031921, 1);
			cm.startQuest(4647);
		}else if(status == 2){
			cm.dispose();//gotta be gms like with the sendNext.
		}
	}else cm.dispose();
}