package client;

import java.math.BigInteger;

import tools.Randomizer;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 16, 2017
 *        A 'custom' CRand just to do basic seeding storing/calculations for damage.
 */
public class CRand{

	public long seed1, seed2, seed3;
	public long oldSeed1, oldSeed2, oldSeed3;

	/**
	 * Sets seeds 1-3.
	 */
	public void randomize(){
		seed1 = Randomizer.nextInt() | 0x100000;
		oldSeed1 = seed1;
		seed2 = Randomizer.nextInt() | 0x1000;
		oldSeed2 = seed2;
		seed3 = Randomizer.nextInt() | 0x10;
		oldSeed3 = seed3;
	}

	public long random(){
		long seed1 = this.seed1;
		long seed2 = this.seed2;
		long seed3 = this.seed3;
		this.oldSeed1 = seed1;
		this.oldSeed2 = seed2;
		this.oldSeed3 = seed3;
		long newSeed1 = (seed1 << 12) ^ (seed1 >> 19) ^ ((seed1 >> 6) ^ (seed1 << 12)) & 0x1FFF;
		long newSeed2 = 16 * seed2 ^ (seed2 >> 25) ^ ((16 * seed2) ^ (seed2 >> 23)) & 0x7F;
		long newSeed3 = (seed3 >> 11) ^ (seed3 << 17) ^ ((seed3 >> 8) ^ (seed3 << 17)) & 0x1FFFFF;
		this.seed1 = newSeed1;
		this.seed2 = newSeed2;
		this.seed3 = newSeed3;
		return (newSeed1 ^ newSeed2 ^ newSeed3) & 0xffffffffl;// & 0xffffffffl will help you convert long to unsigned int
	}

	private BigInteger EAX = new BigInteger("1801439851");// 0x6B5FCA6B; <= this is const

	public double RandomInRange(long randomNum, int max, int min){
		// java not have unsigned long, so i used BigInteger
		BigInteger ECX = new BigInteger("" + randomNum);// random number from Crand32::Random()
		// ECX * EAX = EDX:EAX (64bit register)
		BigInteger multipled = ECX.multiply(EAX);
		// get EDX from EDX:EAX
		long highBit = multipled.shiftRight(32).longValue();// get 32bit high
		long rightShift = highBit >>> 22;// SHR EDX,16
		double newRandNum = randomNum - (rightShift * 10000000.0);
		double value;
		if(min != max){
			if(min > max){// swap
				int temp = max;
				max = min;
				min = temp;
			}
			value = (max - min) * newRandNum / 9999999.0 + min;
		}else{
			value = max;
		}
		return value;
	}
}
