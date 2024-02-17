package client.player.boss;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 18, 2017
 */
public enum BossEntryType{
	CAPTAIN_LAT(2, 24),
	PAPULATUS(2, 24),
	ZAKUM(2, 24),
	SCARGA_TARGA(2, 24),
	HORNTAIL(2, 24),
	PINK_BEAN(2, 24);

	private int entries, reset;

	private BossEntryType(int entries, int reset){
		this.entries = entries;
		this.reset = reset;
	}

	public int getEntries(){
		return entries;
	}

	/**
	 * @return How many hours before the entry resets.
	 */
	public int getReset(){
		return reset;
	}
}
