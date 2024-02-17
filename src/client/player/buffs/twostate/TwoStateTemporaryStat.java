package client.player.buffs.twostate;

/**
 * TwoStateTemporaryStat
 * 
 * @author Eric
 */
public class TwoStateTemporaryStat extends TemporaryStatBase{

	public TwoStateTemporaryStat(boolean bDynamicTermSet){
		super(bDynamicTermSet);
	}

	@Override
	public int GetMaxValue(){
		return 0;
	}

	@Override
	public boolean IsActivated(){
		return nOption != 0;
	}
}