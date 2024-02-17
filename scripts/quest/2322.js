/* ===========================================================
			Resonance
	NPC Name: 		Minister of Home Affairs
	Map(s): 		Mushroom Castle: Corner of Mushroom Forest(106020000)
	Description: 	Quest -  Over the Castle Wall (2)
=============================================================
Version 1.0 - Script Done.(18/7/2010)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendNext("Really? Is there another way you can penetrate the castle? If you don't know of one, then just come see me.");
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendYesNo("Like I told you, we can't be relieved just because the barrier has been broken. The castle of the Mushking Empire is impenetrable from the outside, so it won't be easy for you to enter. First, would you mind investigating the outer walls of the castle?");
	if (status == 1)
		qm.sendNext("Head over to the castle from the #bSplit Road of Destiny#k, past the Mushroom Forest. Good luck.");
	if (status == 2){
		qm.forceStartQuest();
//		qm.forceStartQuest(2322, "1");
		//qm.gainExp(11000);
		//qm.sendOk("Good job navigating through the area.");
		//qm.forceCompleteQuest();
		qm.dispose();
	}
}

function end(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.dispose();
			return;
		}
	}
	if (status == 0)
		qm.sendOk("Hmmm I see... so they have completely shut off the entrance and everything.");
	if (status == 1){
		qm.gainExp(11000);
		qm.sendOk("Good job navigating through the area.");
		qm.forceCompleteQuest();
		qm.dispose();
	}
}
	