package com.AlanYu.Filter;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class TestFilter extends AbstractFilter {

	FastVector fvWekaAttributes;
	 Instances isTrainingSet ;
	 Instances isTestingSet ;
	 Classifier cModel;

	public TestFilter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	void loadData() {

		//
		 isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);
		 isTrainingSet.setClassIndex(3);
		 
		 //
		 // Create the instance
		 
		 Instance iExample = new DenseInstance(4);
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), 1.0);      
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), 0.5);      
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), "gray");
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(3), "positive");
		 
		 // add the instance
		 isTrainingSet.add(iExample);
	}

	@Override
	float testData() {
		
		 // Test the model
		 Evaluation eTest;
		try {
			eTest = new Evaluation(isTrainingSet);
			eTest.evaluateModel(cModel, isTestingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
		
	}

	@Override
	void trainingData() {
		
		cModel = (Classifier)new NaiveBayes();
		
		 try {
			cModel.buildClassifier(isTrainingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void generateData() {
		
		 isTestingSet = new Instances("Rel", fvWekaAttributes, 10);
		 isTestingSet.setClassIndex(3);
		 
		 //
		 // Create the instance
		 
		 Instance iExample = new DenseInstance(4);
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), 1.0);      
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), 0.5);      
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), "gray");
		 iExample.setValue((Attribute)fvWekaAttributes.elementAt(3), "positive");
		 
		 // add the instance
		 isTestingSet.add(iExample);
	}

	void setFeature() {

		// Declare two numeric attributes
		Attribute Attribute1 = new Attribute("firstNumeric");
		Attribute Attribute2 = new Attribute("secondNumeric");

		// Declare a nominal attribute along with its values
		FastVector<String> fvNominalVal = new FastVector(3);
		fvNominalVal.addElement("blue");
		fvNominalVal.addElement("gray");
		fvNominalVal.addElement("black");
		Attribute Attribute3 = new Attribute("aNominal", fvNominalVal);

		// Declare the class attribute along with its values
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("positive");
		fvClassVal.addElement("negative");
		Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

		// Declare the feature vector
		fvWekaAttributes = new FastVector(4);
		fvWekaAttributes.addElement(Attribute1);
		fvWekaAttributes.addElement(Attribute2);
		fvWekaAttributes.addElement(Attribute3);
		fvWekaAttributes.addElement(ClassAttribute);
	}
}
