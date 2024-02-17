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
	
	if(cm.getPlayer().getMap().getMonsters().isEmpty()){
		if(status == 0){
			cm.sendSay("Whoa... I had no idea you were this strong. Oh well. I'll have to retreat for now. The next time I see you... I suppose you will be an enemy.", false, true);
		}else if(status == 1){
			cm.getPlayer().updateQuestInfo(22589, "1");
			cm.warp(914100021);
			cm.dispose();
		}
	}else{
		cm.sendSay("You are a lot weaker than I thought.", false, false);
		cm.dispose();
	}
}