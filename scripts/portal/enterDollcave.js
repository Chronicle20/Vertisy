function enter(pi) {
	if(pi.isQuestStarted(22549) || pi.isQuestStarted(22550) || pi.isQuestStarted(22553))pi.warp(910050300, 1);
	else if(pi.isQuestStarted(22553) || (!pi.isQuestStarted(22553) && !pi.isQuestCompleted(22553)))pi.warp(910050300, 1);
	pi.enableActions();
	return true;
}