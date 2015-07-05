package com.ai;

import java.io.IOException;
import java.util.concurrent.atomic.*;

import com.game.State;
import com.util.Log;

public class ProbMCT implements Runnable {
    private final int [] hand;
    private final int [] host;
    private final int [] deck;
    private final int hostlenght;
    private final int alivenum;
    private final long stop;
    private final AtomicInteger won;
    private final AtomicInteger total;
    private final int [] curDeck;
    public ProbMCT (int [] hand, int numOther, int [] partialCommunity,
                             long stop, AtomicInteger won, AtomicInteger total) throws IOException {
        this.hand = hand.clone();
        this.host = new int[7];
        this.hostlenght = partialCommunity.length;
        for (int i = 0; i < hostlenght; i ++)
            this.host[i] = partialCommunity[i];
        this.deck = new int[52];
        PokerLib.init_deck(this.deck);
        this.alivenum = numOther;
        this.stop = stop;
        this.won = won;
        this.total = total;
        this.curDeck = new int[52];

    }
    /*
    public static void main(String args[]) throws IOException
    {
    	PokerLib.init();
    	final AtomicInteger won =new AtomicInteger(0);
    	final AtomicInteger total=new AtomicInteger(0);
    	MCT m=new MCT(new int[]{73730,98306}, 2, new int[]{139523,164099,81922}, 180+System.currentTimeMillis(), won, total);
    	new Thread(m).start();
    }*/
    public boolean tryReplace () throws IOException {

        if (Math.random() < 0.04)
            return true;

        // Instantiate curDeck
        for (int i = 0; i < 52; i ++)
            curDeck[i] = deck[i];
        PokerLib.shuffle_deck(curDeck);
        //System.out.printf("%d %d\n", hand[0], hand[1]);
        for (int i = 0; i < 52; i ++) {
            //System.out.printf("%d %s\n", curDeck[i], PokerLib.cardToString(curDeck[i]));
            if (curDeck[i] == hand[0] || curDeck[i] == hand[1]) {
                curDeck[i] = -1;
                continue;
            }
            for (int j = 0; j < hostlenght; j ++) {
                if (curDeck[i] == host[j]) {
                    curDeck[i] = -1;
                    break;
                }
            }
        }

        int curPos = 0, bestVal = 9999;

        // Generate missing community cards
        for (int i = hostlenght; i < 5; i ++) {
            while (curDeck[curPos] == -1)
                curPos ++;
            host[i] = curDeck[curPos];
            curDeck[curPos] = -1;
        }

        // Evaluate opponent hands
        for (int i = 0; i < alivenum; i ++) {
        	if(Math.random()<State.enterprob)
        	{
	            for (int j = 5; j < 7; j ++) {
	                while (curDeck[curPos] == -1)
	                    curPos ++;
	                host[j] = curDeck[curPos];
	                curDeck[curPos] = -1;
	            }
        	}else
        	{
	        	while(curDeck[curPos]==-1||curDeck[curPos+1]==-1||
	        			( PokerLib.findgoodhand(curDeck[curPos+1], curDeck[curPos])==false&&PokerLib.findgoodhand(curDeck[curPos], curDeck[curPos+1])==false) )
	            	curPos++;
	        	 host[5] = curDeck[curPos];
	             curDeck[curPos] = -1;
	             host[6] = curDeck[curPos+1];
	             curDeck[curPos+1] = -1;
        	}
            int val = PokerLib.eval_7hand(host);
            if (val < bestVal) {
                bestVal = val;
            }
        }

        // Evaluate my hand
        host[5] = hand[0];
        host[6] = hand[1];
        int myVal = PokerLib.eval_7hand(host);
        // Return true if win
        if (myVal < bestVal) {
            return true;
        } else if (myVal == bestVal) {
            return Math.random() * (1 + alivenum) < 1;
        }
        return false;
    }
    public void run () {
        for (int i = 0;; i ++) {
            if (i % 10 == 0) {
                if (System.currentTimeMillis() >= stop)
                {
                	//System.out.println(won.doubleValue()/total.doubleValue());
                    return;
                }
            }
            total.getAndIncrement();
            try {
				if (tryReplace())
				    won.getAndIncrement();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
