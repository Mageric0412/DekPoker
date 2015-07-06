package com.ai;

import java.io.IOException;
import java.util.concurrent.atomic.*;

import com.game.Card;
import com.game.State;
import com.util.Log;

public class ProbValue {
    private final int [] hand;
    private final int [] host;
    private final int [] deck;
    private final int hostlenght;
    private final int alivenum;
    private final int[] cardnumb;
    private final int[] cc;
    private final static int[] pokprime = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41 };
    private final static int[] doubleprime = { 4, 9, 25, 49, 121, 169, 289, 361, 529, 841, 961, 1369, 1681 };
    private final static int[] tribleprime={8, 27, 125, 343, 1331, 2197, 4913, 6859, 12167, 24389, 29791, 50653, 68921};
    private final static int[] kingprime={16, 81, 625, 2401, 14641, 28561, 83521, 130321, 279841, 707281, 923521, 1874161, 2825761};
    private final static int[] htstraight={210,1155,5005,17017,46189,96577,215441,392863,765049};
    private static int bigpair=12;
    private static int bigvalue=8;
    public ProbValue (int [] hand, int numOther, int [] partialCommunity) throws IOException {
        this.hand = hand.clone();
        this.host = new int[7];
        this.hostlenght = partialCommunity.length;
        for (int i = 0; i < hostlenght; i ++)
            this.host[i] = partialCommunity[i];
        this.deck = new int[52];
        PokerLib.init_deck(this.deck);
        this.alivenum = numOther;
    }

    //turnpower
    public double getProb1()
    {
    	double small=0,equal=0,big=0;
    	long timestart=System.currentTimeMillis();
    	int count=0;
    	int mycards[]=new int[hostlenght+3];
    	int oppocards[]=new int[hostlenght+3];
    	//我的牌加上公牌
    	mycards[0]=hand[0];
    	mycards[1]=hand[1];
    	for(int i=0;i<hostlenght;i++)
    	{
    		mycards[i+2]=host[i];
    		oppocards[i+2]=host[i];
    	}

    	for(int i=0;i<51;i++)
    	{
    		 //除去公牌重复
			boolean repeat=false;
			for(int k=0;k<hostlenght;k++)
			{
				if(host[k]==deck[i])
				{
					repeat=true;
					break;
				}
			}
			//去除手牌重复
			if(hand[0]==deck[i])
				repeat=true;
			if(repeat)
				continue;

    		for(int j=i+1;j<52;j++)
    		{
    			//除去公牌重复
    			repeat=false;
    			for(int k=0;k<hostlenght;k++)
    			{
    				if(host[k]==deck[j])
    				{
    					repeat=true;
    					break;
    				}
    			}
    			//去除手牌重复
    			if(hand[1]==deck[j])
    				repeat=true;
    			if(repeat)
    				continue;

    			//对手手牌
    			oppocards[0]=deck[i];
    			oppocards[1]=deck[j];
    			//去除垃圾牌
    			if(getPower(oppocards)<6)
    			{
    				continue;
    			}

          for(int i=0;i<51;i++)
          {
            boolean repeat=false;
            for(int k=0;k<hostlenght;k++)
      			{
      				if(host[k]==deck[i])
      				{
      					repeat=true;
      					break;
      				}
      			}
      			//去除手牌重复
      			if(hand[0]==deck[i])
      				repeat=true;
            if(hand[1]==deck[i])
        			repeat=true;

          if(oppocards[0]==deck[i])
                repeat=true;
           if(oppocards[1]==deck[i])
                repeat=true;
          if(repeat)
            continue;
            mycards[5]=deck[i];
            oppocards[5]=deck[i];

            double myval=getValue(mycards);

            double oppoval=getValue(oppocards);

            if(myval==oppoval)
              equal++;
            else if(myval>oppoval)
              small++;
            else
              big++;
            //超时保护
            if(count++%20==0)
            {
              if(System.currentTimeMillis()-timestart>200)
              {
                debug("timeover");
                return  (big+equal/2)/(big+small+equal);
              }
            }

          }
    		}

    	}


    	return (big+equal/2)/(big+small+equal);
    }


    //floppower
    public double getProb()
    {
    	double small=0,equal=0,big=0;
    	long timestart=System.currentTimeMillis();
    	int count=0;
    	int mycards[]=new int[hostlenght+2];
    	int oppocards[]=new int[hostlenght+2];
    	//我的牌加上公牌
    	mycards[0]=hand[0];
    	mycards[1]=hand[1];
    	for(int i=0;i<hostlenght;i++)
    	{
    		mycards[i+2]=host[i];
    		oppocards[i+2]=host[i];
    	}
    	double myval=getValue(mycards);
    	for(int i=0;i<51;i++)
    	{
    		 //除去公牌重复
			boolean repeat=false;
			for(int k=0;k<hostlenght;k++)
			{
				if(host[k]==deck[i])
				{
					repeat=true;
					break;
				}
			}
			//去除手牌重复
			if(hand[0]==deck[i])
				repeat=true;
			if(repeat)
				continue;
    		for(int j=i+1;j<52;j++)
    		{
    			//除去公牌重复
    			repeat=false;
    			for(int k=0;k<hostlenght;k++)
    			{
    				if(host[k]==deck[j])
    				{
    					repeat=true;
    					break;
    				}
    			}
    			//去除手牌重复
    			if(hand[1]==deck[j])
    				repeat=true;
    			if(repeat)
    				continue;
    			//对手手牌
    			oppocards[0]=deck[i];
    			oppocards[1]=deck[j];
    			//去除垃圾牌
    			if(getPower(oppocards)<6)
    			{
    				continue;
    			}
    			double oppoval=getValue(oppocards);
    			if(myval==oppoval)
    				equal++;
    			else if(myval>oppoval)
    				small++;
    			else
    				big++;
    			//超时保护
    			if(count++%20==0)
    			{
    				if(System.currentTimeMillis()-timestart>200)
    				{
    					debug("timeover");
    					return  (big+equal/2)/(big+small+equal);
    				}
    			}
    		}
    	}
    	return (big+equal/2)/(big+small+equal);
    }
    public double getValue(int card[])
    {
    	if(card.length==5)
    		return PokerLib.eval_5hand(card);
    	else if(card.length==6)
    		return PokerLib.eval_6hand(card);
    	else
    		return PokerLib.eval_7hand(card);
    }
    /**
	 * 根据编码值，解码获得牌值
	 * @param value	编码值
	 * @return		牌值
	 */
	public static int getNumByValue(int value){
		value = value>>16;
		for (int i = 0; i < 13; i++) {
			if ((value&0x01)!=0) {
				return (i+2);
			}
			else {
				value = value>>1;
			}
		}
		return 2;
	}
	/**
	 * 根据编码值，解码获得牌的花色
	 * @param value	编码值
	 * @return		花色
	 */
	public static int getColorByValue(int value) {
		value = value>>12;
		for (int i = 0; i < 4; i++) {
			if ((value&0x01)!=0) {
				return i;
			}
			else {
				value = value>>1;
			}
		}
		return 0;
	}
	/**
	 * 对两张手牌进行评估
	 * @param OurTwoCards	两张底牌
	 * @return              两张牌的牌力评估值
	 */
	public static double getPower(int[] hand){
		float result = 0;
		float result_temp = 0;
		int n1 = getNumByValue(hand[0]);
		int n2 = getNumByValue(hand[1]);

		int c1 = getColorByValue(hand[0]);
		int c2 = getColorByValue(hand[1]);

		int big=n1>n2?n1:n2;
		switch (big) {
		case 14:{
			result_temp = 10;
		}break;
		case 13:{
			result_temp = 8;
		}break;
		case 12:{
			result_temp = 7;
		}break;
		case 11:{
			result_temp = 6;
		}break;
		default:{
			result_temp = (float) (big/2.0);
		}break;
		}
		if (n1==n2) {	//对子
			result = result_temp*2;
			if (result<5) {
				result = 5;
			}
			return result;
		}
		else {		//非对子
			if (c1==c2) {	//同种花色
				result = result_temp+2;
			}
			int off = Math.abs(n1-n2);
			switch (off) {
			case 1:{
				result = result_temp+0;
				if ((n1<12)&&(n2<12)) {
					result += 1;
				}
			}break;
			case 2:{
				result = result_temp - 1;
				if ((n1<12)&&(n2<12)) {
					result += 1;
				}
			}break;
			case 3:{
				result = result_temp - 2;
			}break;
			case 4:{
				result = result_temp - 4;
			}break;
			default:{
				result = result_temp - 5;
			}break;

			}
		}
		return result;
	}

  /*
  对当前公共牌与手牌组合进行判断
  @return   返回行动代码，
  preflop:
  0表示大对，1表示小对，
  2表示非同花大牌，
  3表示同花相连，4表示同花Ax,
  6表示同花隔1牌、12隔2，5表示同花大牌KX/QJ
  flop:
  中小对、
  9大牌
  7表示差顺（两头）差花，
  turn:
  8表示中小的最大公牌对
  11小两对

  10其他
  */

  public static int getPower0(int[] card,int curState){

    int cardlength=card.length;
    int h1 = getNumByValue(card[0]);
		int h2 = getNumByValue(card[1]);
    int sumprime=1; //牌值的素数乘积
    int pairnum=0;
    int maxflopvalue=0;
    int maxpairvalue=0;
    boolean headtile=false;
    boolean flushing=false;
		int hc1 = getColorByValue(card[0]);
		int hc2 = getColorByValue(card[1]);
    int value=getPower(new int[]{card[0],card[1]});
    int big=h1>h2?h1:h2;
    int small=h1>h2?h2:h1;
    //preflop
    if(curState==40)
    {
      //对子
      if(h1==h2)
      {
           if（value>=bigpair）
           return 0;

         return 1;
      }
      //非同花
      else if(hc1!=hc2)
      {
         if(value>=bigvalue)
         return 2;
      }
      //同花
      else if(hc1==hc2)
      {
        //同花Ax

        if(big==14)
         return 4;

        else if(big==13||(big==12&&small==11))
         return 5;

        else if((big-small)==1)//同花相连
        return 3;
         else if(big-small==2) //最多相隔1
          return 6;
          else if(big-small==3) //最多相隔2
           return 12;
      }
      else
      return 10;
    }
    //flop
    else if((curState==41)&&(cardlength==5))
    {
      cardnumb=new int[5];
      cc=new int[5];

      for(int i=0;i<5;i++)
        cardnumb[i]=getNumByValue(card[i])-2;

     //获取最大公牌
      for(int i=2;i<5;i++)
       if(maxflopvalue<getNumByValue(card[i]))
           maxflopvalue=getNumByValue(card[i]);

     //所有牌的素数乘积
      for(int i=0;i<5;i++)
        sumprime*=pokprime[cardnumb[i]];

    //是否有差顺牌
      for(int i=0;i<htstraight.length;i++)
         if(sumprime%htstraight[i]==0)
         {
             headtile=true;
             return;
         }

    //是否差同花
    for(int i=0;i<5;i++)
     cc[i]=(card[i]>>12)&0x0F;
    if(((cc[0]&cc[1]&cc[2]&cc[3])!=0)||((cc[0]&cc[1]&cc[2]&cc[4])!=0)||((cc[0]&cc[1]&cc[3]&cc[4])!=0)||((cc[0]&cc[2]&cc[3]&cc[4])!=0)||(cc[1]&cc[2]&cc[3]&cc[4]))
       flushing=true;


     //一个对子
      for(int i=0;i<doubleprime.length;i++)
       if(sumprime%doubleprime[i]==0)
        {
          pairnum++;
          if(maxpairvalue<(i+2))
           maxpairvalue=i+2;
        }

      if(pairnum==1&&maxpairvalue>=maxflopvalue)
        return 0;
      else if(pairnum==1&&maxpairvalue<maxflopvalue)
        return 1;  //翻牌后仅仅有一个中小对，当做小对处理

      else if(headtile||flushing)
        return 7;

      else if(big>maxflopvalue&&small>maxflopvalue)
       return 9;
    }

    //turn
    else if((curState==42)&&(cardlength==6))
     {
       cardnumb=new int[6];
       cc=new int[6];
       for(int i=0;i<6;i++)
         cardnumb[i]=getNumByValue(card[i])-2;

      //获取最大公牌
      for(int i=2;i<6;i++)
      if(maxflopvalue<getNumByValue(card[i]))
        maxflopvalue=getNumByValue(card[i]);

      //所有牌的素数乘积
      for(int i=0;i<6;i++)
       sumprime*=pokprime[cardnumb[i]];

      //仍然没有两对
       for(int i=0;i<doubleprime.length;i++)
        if(sumprime%doubleprime[i]==0)
         {
           pairnum++;
           if(maxpairvalue<(i+2))
            maxpairvalue=i+2;
         }

     //是否有差顺牌
      for(int i=0;i<htstraight.length;i++)
        if(sumprime%htstraight[i]==0)
          {
              headtile=true;
              return;
          }

         //是否差同花
         for(int i=0;i<5;i++)
          cc[i]=(card[i]>>12)&0x0F;
         if(((cc[0]&cc[1]&cc[2]&cc[3])!=0)||((cc[0]&cc[1]&cc[2]&cc[4])!=0)||((cc[0]&cc[1]&cc[3]&cc[4])!=0)||((cc[0]&cc[2]&cc[3]&cc[4])!=0)||(cc[1]&cc[2]&cc[3]&cc[4]))
            flushing=true;

         if(pairnum==2&&big<maxflopvalue)
          return 11;
         else if(pairnum==1&&big>=maxflopvalue)
         return 0;
         else if(flushing||headtile)
         return 7;
         else if(pairnum==1&&big<maxflopvalue)
         return 1;
         else if(big>maxflopvalue&&small>maxflopvalue)
         return 9;
     }

     else
      return 10;
    //river

  }

    public static void main(String []args) throws IOException
    {
    	PokerLib.init();
    	ProbValue p=new ProbValue(new int[]{1,2},2, new int[]{1,2,3});
    	Card c1=new Card("SPADES","9");
    	//Card c2=new Card("SPADES","8");
    	Card c2=new Card("HEARTS","9");
    	System.out.println(p.getPower(new int[]{c1.getValue(),c2.getValue()}));
    	c1=new Card("SPADES","6");
    	c2=new Card("SPADES","7");
    	System.out.println(p.getPower(new int[]{c1.getValue(),c2.getValue()}));
    }
    public void debug(String s) {
		try {
			Log.getIns("8888").log(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
