/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package server;

import client.inventory.Item;

/**
 * @author Matze
 */
public class MaplePlayerShopItem{

	private Item item;
	private int price;
	private boolean doesExist;

	public MaplePlayerShopItem(Item item, short perBundle, int price){
		this.item = item;
		this.item.setPerBundle(perBundle);
		this.price = price;
		this.doesExist = true;
	}

	public void setDoesExist(boolean tf){
		this.doesExist = tf;
	}

	public boolean isExist(){
		return doesExist;
	}

	public Item getItem(){
		return item;
	}

	public short getBundles(){
		return item.getQuantity();
	}

	public void setBundles(short bundles){
		item.setQuantity(bundles);
	}

	public short getPerBundle(){
		return item.getPerBundle();
	}

	public void setPerBundle(short perBundle){
		item.setPerBundle(perBundle);
	}

	public int getPrice(){
		return price;
	}
}