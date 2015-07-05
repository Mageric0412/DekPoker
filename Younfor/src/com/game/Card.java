package com.game;

import java.io.IOException;

import com.ai.PokerLib;
import com.util.Log;

public class Card {
	public String color, point;
	public static void main(String []args)
	{
		PokerLib.init();
		Card c1=new Card("SPADES","A");
		int value=c1.getValue();
		value = value>>16;
		for (int i = 0; i < 13; i++) {
			if ((value&0x01)!=0) {
				System.out.println(i+2);
				return;
			}
			else {
				value = value>>1; 
			}
		}
	}
	public Card(String color, String point) {
		super();
		this.color = color;
		this.point = point;
	}
	
	public int getValue()
	{
		//Log.getIns("1111").log("value:"+point+color.charAt(0));
		if(point.equals("10"))
			point="T";
		return PokerLib.stringToCard(point+color.charAt(0));
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public int getRank() 
	{
		if(point.charAt(0)<='9'&&point.charAt(0)>='2')
			return point.charAt(0)-'0';
		if(point.equals("10"))
			return 10;
		switch(point.charAt(0))
		{
		case 'J': return 11;
		case 'Q':return 12;
		case 'K':return 13;
		case 'A': return 14;
		}
		return -1;
	}
	public int getSuit()
	{
		if(color.equals("SPADES"))
			return 0;
		if(color.equals("HEARTS"))
			return 1;
		if(color.equals("DIAMONDS"))
			return 2;
		if(color.equals("CLUBS"))
			return 3;
		return -1;
	}
	public String getPoint() {
		return point;
	}
	public void setPoint(String point) {
		this.point = point;
	}
	/*
	public static void main(String []args)
	{
		System.out.println(PokerLib.stringToCard("3S"));
	}
	*/
}
