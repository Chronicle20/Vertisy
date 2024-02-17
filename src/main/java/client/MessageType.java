package client;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 18, 2016
 */
public enum MessageType{
    // All ones in 117
    /*public static final int GREEN_CLEAR = -9;
    public static final int GREEN_PERM = -8;
    public static final int GREEN_FADE = -7;
    public static final int GM_TEXT = -6;
    public static final int PROFESSION = -5;
    public static final int CHAT_HIDDEN = -4;
    public static final int CHAT = -3;
    public static final int SHOP = -2;
    public static final int YELLOW_FADE = -1;*/
    /*public static final int NOTICE = 0;
    public static final int POPUP = 1;
    public static final int LIGHTBLUE = 2, MEGAPHONE = 2;
    public static final int SUPER_MEGAPHONE = 3;
    public static final int SERVERNOTICE = 4;
    public static final int ERROR = 5, PINK = 5;
    public static final int SYSTEM = 6, LIGHTBLUE2 = 6;
    public static final int MAPLETIP = 255;// placeholder value
    */
	/**
	 * Blue, automatically prefixed with [Notice]
	 */
	NOTICE(0),
	/**
	 * Annoying as fuck popup message
	 */
	POPUP(1),
	/**
	 * Light blue text used for megaphones
	 */
	MEGAPHONE(2),
	SUPER_MEGAPHONE(3),
	SERVER_NOTICE(4),
	/**
	 * Brown
	 */
	ERROR(5),
	/**
	 * Blue
	 */
	SYSTEM(6),
	HeartSpeaker(19),
	SkullSpeaker(20),
	/**
	 * Yellow text used for Maple Tips
	 */
	MAPLETIP(-1),
	/**
	 * Gold text in middle of screen
	 */
	TITLE(-2);

	/**
	 * public static final int
	 * Notice = 0,
	 * Alert = 1,
	 * SpeakerChannel = 2,
	 * SpeakerWorld = 3,
	 * Slide = 4,
	 * Event = 5,
	 * NoticeWithoutPrefix = 6,
	 * UtilDlgEX = 7,
	 * ItemSpeaker = 8,
	 * SpeakerBridge = 9,
	 * ArtSpeakerWorld = 10,
	 * BlowWeather = 11,
	 * GachaponAnnounce = 12,
	 * GachaponAnnounce_Open = 13,
	 * GachaponAnnounce_Copy = 14,
	 * UListClip = 15,
	 * FreeMarketClip = 16,
	 * DestroyShop = 17,
	 * CashShopAD = 18,
	 * HeartSpeaker = 19,
	 * SkullSpeaker = 20,
	 * // KMS Exclusives
	 * GachaponMsg = 12,
	 * AnnouncedQuest_Open = 13,
	 * AnnouncedQuest_Closed = 14,
	 * MiracleTime = 15,
	 * EventMsg_With_Channel = 16,
	 * LotteryItemSpeaker = 17,
	 * LotteryItemSpeaker_World = 18,
	 * MonsterLife_WorldMsg = 19,
	 * NoticeWindow = 20,
	 * PickupItem_World = 21,
	 * MakingSkillMeisterItem = 22,
	 * SpeakerWorld_GuildSkill = 23,
	 * WeatherMsg = 24,
	 * NO = 20
	 * ;
	 */
	private final int value;

	private MessageType(int value){
		this.value = value;
	}

	public int getValue(){
		return value;
	}

	public static MessageType getType(int value){
		for(MessageType type : values()){
			if(type.value == value) return type;
		}
		return NOTICE;
	}
}
