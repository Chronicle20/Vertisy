/**
 *	@Name: A Secret Note (Holiday PQ Pre-Quest)
 *	@Author: iPoopMagic (David)
 */
var status = -1;

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		qm.sendOk("Are you plotting against us with Scrooge?");
		qm.dispose();
		return;
	}
	if (status == 0) {
		qm.sendAcceptDecline("Hey there, #b#h0##k. My name is Snow Spirit. It's the that time of the year again here in Happyville. Maplemas is coming, and the whole town is eager to enjoy the festivities. One person, however, seems intent on ruining all that for us. " +
							"Have you heard about Scrooge and his plans? He is trying to take away our Maplemas, and he is currently holding up somewhere hatching up a plan to do just that. I don't understand why he'd want to do such a terrible thing... but since I am the " +
							"protector of Happyville, I am here to ask you for help in our mission to defeat Scrooge and save Maplemas. Would you like to join me?");
	} else if (status == 1) {
		qm.forceStartQuest();
		qm.forceCompleteQuest();
		qm.sendOk("Great! I knew you'd volunteer to help out. Now allow me to explain to you what we do for Maplemas here in Happyville. Every year, the residents of Happyville get together and hold a festival in preparation for Maplemas by creating a giant snowman. " + 
					"On the eve of Maplemas, the residents will gather up in front of the snowman, say their prayers, and make new year's resolutions as well. (Shhh... this is just between you and me, but while the residents say their prayers, I cast a spell on them for good luck.) " +
					"Anyway, the good people here have been very busy making the giant snowman in hopes of successfully celebrating the bigger holiday of the year, but... there's this man named Scrooge, he lives a world away here, and he's trying to hatch a plan to ruin our Maplemas. " +
					"He had already been banned from Maple World for various offenses, he once tried to cook the Easter Bunny, and even got in a fight with Cody. The problem is... he somehow found his way into Happyville without anyone knowing. His plan is to remove the giant snowman and " +
					"ruin the holidays for everyone here. This is why we must alert this to Alcaster in El Nath! He's very wise and he will figure out the best way to handle this crisis. Unfortunately, I can't leave this place, but El Nath isn't too far from here, so I am sure you can make " +
					"your way there and let him know in place of me. Good luck.");
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.forceCompleteQuest();
	qm.dispose();
}