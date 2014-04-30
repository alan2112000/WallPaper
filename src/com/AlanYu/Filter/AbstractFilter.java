package com.AlanYu.Filter;

import android.database.Cursor;
import android.util.Log;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public abstract class AbstractFilter {

	protected FastVector fvWekaAttributes;
	protected Classifier cModel;
	protected Instances trainingData;
	protected Instances testData;
	protected Instances dataUnLabeled;
	protected J48 tree;
	protected IBk ibk;
	protected KStar kstar;
	protected final static int CLASS_INDEX_TOUCH = 4;

	protected abstract void setOption();

	public abstract void testData();

	public abstract void trainingData();

	public abstract void predictInstance(Instance currentInstance);

	public FastVector getFvWekaAttributes() {
		return fvWekaAttributes;
	}

	protected void setFvWekaAttributes(FastVector fvWekaAttributes) {
		this.fvWekaAttributes = fvWekaAttributes;
	}

	protected Instances getTrainingData() {
		return trainingData;
	}

	protected void setTrainingData(Instances trainingData) {
		this.trainingData = trainingData;
	}

	protected Instances getTestData() {
		return testData;
	}

	public void setTestData(Instances testData) {
		this.testData = testData;
	}

	public Instances getDataUnLabeled() {
		return dataUnLabeled;
	}

	public void setDataUnLabeled(Instances dataUnLabeled) {
		this.dataUnLabeled = dataUnLabeled;
	}

	protected void setFeature() {

		Log.d("Seting Feature ", "seting feature ");

		// add numeric attribute
		Attribute attribute1 = new Attribute("x");
		Attribute attribute2 = new Attribute("y");
		Attribute attribute3 = new Attribute("pressure");
		Attribute attribute4 = new Attribute("size");

		// nominal attribute along with its values

		// declare class attribute
		FastVector fvClassVal = new FastVector(8);
		fvClassVal.addElement("owner");
		fvClassVal.addElement("eraser");
		fvClassVal.addElement("fucker");
		fvClassVal.addElement("gary");
		fvClassVal.addElement("peg");
		fvClassVal.addElement("weiling");
		fvClassVal.addElement("joanne");
		fvClassVal.addElement("mako");
		Attribute classAttribute = new Attribute("the class", fvClassVal);

		// Declare feature vector
		fvWekaAttributes = new FastVector(5);
		fvWekaAttributes.addElement(attribute1);
		fvWekaAttributes.addElement(attribute2);
		fvWekaAttributes.addElement(attribute3);
		fvWekaAttributes.addElement(attribute4);
		fvWekaAttributes.addElement(classAttribute);

		// isTrainingSet = new Instances("Rel", fvWekaAttributes, 1000);
		// isTrainingSet.setClassIndex(5);

	}

	protected void setInstances(Cursor cursor) {

		if (cursor.moveToFirst())
			do {
				Instance iExample = new DenseInstance(5);
				Log.d("abstractFilter", "setting instance value ");
				// iExample.setValue((Attribute) getFvWekaAttributes()
				// .elementAt(0),
				// Double.valueOf(cursor.getString(cursor.getColumnIndex("X"))));
				iExample.setValue((Attribute) getFvWekaAttributes()
						.elementAt(1), Double.valueOf(cursor.getString(cursor
						.getColumnIndex("Y"))));
				iExample.setValue((Attribute) getFvWekaAttributes()
						.elementAt(2), Double.valueOf(cursor.getString(cursor
						.getColumnIndex("PRESSURE"))));
				iExample.setValue((Attribute) getFvWekaAttributes()
						.elementAt(3), Double.valueOf(cursor.getString(cursor
						.getColumnIndex("SIZE"))));
				iExample.setValue((Attribute) getFvWekaAttributes()
						.elementAt(4), Double.valueOf(cursor.getString(cursor
						.getColumnIndex("LABEL"))));
				Log.d("abstract Filter  ", "add to training set  ");
				trainingData.add(iExample);
				testData.add(iExample);
			} while (cursor.moveToNext());
		cursor.close();
	}

	public void addInstanceToTrainingData(Instance instance) {
		trainingData.add(instance);
	}

	public void addInstanceToTestData(Instance instance) {
		testData.add(instance);
	}
}
