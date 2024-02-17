/**
 *	@Name: Jack (CWKPQ)
 *	@Modified: iPoopMagic (David)
 */

var status = -1;
var expedition;

function start() {
	action(1, 0, 0);
}

function action(mode, type, selection) {
	expedition = cm.getExpedition(Packages.server.expeditions.MapleExpeditionType.CWKPQ);
	((mode > 0) ? status++ : status--);
	var em = cm.getEventManager("CWKPQ");
	if (em == null) {
		cm.sendNext("This party quest is under construction. Please be patient as we try to fix it.");
		cm.dispose();
		return;
	}
	if (!expedition.isLeader(cm.getPlayer())) {
		cm.sendNext("Only your expedition leader may talk to me.");
		cm.dispose();
		return;
	}
	switch (cm.getPlayer().getMapId()) {
		case 610030100:
			if (status == 0) {
				cm.sendNext("Agh, you have made it in. Let me tell you real quick: they've caught us already. Master Guardians are about to come here in about a minute. We'd better hurry.");
			} else if (status == 1) {
				cm.sendNext("The portal to the Twisted Masters is busted. We have to find an alternate way, one that will take us through many death traps.");
			} else if (status == 2) {
				cm.sendNext("You can find the portal somewhere around here... you'd better find it, quick. I'll catch up.");
				cm.dispose();
				em.setProperty("glpq1", "1");
			}
			break;
		case 610030200:
			if (status == 0) {
				cm.sendNext("That was a success! Now, for this path, I do believe we need one of every Adventurer class to get past.");
			} else if (status == 1) {
				cm.sendNext("They need to use their skills on each of these things called Sigils. Once all five have been done, we can get past.");
				cm.dispose();
			}
			break;
		case 610030300:
			if (status == 0) {
				cm.sendNext("Now what we have here are more Sigils. All five Adventurers have to climb to the very top and go through the portal.");
			} else if (status == 1) {
				cm.sendNext("Beware of these death traps: Menhirs. They really pack a punch.");
//				em.setProperty("glpq3", "0");
				cm.dispose();
			}
			break;
		case 610030400:
			if (status == 0) {
				cm.sendNext("Now what we have here are more Sigils. However, some of them don't work.");
			} else if (status == 1) {
				cm.sendNext("These Stirges will get in your way, but they're merely a distraction. Try every one of these Sigils until they work.");
				var map = cm.getPlayer().getEventInstance().getMapInstance(610030400);
				map.setReactorState(); //because everything is at 0 =[
				map.getReactorById(6109016).setAlive(true);
				map.getReactorById(6109017).setAlive(true);
				map.getReactorById(6109018).setAlive(true);
				map.getReactorById(6109019).setAlive(true);
				map.getReactorById(6109020).setAlive(true);
				cm.dispose();
			}
			break;
		case 610030500:
			if (status == 0) {
				cm.sendNext("Surprised you made it this far! What you see here is the statue of Crimsonwood Keep, but without any of it's weapons.");
			} else if (status == 1) {
				cm.sendNext("There are five rooms, marked by a statue near each of them, around the statue.");
			} else if (status == 2) {
				cm.sendNext("I suspect that each of these rooms have one of the statue's five weapons.");
			} else if (status == 3) {
				cm.sendNext("Bring back the weapons and restore them to the Relic of Mastery!");
				cm.dispose();
			}
			break;
		case 610030700:
			cm.sendNext("That was some good work out there! This leads the way to the Twisted Masters' Armory.");
			cm.dispose();
			break;
	}
}
