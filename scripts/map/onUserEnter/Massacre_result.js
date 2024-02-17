function start(ms) {
	if(ms.getPlayer().getMapId() == 910320001 || 910320000){
		var py = ms.getSubwayPQ();
		if (py != null) {
		    py.sendScore(ms.getPlayer());
		}
	}else{
		var py = ms.getPyramid();
		if (py != null) {
		    py.sendScore(ms.getPlayer());
		}
	}
}