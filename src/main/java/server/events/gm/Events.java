package server.events.gm;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 25, 2017
 */
public enum Events{
	OLA(Ola.class, 109030001, 109030101, 109030201, 109030301, 109030401);

	/**
	 * if(mapid == 60000){
	 * sb.append("Paul!");
	 * }else if(mapid == 104000000){
	 * sb.append("Jean!");
	 * }else if(mapid == 200000000){
	 * sb.append("Martin!");
	 * }else if(mapid == 220000000){
	 * sb.append("Tony!");
	 * }else{
	 * return null;
	 * }
	 */
	private Class<? extends Event> eventClass;
	private int[] maps;

	private Events(Class<? extends Event> eventClass, int... maps){
		this.eventClass = eventClass;
		this.maps = maps;
	}

	public Class<? extends Event> getEventClass(){
		return eventClass;
	}

	public int[] getMaps(){
		return maps;
	}
}
