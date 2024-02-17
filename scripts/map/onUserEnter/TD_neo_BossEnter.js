function start(ms) {
	ms.getPlayer().getMap().clearAndReset(true);
	var pmapId = ms.getPlayer().getMapId();
	var mobId = 100100;
	var x = 0;
	var y = 0;
	if (pmapId >= 240070203 && pmapId <= 240070209) { // <Year 2099> Midnight Harbor
		mobId = 7220003;
		x = 599;
		y = 395;
	} else if (pmapId == 240070303 && pmapId <= 240070309) { // <Year 2215> Bombed City
		mobId = 8220010;
		x = -107;
		y = 295;
	} else if (pmapId == 240070403 && pmapId <= 240070409) { // <Year 2216> Ruined City
		mobId = 8220011;
		x = -2899;
		y = 662;
	} else if (pmapId == 240070503 && pmapId <= 240070509) { // <Year 2230> Dangerous Tower
		mobId = 8220012;
		x = 170;
		y = 242;
	} else if (pmapId == 240070603 && pmapId <= 240070609) { // <Year 2503> Air Battleship
		mobId = 8220013;
		x = 190;
		y = -506;
	}
	ms.getPlayer().dropMessage(5, "Watch out! MapID: " + pmapId + ", Position: (" + x + ", " + y + ")");
	ms.getPlayer().getMap().spawnMonsterOnGroundBelow(mobId, x, y);
}