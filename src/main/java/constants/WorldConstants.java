package constants;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 29, 2016
 */
public class WorldConstants{

	// meme
	public enum WorldInfo{
		SCANIA(true, true, 2, 5, 3, 1, 1, 3, "", "", ""),
		BERA(false, false, 1, 4, 2, 1, 1, 2, "", "", ""),;

		private boolean enabled, selectable;
		private int expRate, questExpRate, mesoRate, dropRate;
		private final int flag, channels;
		private final String serverMessage, eventMessage, recommendedMessage;

		// Flag types: 0 = nothing, 1 = event, 2 = new, 3 = hot
		private WorldInfo(boolean enabled, boolean selectable, int channels, int expRate, int questExpRate, int mesoRate, int dropRate, int flag, String serverMessage, String eventMessage, String recommendedMessage){
			this.setEnabled(enabled);
			this.setSelectable(selectable);
			this.channels = channels;
			this.expRate = expRate;
			this.questExpRate = questExpRate;
			this.mesoRate = mesoRate;
			this.dropRate = dropRate;
			this.flag = flag;
			this.serverMessage = serverMessage;
			this.eventMessage = eventMessage;
			this.recommendedMessage = recommendedMessage;
		}

		public boolean isEnabled(){
			return enabled;
		}

		public void setEnabled(boolean enabled){
			this.enabled = enabled;
		}

		public boolean isSelectable(){
			return selectable;
		}

		public void setSelectable(boolean selectable){
			this.selectable = selectable;
		}

		public int getChannels(){
			return channels;
		}

		public int getExpRate(){
			return expRate;
		}

		public void setExpRate(int expRate){
			this.expRate = expRate;
		}

		public int getQuestExpRate(){
			return questExpRate;
		}

		public void setQuestExpRate(int questExpRate){
			this.questExpRate = questExpRate;
		}

		public int getMesoRate(){
			return mesoRate;
		}

		public void setMesoRate(int mesoRate){
			this.mesoRate = mesoRate;
		}

		public int getDropRate(){
			return dropRate;
		}

		public void setDropRate(int dropRate){
			this.dropRate = dropRate;
		}

		public int getFlag(){
			return flag;
		}

		public String getServerMessage(){
			return serverMessage;
		}

		public String getEventMessage(){
			return eventMessage;
		}

		public String getRecommendedMessage(){
			return recommendedMessage;
		}
	}
}
