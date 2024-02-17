/**
 *	@Author: iPoopMagic (David)
 *	@Name: The Right Path
 */
var status = -1;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	}
	if (mode == 0) {
		qm.dispose();
		return;
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			qm.sendSimple("Welcome back to our camp. What can I do for you?\r\n#b#L0#I found Jack Barricade.#l#k");
		} else if (selection == 0) {
			qm.sendSimple("You did? Still alive, eh? I would have thought that the wilderness would've gotten him. So you spoke to that scoundrel? Man! Did he manage to loot any treasure yet?\r\n#b#L1#Not yet, but he's working on it.#l\r\n#L2#I don't think he's in it for the mesos.#l#k");
		} else if (selection == 1) {
			qm.sendOk("That guy will never stop trying, will he?");
			qm.dispose();
		} else if (selection == 2) {
			qm.sendSimple("Oh really? Then what..? The glory of excavating a lost civilization? #b" +
							"\r\n#L10#Yes, he and his brother want to lay claim to being the best treasure hunters in the entire Maple World!#l" + 
							"\r\n#L11#No, I think Jack does it for the rush. He's a thrill seeker.#l" +
							"\r\n#L12#Jack thinks knowledge is the most valuable treasure.#l" +
							"\r\n#L13#Wait a minute.. For all I know, you guys could be competing treasure hunting company!#l#k");
		} else if (selection == 10) {
			qm.sendOk("We have to beat them to it then! There's no way we can let that happen. Jack and John, here we come!");
			qm.dispose();
		} else if (selection == 11) {
			qm.sendOk("What a guy, he thinks he can do what he wants just for the thrill of it!");
			qm.dispose();
		} else if (selection == 12) {
			qm.sendSimple("Knowledge? Really? And what would a grubby-handed treasure hunter know of knowledge? #b" +
							"\r\n#L20#Well, I guess he had enough knowledge of Omok to win a map off of you!#l" +
							"\r\n#L21#Jack has a theory that an object known as the Antellion was responible for the disappearance of Masteria.#l#k");
		} else if (selection == 13) {
			qm.sendOk("I cannot release that information. Until I've furthered my trust in you.");
			qm.dispose();
		} else if (selection == 20) {
			qm.sendOk("Yeah, and now we're done for good! You don't realize how much that map is worth to us!");
			qm.dispose();
		} else if (selection == 21) {
			qm.sendSimple("The Antellion? Jack knows of it? And he's interested in the cause of Masteria's disappearance? I see he's no ordinary treasure hunter. And what has he learned? #b" +
							"\r\n#L30#He was captured by the corrupted warriors and taken to their leaders, but he managed to escape. From what he saw inside, Jack thinks they're plotting someting big... and he's out to find out what it is.#l#k");
		} else if (selection == 30) {
			qm.sendSimple("He made it to Crimsonwood Keep? The ascent up Crimsonwood Mountain is legendary in difficulty. Alright. The man's got guts, I'll give him that. And you said he escaped those thugs, and still has the gall to go back for more? Ha! Can't let a bone go, eh? I can respect that in a man! #b" +
							"\r\n#L40#He also mentioned that he won the map fair and square, and only took it because his own mission was so pressing.#l#k");
		} else if (selection == 40) {
			status = 20;
			qm.sendNext("Hmph. Had I known he was interested in Masteria's disappearance, our conversation might have gone differently. I'll admit that my ego may have gotten the better of me at that time. Yeah, go ahead and laugh but Omok's one of the few things that really gets my blood riled up... I take it quite seriously. And who would've thought I'd find a decent player out here in the woods? Very well, I may have prejudged the man's motives. I'll reserve my judgment until I hear his full story.");
		} else if (status == 21) {
			qm.sendYesNo("You have the #bMap of Phantom Forest#k Jack gave you. Do you wish to return the map to Taggrin?");
		} else if (status == 22) {
			qm.sendSimple("What's this... our map? The rogue gave it to you? And you are giving it back? This is ... unexpected. #b" +
							"\r\n#L50#It's your map, so I'm returning it to you.#l#k");
		} else if (selection == 50) {
			qm.sendNext("Very well. I thank you for this. It's.. an honorable gesture. I have some things to think about. Please, come back and speak to me in a little while.");
			qm.gainItem(3992040, -1);
			qm.forceStartQuest();
			qm.dispose();
		}
	}
}

function end(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	}
	if (mode == 0) {
		qm.dispose();
		return;
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			qm.sendNext("I want to thank you again for returning our map. The gesture tells me something of who you are. Perhaps someone to be trusted, a potential ally. Time will tell if my faith is misplaced.");
		} else if (status == 1) {
			qm.sendNextPrev("You asked me once what we were, if not bandits. <smiles> So I tell you this now: We are the members of the #bRaven Ninja Clan#k, and hail from an island called the isle of Vigilance off the northern shores of Masteria.");
		} else if (status == 2) {
			qm.sendNextPrev("We Raven Ninjas trace our lineage back to the ancient Masterian, guild of the #bShadowknights#k, so you could consider Crimsonwood Keep our ancestral home. Our mission here is to discover the reason for Masteria's disappearance and its return. It is almost certainly tied with the current events at the Keep, and what you and this Jack Barricade are pursuing.");
		} else if (status == 3) {
			qm.sendOk("Based on what you have told me, I have expanded our mission to include the elimination of these trespassers that now occupy Crimsonwood Keep. <smiles> It's what we do best. But we will #bpay a bounty#k for proof of those you happen to dispatch as well. Consider yourself a contractor. My brother Joko will handle the details so you should speak to him. \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v3992040# 1 #t3992040#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 35935 exp");
			qm.gainExp(35935);
			qm.gainItem(3992040, 1);
			qm.forceCompleteQuest();
			qm.dispose();
		}
	}
}