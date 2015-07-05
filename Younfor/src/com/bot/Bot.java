package com.bot;

import com.game.State;

public interface Bot {
	public int getBestAction (State state, long timeMS); 
}
