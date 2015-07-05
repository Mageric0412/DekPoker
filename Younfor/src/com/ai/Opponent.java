package com.ai;

public class Opponent {
//对手模型
	
	//注意
	/*
	 *  VPIP AF PFR 任意两项超过了规定值就是激进,任意两项低于了规定值就是保守
	 *  根据激进保守人数,选择不同的策略.
	 */
	//任何位置
	public double raisenum[]=new double[4]; //翻牌前,翻牌后,转牌,河牌 加注数
	public double foldnum[]=new double[4]; //翻牌前,翻牌后,转牌,河牌 弃牌数
	public double callnum[]=new double[4]; //翻牌前,翻牌后,转牌,河牌 跟注数
	//加注量
	public double raisebet[]=new double[4];//翻牌前,后,转牌,河牌,加注量
	public double getAveBet(int i)
	{
		return raisebet[i]/(raisenum[i]+1.1);
	}
	/*
	 * VPIP Voluntarily Put $ In Pot,The SB and BB postings are not voluntary unless they add more money eg SBcalling or BB calling or raising 主动往底池里投钱的频率，投小盲注和大盲注不计在内，除非他们增加了更多的钱，比如小盲注跟注，或者大盲注加注。
	 */
	/*
	 * <15% is very tight. These players are only playing super premium hands from early position and only still maintain a conservative range when in position.

	15% - 22% is tight. They will usually be a bit looser from early position then the <15% players, make notes on whether they play pocket pairs from early position, or whether they raise or limp. They will tend to have a much wider range in late position.
	
	22% - 30% is semi loose. They'll usually open all PPs and strong non pair hands, like suited broadways and strong aces, from early position. They will have a wide range from late position.
	
	30% - 40% is considerably loose. These players are usually playing far too many hands in all positions and should make for easy winnings.
	
	40% - 60% is maniac loose. They're playing all sorts of trash from every possible position, these are the type of players you really want at your table, though they are becoming much rarer these days.
	
	>60% is free money.
	 */
	public double VPIP=0;
	public double getVPIP()
	{
		double r=0,c=0;
		for(int i=0;i<4;i++)
		{
			r+=raisenum[i];
			c+=callnum[i]+raisenum[i]+foldnum[i];
		}
		if(c==0)
			return 0;
		return r/c;
	}
	/*
	 * Preflop raise % (PFR):【15%-19%】 
　　指某位牌手在翻牌前加注的频率。如果频率越高，那么对手的牌风也就越凶。将PFR同VPIP组合起来分析就是你捕获fish的主要数据。
 		计算方法: 统计翻牌前加注的次数   (raise+all_in)/(raise+fold+call+check)
	 */
	/*
	 * If their PFR is very small (<5%) then you don't need to worry about getting raised off marginal hands. If they do raise you can fold nearly all speculative hands unless you have the implied odds to call and stack them with a pocket pair for example, the deeper you're playing the wider the range of hands you can call with.

	If their PFR is <1/2 their VPIP, then this player is quite passive pre-flop and limping over 50% of hands they play.
	
	If PFR is between 50% - 75% of VPIP then they're raising more than they're limping, but they're not overly aggressive.
	
	Any player with a ratio >75% is raising the majority of their hands and they're playing aggressively pre-flop.
	 */
	public double PFR=0;
	public double getPFR()
	{
		double r=raisenum[0];
		double c=raisenum[0]+callnum[0]+foldnum[0];
		if(c==0)
			return 0;
		return r/c;
	}
	/*
	 * AF
	 * This is an indicator of post-flop aggression. It is calculated by the following formula (raise% + bet%)/(call%) post-flop. It's the ratio of times a player is aggressive against the times they're passive. You need at least 100 – 200 sample size of hands to be sure of this stat but more hands for tighter players.
	 <1.5 is passive, these players are calling a lot and betting/raising very little, a raise from these players usually means a strong hand. You can value bet lighter against these opponents because they tend to call with wide ranges.

	1.5 – 2.5 is about average. These players aren't overly aggressive post-flop but it's important to look at it in the context of their VPIP and other stats.
	
	2.5 – 3.5 is aggressive. Be prepared to assign a wider range to bets and raises.
	
	>3.5 is very aggressive. These players prefer to bet or raise rather then call and may do so lightly. Against these players it may be profitable to induce bluffs. As mentioned, in some cases a high AF can be an indication of a high fold%, so don't just assume they're raising you light.
		 */
	public double AF=0;
	public double getAF()
	{
		double r=0,c=0;
		for(int i=1;i<4;i++)
		{
			r+=raisenum[i];
			c+=callnum[i];
		}
		if(c==0&&r<=3)
			return 0;
		else if(c==0&&r>3)
			return 1.6;
		return r/c;
	}
}
