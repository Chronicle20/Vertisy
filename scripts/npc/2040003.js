/** @Author: iPoopMagic (David)
 *	@Name: Assistant Cheng
 *	@Map: Ludibrium - Toy Factory <Process 1> Zone 1 (220020000)
 *	@Description: Missing Mechanical Parts (Quest: 3239)
*/
function start() {
	if(cm.isQuestStarted(3239)) {
		cm.warp(922000000, 0);
	} else {
		cm.sendNext("Thanks to you, the Toy Factory is running smoothly again. I'm so glad you came to help us out. We've been keeping an extra eye on all of our parties, so don't worry about it. Well then, I need to get back to work!");
	}
	cm.dispose();
}