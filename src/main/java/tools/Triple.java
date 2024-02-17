/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.Serializable;

/**
 * @author iPoopMagic (David)
 * @param <L> Left element type
 * @param <M> Middle element type
 * @param <R> Right element type
 */
public class Triple<L, M, R> implements Serializable{

	private static final long serialVersionUID = 1L;
	public L left;
	public M mid;
	public R right;

	public Triple(L left, M mid, R right){
		this.left = left;
		this.mid = mid;
		this.right = right;
	}

	public L getLeft(){
		return left;
	}

	public M getMiddle(){
		return mid;
	}

	public R getRight(){
		return right;
	}

	@Override
	public String toString(){
		return left.toString() + ":" + mid.toString() + ":" + right.toString();
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((mid == null) ? 0 : mid.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(obj instanceof Triple<?, ?, ?>){
			final Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
			return equals(getLeft(), other.getLeft()) && equals(getMiddle(), other.getMiddle()) && equals(getRight(), other.getRight());
		}
		return false;
	}

	private boolean equals(final Object object1, final Object object2){
		return !(object1 == null || object2 == null) && (object1 == object2 || object1.equals(object2));
	}
}
