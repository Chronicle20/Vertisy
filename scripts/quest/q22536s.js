var status = -1;

function start(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        qm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		qm.sendSay("Hmm? I don't recall seeing you around here before. What brings you to Kerning City? Are you here to become a Thief?\r\n\r\n#L0# #b(You ask her if she has noticed anyone who smells like herbs.)#l", false, true);
	}else if(status == 1){
		qm.sendSay("Smells like herbs? I don't know... I thought everyone uses potions these days! Why are you asking about herbs? Are you looking to buy some?\r\n\r\n#L0# #b(You explain what happened to Sabitrama.)#l", false, true);
	}else if(status == 2){
		qm.sendSay("Hmm... An herb thief? I see. What? Wait, wait, wait a minute! Are you suggesting that the thief is from Kerning City?\r\n\r\n#L0# #bWell Kerning City is a Thief Town, after all.#l", false, true);
	}else if(status == 3){
		qm.sendSay("We're not burglars! This is a Thief town, not a burglar town! Argh! It drives my crazy when... Geez! The things you're implying about is Thieves in Kerning City! It's true that we can be a bit sneaky and petty, a little under-handed and cunning, yes. But we don't threaten other people's livelihood to get what we want!\r\n\r\n#L0# #bReally?!#l", false, true);
	}else if(status == 4){
		qm.sendSay("Really! I know people get the wrong idea about us, but this...! Man, as someone born and raised in Kerning City. I am deeply offended! I swear on my mother that the burglar that you're looking for is not from Kerning City!\r\n\r\n#L0# #bOh? Well, where is the burglar from then?#l", false, true);
	}else if(status == 5){
		qm.sendAcceptDecline("How should I know?! You can't just assume that the burglar is from Kerning City! That's completely unfair. You know what? I will hunt down the dirty rotten burglar myself! I'm going to grab that Sabitrama's Herb burglar with my own two hands and reclaim the honor of Kerning City. I swear it!");
		//also has 4k exp display
	}else if(status == 6){
		if(!qm.isQuestStarted() && !qm.isQuestCompleted()){
			qm.gainExp(4000);
			qm.forceStartQuest();
			qm.forceCompleteQuest();
		}
		qm.sendSay("Fine! #bI'll investigate the burglar you're looking for so stay right here.#k I'll contact you when I get to the bottom of this Argh!", false, false);
		qm.dispose();
	}
}