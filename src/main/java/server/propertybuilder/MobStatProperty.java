package server.propertybuilder;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 20, 2017
 */
public class MobStatProperty{

	public double expMultiplier = 1D, hpMultiplier = 1D, mpMultiplier = 1D, levelMultiplier = 1D;

	public MobStatProperty expMultiplier(double expMultiplier){
		this.expMultiplier = expMultiplier;
		return this;
	}

	public MobStatProperty hpMultiplier(double hpMultiplier){
		this.hpMultiplier = hpMultiplier;
		return this;
	}

	public MobStatProperty mpMultiplier(double mpMultiplier){
		this.mpMultiplier = mpMultiplier;
		return this;
	}

	public MobStatProperty levelMultiplier(double levelMultiplier){
		this.levelMultiplier = levelMultiplier;
		return this;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(expMultiplier);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(hpMultiplier);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(levelMultiplier);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mpMultiplier);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		MobStatProperty other = (MobStatProperty) obj;
		if(Double.doubleToLongBits(expMultiplier) != Double.doubleToLongBits(other.expMultiplier)) return false;
		if(Double.doubleToLongBits(hpMultiplier) != Double.doubleToLongBits(other.hpMultiplier)) return false;
		if(Double.doubleToLongBits(levelMultiplier) != Double.doubleToLongBits(other.levelMultiplier)) return false;
		if(Double.doubleToLongBits(mpMultiplier) != Double.doubleToLongBits(other.mpMultiplier)) return false;
		return true;
	}
}
