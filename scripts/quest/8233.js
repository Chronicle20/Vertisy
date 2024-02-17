/**
 * @quest: Rags to Riches (8233)
 * @npc: Lita Lawless 
 */
var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendSimple("Ah! A fellow hunter, eh? Well, we've gotten some strange reports of all this stuff going on the Phantom Forest-people being attacked by bags of money, creepy ghosts, even a strange sighting of a Yeti-like creature. To help put a stop to this, Icebyrd and I are offering bounties upon certain creatures that are troublesome. Complete all of the Bounties, and I'll deputize you! Are you interested?\r\n#b#L0#If you've got the mesos, I got the time.#l\r\n");
	} else if (selection == 0) {
		qm.sendSimple("That's the spirit I like to see! I'll warn you now though, this won't be easy--these are no ordinary ghosts I'm sending you after. Are you sure you're ready to brave the dangers of the Phantom Forest and hunt these terrifying things?\r\n#b#L10#I ain't afraid of no ghosts!#l\r\n#L11#On second thought, maybe I'd better see Miki for some potions first...#l\r\n");
	} else if (selection == 10) {
		qm.sendSimple("Neither am I, friend! Excellent. So, I was speaking with a rather testy Shadower the other day, and he told me a tale about, coming across a strange creature in the Phantom Forest. One that merely gazed at him for a moment, then began to float towards him rather menacingly.\r\n#b#L20#Let me guess. He said 'Live and let live' and happily moved to another reason of the forest?#l\r\n");
	} else if (selection == 11) {
		qm.sendNext("Come see me when you're ready...");
		qm.dispose();
	} else if (selection == 20) {
		qm.sendNext("Good joke! No properly-trained Thief would do that! This creature appeared weakened, and we are thieves after all, so taking a dangerous creature's life isn't all that different from stealing a bag of mesos. At any rate, as he moved in preparation for battle, he began to grow cold...it was as if the air around him began to chill. He felt his joints stiffen, and he had trouble focusing at the creature approached.");
	} else if (status == 4) {
		qm.sendSimple("It resembled a Jr. Wraith, only much larger and foreboding, and it was as if all emotion was drained from the rea as it grew closer. He described it as an 'Elder Wraith'. Needless to say, my fellow Shadower was quite distressed. He fumbled his potion bag and tripped over himself, he was clearly flustered by the creature.\r\n#b#L30#That doesn't sound like any Thief I know. Dont' they have superhuman speed and leaping ability?");
	} else if (selection == 30) {
		qm.sendNext("Precisely! Obviously, the creature frightened him so much that he nearly forgot his training. Perhaps he thought it was some sort of vengeful spirit? No one can be sure, save him. So, the creature hovered closer, preparing a strike that surely would've ended my comrade's life, but he dodged it at the last moment and came to his senses. He readied his weapon and the battle began. He was surprised by the Elder Wraith's relentlessness because it chased him wherever he went. Finally after a long battle, he trumphed, and the creature disappeared, leaving only a Soiled Rag behind.\r\n");
	} else if (status == 5) {
		qm.sendSimple("#b#L40#An Elder Wraith? Interesting! I'd always wondered if they existed!#l");
	} else if (selection == 40) {
		qm.sendNext("Well, now you know. Part of me wonders what happened to them. They used to be something alive, after all.. ah well, a mystery for another day. As sheriff, I don't want anyone running into those things. I want you to bring me back 30 Soiled Rags from those horrific creatures. Get going!");
	} else if (status == 6) {
		qm.forceStartQuest();
		qm.dispose();
	}
}

function end(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendOk("Hmm, looks like you're short of the 30 Soiled Rags I asked for. I can understand if you want to rest, Just be sure to get them all before you return to me.");
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendOk("Back so soon?  Let's see how you did...5, 15...30! Excellent work! You've done an excellent service to the citizens of New Leaf City. Please take these mesos as a token of appreciation! Be sure to return once you rest-I have plenty of bounties available!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#fUI/UIWindow.img/QuestIcon/7/0# 2000000 mesos\r\n#fUI/UIWindow.img/QuestIcon/8/0# 35935 exp");
		qm.gainItem(4032011, -30);
		qm.getPlayer().gainMeso(2000000, true);
		qm.gainExp(35935 * 5);
		qm.completeQuest();
		qm.dispose();
	}
}