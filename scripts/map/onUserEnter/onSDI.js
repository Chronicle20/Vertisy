function start(ms) {
	var questinfo = ms.getPlayer().getQuestInfo(22580)
    if(questinfo.length == 0) ms.getPlayer().updateQuestInfo(22580, "" + 1);
}