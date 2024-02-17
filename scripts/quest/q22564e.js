var status = -1;

function end(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        qm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		qm.sendSay("Hmmm. You look like a human, so what brings you to the halfingers' village? Ack! That...that dragon next to you is... an Onyx Dragon? That would make you...the human that Grendel the Really old was talking about! The human with the Onyx Dragon?!", false, true);
	}else if(status == 1){
		qm.sendSayUser("#b(Chief Tatamo must indeed be a Halfinger because he instantly recognized Mir as an Onyx Dragon. Since he's a Halfinger, it is unlikely that he would hurt Mir.)", true, true);
	}else if(status == 2){
		qm.sendSay("Whoa! it's amazing that there are still Onyx Dragons right here in John! And it looks so young. It must have just hatched. I cannot believe my eyes", true, true);
	}else if(status == 3){
		qm.sendSayUser("#b(It seems Grendel the Really Old must have been pretending he didn't recognize Mir as an Onyx Dragon. He must've known all along...)", true, true);
	}else if(status == 4){
		qm.sendSay("Come to think of it, Onyx Dragons are one of those special dragons that can only be whole when they make a Spirit Pact! Without that pact, an Onyx Dragon is nothing. Your dragon looks quite strong. Wait, are you his...?!", true, true);
	}else if(status == 5){
		qm.sendSayUser("Yes. I am his Dragon Master. Mir. say hello", true, true);
	}else if(status == 6){
		qm.sendSay(1013000, "I don't need to speak to anyone other than my master. Sniff.", 4, true, true);
	}else if(status == 7){
		qm.sendSayUser("#bI'm sorry. He's a little shy.", true, true);
	}else if(status == 8){
		qm.sendSay("No worries! I've heard that Onyx Dragons can be skittish. I still can't believe that I am looking at a bona fide Onyx Dragon with my own two eyes.", true, true);
	}else if(status == 9){
		qm.sendSayUser("#bIf they're so skittish and cautious, how did they go extinct?", true, true);
	}else if(status == 10){
		qm.sendSay("That's... Well, that's an all but forgotten story. Hundreds of years ago, there was a powerful, dark force in Maple World. It was he who destroyed all the Onyx Dragons.", true, true);
	}else if(status == 11){
		qm.sendSayUser("#bBuy why did he destroy them?", true, true);
	}else if(status == 12){
		qm.sendSay("I couldn't say. All I know is that the Onyx Dragons fought against him and that they were obliterated as a result. I was still but a young Halfinger then, so I don't know the details.", true, true);
	}else if(status == 13){
		qm.sendSay("But it seems they weren't completely obliterated after all. I wonder how difficult life must be for this little creature. We have great facilities for raising dragons in Leafre.Interested in settling down by any chance?", true, true);
	}else if(status == 14){
		qm.sendSay(1013000, "No. I go where my master goes.", 4, true, true);
	}else if(status == 15){
		qm.sendSay("Ah, yes. Of course. I've also heard that Onyx Dragons texture their relationships with their masters more than even their own instincts. I see its true", true, true);
	}else if(status == 16){
		qm.sendSay("Onyx Dragons are supposedly spiritually connected to their masters. The master's power increases the Onyx Dragon strength, and the master, in turn, can harness that strength.", true, true);
	}else if(status == 17){
		qm.sendSay("But not just anyone can become the master of an Onyx Dragon. Onyx dragons have a keen eye for those with strong spirits. They are extremely picky. You must have an extremely powerful spirit, my friend.", true, true);
	}else if(status == 18){
		qm.sendSay("I wish you'd consider leaving him here in Leafre but I know you won't. I wonder...are there other Onyx Dragons still out there? Don't give up. I'll help Grendel the Really Old find another of his race.", true, true);
	}else if(status == 19){
		qm.sendSay("I'll send a message to you if I discover anything.\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#2000 exp\r\n#fUI/UIWindow.img/QuestIcon/10/0#1 sp", true, true);
	}else if(status == 20){
		qm.gainExp(2000);
		qm.getPlayer().gainSp(1);
		qm.forceCompleteQuest();
		qm.dispose();
	}
	
}