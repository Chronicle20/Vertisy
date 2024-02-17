/**
 *	@Modified: iPoopMagic (David)
 */

function enter(pi) {
    var em = pi.getEventManager("CWKPQ");
	if (em != null) {
		var stage = parseInt(em.getProperty("glpq3"));
		if (stage >= 5) {
			em.setProperty("glpq3", parseInt(em.getProperty("glpq3")) + 1);
			pi.warp(610030300, 0);
			pi.mapMessage(6, "The " + getNumber(em.getProperty("glpq3")) + " adventurer has passed through!");
			if (em.getProperty("glpq3").equals("10")) {
				pi.mapMessage(6, "The Antellion grants you access to the next portal! Proceed!");
				pi.getPlayer().getMap().environmentChange("3pt", 2);
			}
		} else {
			pi.getPlayer().dropMessage(5, "All Sigils must be activated before entering.");
		}
    }
}

function getNumber(prop) {
	var number = parseInt(prop);
	var string = "1st";
	switch (number) {
		case 6:
			string = "1st";
			break;
		case 7:
			string = "2nd";
			break;
		case 8:
			string = "3rd";
			break;
		case 9:
			string = "4th";
			break;
		case 10:
			string = "5th";
			break;
	}
	return string;
}