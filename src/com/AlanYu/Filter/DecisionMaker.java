package com.AlanYu.Filter;

import android.util.Log;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Vote;
import weka.core.FastVector;
import weka.core.Instances;

public class DecisionMaker {

	public static final int TRAINING = 0;
	public static final int TEST = 1;
	public static final int COLLECT = 2;
	public static final int IS_OWNER = 0;
	public static final int IS_OTHER = 1;

	private J48Classifier j48;
	private kNNClassifier knn;
	private KStarClassifier kstar;
	private DecisionTableFilter dt;
	private RandomForestClassifier randomF;

	public DecisionMaker() {
		j48 = new J48Classifier();
		knn = new kNNClassifier();
		kstar = new KStarClassifier();
		dt = new DecisionTableFilter();
		randomF = new RandomForestClassifier();
	}

	public void addDataToTraining(Instances trainingData) {
		Log.d("DecisionMaker", "set trainging data");
		j48.setTrainingData(trainingData);
		knn.setTrainingData(trainingData);
		kstar.setTrainingData(trainingData);
		dt.setTrainingData(trainingData);
		randomF.setTrainingData(trainingData);
	};

	public void buildClassifier() {
		Log.d("DecisionMaker", "build classifier");
		j48.trainingData();
		knn.trainingData();
		kstar.trainingData();
		dt.trainingData();
		randomF.trainingData();
	};

	public int getFinalLabel(Instances unLabelData) {
		return predictionInstances(unLabelData);
	}

	// caculate how many label is owner in the unLabelData
	public int predictionInstances(Instances unLabelData) {
		int ownerLabelNumber = 0;
		int otherLabelNumber = 0;
		Log.d("DecisionMaker", "Predcting Label");
		Vote vote = new Vote();
		Classifier cls[] = { j48.returnClassifier(), knn.returnClassifier(),
				kstar.returnClassifier(), dt.returnClassifier(),
				randomF.returnClassifier() };
		vote.setClassifiers(cls);
		double prediction[] = null;
		for (int i = 0; i < unLabelData.numInstances(); i++) {
			try {
				prediction = vote.distributionForInstance(unLabelData
						.instance(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (prediction[0] > prediction[1]) {
				ownerLabelNumber++;
			}
			otherLabelNumber++;
		}
		Log.d("DecisionMaker","owner :"+Double.toString(ownerLabelNumber) + "other : "+Double.toString(otherLabelNumber));
		if (ownerLabelNumber > otherLabelNumber)
			return IS_OWNER;
		else
			return IS_OTHER;
	}
	
	public FastVector getWekaAttributes(){
		return j48.getFvWekaAttributes();
	}

}
