package com.AlanYu.Filter;

public abstract class AbstractFilter {

	private boolean IS_TRAINING = true; 
	
	public boolean isTraining() {
		return IS_TRAINING;
	}

	public AbstractFilter() {
	}

	abstract void loadData();
	abstract float  testData(); 
	abstract void trainingData();

	public boolean isIS_TRAINING() {
		return IS_TRAINING;
	}

	public void setIS_TRAINING(boolean iS_TRAINING) {
		IS_TRAINING = iS_TRAINING;
	}
	
	
}


