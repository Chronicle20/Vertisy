package scripting.npc;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 22, 2017
 */
public enum ScriptMessageType{
    // http://forum.ragezone.com/f427/christmas-v83-v90-nexons-scriptman-1089193/
	Say(0),
	SayImage(1),
	AskYesNo(2),
	AskText(3),
	AskNumber(4),
	AskMenu(5),
	AskQuiz(6),
	AskSpeedQuiz(7),
	AskAvatar(8),
	AskMemberShopAvatar(9),
	AskPet(10),
	AskPetAll(11),
	AskScript(12),
	AskAccept(13),
	AskBoxText(14),
	AskSlideMenu(15),
	AskCenter(16);

	private final int nMsgType;

	private ScriptMessageType(int nMsgType){
		this.nMsgType = nMsgType;
	}

	public int getMsgType(){
		return nMsgType;
	}

	public static ScriptMessageType getType(int nMsgType){
		for(ScriptMessageType messageType : ScriptMessageType.values()){
			if(messageType.getMsgType() == nMsgType) return messageType;
		}
		return null;
	}
}