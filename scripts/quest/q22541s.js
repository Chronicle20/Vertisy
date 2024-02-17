var status = -1;

function start(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        qm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		qm.sendSay("Are you here to borrow a book? Glad to see you're pushing yourself to learn more. Alight, then. Which book would you like?", false, true);
	}else if(status == 1){
		qm.sendSayUser("I'd like to see the second part of #bDragon Types and Characteristics (Vol I)", true, true);
	}else if(status == 2){
		qm.sendSay("Ah... You must be talking about the book that was published in leafre. Well, I know I loaned Vol. I of that book to a young man from Henesys and Vol. II was... Oh no, I think someone borrowed that as well.", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bWHAT? Someone borrowed it? Who?", true, true);
	}else if(status == 4){
		qm.sendSay("You know #bIcarus#k from #bKerning City#k? He's always dreamt of flying. He borrowed the book a while ago and still hasn't return it... Hm...", true, true);
	}else if(status == 5){
		qm.sendSayUser("#bUh, when do you think he'll return it?", true, true);
	}else if(status == 6){
		qm.sendAcceptDecline("Well, we don't have set due dates for books borrowed here in Magic Library. If you want, you could go to Kerning City yourself and get the book from Icarus, then return the book to me after you've read it. What do you say?");
	}else if(status == 7){
		qm.forceStartQuest();
		qm.sendSay("You win, because you get to read the book you're looking for, and I win, because I get my book back. Good idea, right? It's interesting to see someone so young show interest in dragons, nto to mention the intiguing little lizard that follows you everywhere. Do you mind me asking what type of a lizard that is?", false, true);
	}else if(status == 8){
		qm.sendSayUser("#b(You can't let anyone know that Mir is actually a dragon. Don't say a word!!)", true, true);
	}else if(status == 8){
		qm.sendSay("Fine, fine. I get it! You don't have to shake your head so adamantly! Let's just worry about the book then, okay?", true, false);
		qm.dispose();
	}
}