/**
* Capacity Master
*/

var skillName = "Capacity";

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
	if(status == 0){
		cm.sendOk("Hey #b#h ##k, I'm " + cm.getRSSkillHighestLevelName(skillName) + ". \r\nI'm currently the #1 player in " + skillName + ".\r\nLevel: " + cm.getRSSkillHighestLevel(skillName) + "\r\nEXP: " + cm.getRSSkillHighestExp(skillName) );
		cm.dispose();
	}
}
