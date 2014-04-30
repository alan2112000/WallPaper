package com.AlanYu.Filter;

import android.util.Log;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.KStar;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

public class RandomForestClassifier extends AbstractFilter {

	public RandomForestClassifier() {
		this.setFeature();
		this.setOption();
		trainingData = new Instances("Rel", this.getFvWekaAttributes(), 1000);
		trainingData.setClassIndex(CLASS_INDEX_TOUCH);
	}

	@Override
	protected void setOption() {
		Log.d("set Option", "in seting option in classifier");
		try {
			randomF = new RandomForest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void testData() {
	}

	@Override
	public void trainingData() {
		Log.d("TrainingData", "in traininData phase.....");
		try {
			randomF.buildClassifier(trainingData);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void predictInstance(Instance currentInstance) {
		dataUnLabeled = new Instances("TestInstances",getFvWekaAttributes(),10);
		dataUnLabeled.add(currentInstance);
		dataUnLabeled.setClassIndex(dataUnLabeled.numAttributes()-1);
		double[] prediction;
		try {
			prediction = randomF.distributionForInstance(dataUnLabeled.firstInstance());
			   //output predictions
			System.out.println("\n Result RandomForest \n ====================\n");
	        for(int i=0; i<prediction.length; i++)
	        {
	            System.out.println("Probability of class "+
	                                trainingData.classAttribute().value(i)+
	                               " : "+Double.toString(prediction[i]));
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Classifier returnClassifier() {
		return randomF;
	}

}
