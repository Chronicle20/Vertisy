var status = -1;

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		if (type == 1 && mode == 0)
			status -= 2;
		else {
			qm.sendOk("I understand ... my wife's birthday is coming up and I am screwed! Please come back if you have some spare time.");
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendYesNo("Ohhhh, you're the one that helped me out the other day. You look much stronger now. How's traveling these days? I actually have another favor to ask you ... this time, my wife's birthday is coming up and I need more flowers. Can you get them for me?");
	} else if (status == 1) {
		qm.forceStartQuest();
		qm.sendNext("Thank you! This time I'd like to give my wife #b#t4031026##k ... It has a very pleasant scent, and I heard it's found deep in the forest ... I heard the place where it exists doesn't let everyone in; only a selected few, I think. Something about #p1061006# at #m105040300# and something something ...");
	} else if (status == 2) {
		qm.sendOk("Please get me #b20 #t4031026#s#k. I think 20 will cover the house with that pleasant scent. Please hurry!");
		qm.dispose();
	}
}

function end(mode, type, selection) {
	status++;
	if (mode != 1) {
		if (type == 1 && mode == 0) 
			status -= 2;
		else {
			qm.sendOk("You haven't gotten the #b#t4031026##k yet. There's #p1061006# at #m105040300# and I heard that with that you can go to the place where #t4031026##k is. Please go into the orest and collect #t4031026##k for me. I need 20 to make my wife's birthday present.");
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendNext("Ohhh ... you got me #b20 #t4031026#s#k~! This is awesome ... I can't believe you went deep into the forest and got these flowers ... there's a story about this flower where it supposedly doesn't die for 500 years. With this, I can make the whole house smell like flowers.");
	} else if (status == 1) {
		qm.sendOk("Oh, and ... since you have worked hard for me, I should reward you well. I found this glove in the ship. Some travelers leave things here and there. It looks like something you may need, so take it.");
		qm.forceCompleteQuest();
		qm.gainItem(4031026, -20);
		var item = 0;
		switch (qm.getPlayer().getJob()) {
			case 0:
			case 1000:
			case 2000:
				item = 1082002;
				break;
			case 100:
			case 110:
			case 120:
			case 130:
			case 111:
			case 112:
			case 121:
			case 122:
			case 131:
			case 132:
			case 1100:
			case 1110:
			case 1111:
			case 1112:
			case 2000:
			case 2100:
			case 2110:
			case 2111:
			case 2112:
				item = 1082036;
				break;
			case 200:
			case 210:
			case 211:
			case 212:
			case 220:
			case 221:
			case 222:
			case 230:
			case 231:
			case 232:
			case 1200:
			case 1210:
			case 1211:
			case 1212:
				item = 1082056;
				break;
			case 300:
			case 310:
			case 311:
			case 312:
			case 320:
			case 321: 
			case 322:
			case 1300:
			case 1310:
			case 1311:
			case 1312:
				item = 1082070;
				break;
			case 400:
			case 410:
			case 411:
			case 412:
			case 420:
			case 421: 
			case 422:
			case 1400:
			case 1410: 
			case 1411:
			case 1412:
				item = 1082045;
				break;
			case 500:
			case 510:
			case 511:
			case 512:
			case 520:
			case 521:
			case 522:
			case 1500:
			case 1510:
			case 1511:
			case 1512:
				item = 1082192;
				break;
		}
		qm.gainItem(item, 1);
		qm.gainExp(8000 * 4, true);
	}
}