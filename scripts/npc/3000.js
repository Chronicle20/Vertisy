/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*Amos the Strong - Entrance
**9201043
**@author Jvlaple
*/

var status = 0;
var MySelection = -1;
var sel = -1;

function start() {
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
	var currentTask = cm.getPlayer().getSlayerTask();
	if(status == 0){
		var text = "I see you are an experienced hunter, what would you like?\r\n#b";
		if(currentTask === undefined || currentTask === null){
			text += "#L0#New Task#l";
		}else{
			if(currentTask.isCompleted()){
				text += "#L1#Finish Task#l";
			}else{
				text += "#L2#Current Task#l";
			}
		}
		cm.sendSimple(text);
	}else if(status == 1){
		sel = selection;
		if(sel == 0){
			currentTask = cm.createSlayerTask();
			cm.getPlayer().setSlayerTask(currentTask);
			var mob = cm.getMonsterLifeFactory(cm.getPlayer().getSlayerTask().getTargetID());
			var text = "Here is your new task: \r\n";
			text += cm.getSlayerMobImg(cm.getPlayer().getSlayerTask().getTargetID()) + "\r\n";
			text += "#eMob Name:#n#d #o " + mob.getId() + "##k\r\n";
			text += "#eHealth:#n#r " + mob.getStats().getHp() + "#k\r\n";
			text += "#eExp: #n#g" + mob.getExp() + "#k\r\n";
			text += "#eSlayer Exp:#n#b " + mob.getStats().getLevel() + "#k\r\n";
			text += "#eAmount Left:#r " + (currentTask.getRequiredKills() - currentTask.getKills()) + "#k#n\r\n";
			text += "#eFound at:#n#b " + currentTask.getMap() + "#k\r\n";
			cm.sendOk(text);
			cm.dispose();
		}else if(sel == 1){
			if(currentTask.isCompleted()){
				cm.getPlayer().incrementTasksCompleted();
				cm.getPlayer().setSlayerTask(null);
				var level = cm.getPlayer().getLevel();
				var amount = 1;
				if(level > 30 && level <= 50){
					amount = 2;
				}else if(level > 50 && level <= 70){
					amount = 3;
				}else if(level > 70){
					amount = 4;
				}
				cm.gainItem(4310226, amount);
				cm.gainExp(cm.getPlayer().getLevel() * 200);
				cm.sendOk("Congratulations! You have completed the task. Come back if you want another challenge!");
			}
			cm.dispose();
		}else if(sel == 2){
			var mob = cm.getMonsterLifeFactory(cm.getPlayer().getSlayerTask().getTargetID());
			var text = "";
			text += cm.getSlayerMobImg(cm.getPlayer().getSlayerTask().getTargetID()) + "\r\n";
			text += "#eMob Name:#n#d #o " + mob.getId() + "##k\r\n";
			text += "#eHealth:#n#r " + mob.getStats().getHp() + "#k\r\n";
			text += "#eExp: #n#g" + mob.getExp() + "#k\r\n";
			text += "#eSlayer Exp:#n#b " + mob.getStats().getLevel() + "#k\r\n";
			text += "#eAmount Left:#r " + (currentTask.getRequiredKills() - currentTask.getKills()) + "#k#n\r\n";
			text += "#eFound at:#n#b " + currentTask.getMap() + "#k\r\n";
			cm.sendOk(text);
			cm.dispose();
		}
	}
}