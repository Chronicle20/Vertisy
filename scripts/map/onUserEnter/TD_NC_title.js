function start(ms) {
	switch ((ms.getPlayer().getMapId() / 100) % 10) {
		case 0:
			ms.mapEffect("temaD/enter/teraForest");
			break;
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
			ms.mapEffect("temaD/enter/neoCity" + ((ms.getPlayer().getMapId() / 100) % 10));
			break;
	}
}