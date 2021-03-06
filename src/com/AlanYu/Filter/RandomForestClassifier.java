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
		this.classifierName = "RandomForest";
		trainingData = new Instances("Rel", this.getFvWekaAttributes(), 1000);
		trainingData.setClassIndex(CLASS_INDEX_TOUCH);
	}

	@Override
	protected void setOption() {
		Log.d("set Option", "in seting option in classifier");
		String[] options = null ;
		try {
			options = weka.core.Utils.splitOptions("-I 10 -K 0 -S 1");
			randomF = new RandomForest();
			randomF.setOptions(options);
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
		dataUnLabeled.add(currentInstance);
		double[] prediction;
		try {
			prediction = randomF.distributionForInstance(dataUnLabeled.firstInstance());
			   //output predictions
			printResult(prediction);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataUnLabeled.remove(0);
	}

	@Override
	public Classifier returnClassifier() {
		return randomF;
	}

}
