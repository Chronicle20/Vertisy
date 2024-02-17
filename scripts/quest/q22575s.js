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
		qm.sendSay("Hello there, Evan. Oh I didn't mean to startle you. Don't worry. I'm not some weirdo. I belong to the secret organization that you just joined.", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bYou're Doll Left Behind's master, then?", true, true);
	}else if(status == 2){
		qm.sendSay("Oh, you must be talking about Francis. No, no, I'm not his master, but I am senior to him, yes. I assigned a mission to you a while back in Orbis.", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bThe mission from behind the Hidden Brick?", true, true);
	}else if(status == 4){
		qm.sendSay("Yes. It was I who left the piece of paper with the mission behind the Hidden Brick. Thanks to you, I was able to make great use of the Growth Accelerant. It was very helpful.", true, true);
	}else if(status == 5){
		qm.sendSayUser("#bHahaha. I'm happy to be a contributing member of the organization", true, true);
	}else if(status == 6){
		qm.sendYesNo("Your contributions have made you quite the talk of the organization. I feel I can entrust you with another mission. Will you accept it?");
	}else if(status == 7){
		qm.sendSay("This mission more difficult than the last, because it can only be done in cold regions like El Nath. Have you heard of the #bDead Tree Forest#k? It's in El Nath. You must go there and defeat #rzombies#k, then rescue Zombie's Lost Tooth.", false, true);
	}else if(status == 8){
		qm.forceStartQuest();
		qm.sendSay("Collect #b150 Zombie's Lost Tooths#k, then pass those to #bShammos#k in the basement of the Chief's Resident in El Nath. Shammos will give you the promised item. I will get in touch with you again then.", true, false);
		qm.dispose();
	}
}