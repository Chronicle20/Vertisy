

function enter(pi) {
	
    if(pi.isQuestCompleted(22588)){
    	if(pi.getPlayer().getQuestInfo(22589) !== "1")pi.getPlayer().updateQuestInfo(22589, "1");
		pi.warp(914100010, 2);
    }else{
		pi.warp(922030000);
	}
}