package com.AlanYu.Filter;

import android.util.Log;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.KStar;
import weka.classifiers.rules.DecisionTable;
import weka.core.Instance;
import weka.core.Instances;

public class DecisionTableFilter extends AbstractFilter {

	public DecisionTableFilter() {
		this.setFeature();
		this.setOption();
		this.classifierName = "Decision Table"; 
		trainingData = new Instances("Rel", this.getFvWekaAttributes(), 1000);
		trainingData.setClassIndex(CLASS_INDEX_TOUCH);
	}

	@Override
	protected void setOption() {
		Log.d("set Option", "in seting option in classifier");
		String[] options = null; 
		try {
			dt = new DecisionTable();
			options = weka.core.Utils.splitOptions("-X 1");
			dt.setOptions(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void testData() {
		Log.d("testing data", "in testing data phase .....");
		Evaluation eTest;
		try {
			eTest = new Evaluation(trainingData);
			// eTest.evaluateModel(dt, testData);
			System.out.println(eTest.toSummaryString(
					"\n Results\n=============\n", false));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	@Override
	public void trainingData() {
		Log.d("TrainingData", "in traininData phase.....");
		try {
			dt.buildClassifier(trainingData);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void predictInstance(Instance currentInstance) {
		dataUnLabeled = new Instances("TestInstances", getFvWekaAttributes(),
				10);
		dataUnLabeled.add(currentInstance);
		dataUnLabeled.setClassIndex(dataUnLabeled.numAttributes() - 1);
		double[] prediction;
		try {
			prediction = dt.distributionForInstance(dataUnLabeled
					.firstInstance());
			// output predictions
			this.printResult(prediction);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataUnLabeled.clear();
	}

	@Override
	public Classifier returnClassifier() {
		return dt;
	}

}
