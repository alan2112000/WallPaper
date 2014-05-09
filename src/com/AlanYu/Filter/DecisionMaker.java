package com.AlanYu.Filter;

import android.util.Log;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Vote;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("deprecation")
public class DecisionMaker extends Vote {

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
	private Instances dataUnLabeled;
	private FastVector fvWekaAttributes;
	private String classifierName = "Vote";
	private Instances trainingData;
	private double threshold ;

	public DecisionMaker() {
		init();

	}

	private void init() {
		j48 = new J48Classifier();
		knn = new kNNClassifier();
		kstar = new KStarClassifier();
		dt = new DecisionTableFilter();
		randomF = new RandomForestClassifier();
		this.setOption();
		this.setFeature();
	}

	public void addDataToTraining(Instances trainingData) {
		Log.d("DecisionMaker", "set trainging data");
		j48.setTrainingData(trainingData);
		knn.setTrainingData(trainingData);
		kstar.setTrainingData(trainingData);
		dt.setTrainingData(trainingData);
		randomF.setTrainingData(trainingData);
		this.trainingData = trainingData;
	};

	public void buildClassifier() {
		Log.d("DecisionMaker", "build classifier");
		j48.trainingData();
		knn.trainingData();
		kstar.trainingData();
		dt.trainingData();
		randomF.trainingData();
		Classifier cls[] = { j48.returnClassifier(), knn.returnClassifier(),
				kstar.returnClassifier(), dt.returnClassifier(),
				randomF.returnClassifier() };
		this.setClassifiers(cls);
	};

	public int getFinalLabel(Instances unLabelData) {
		return predictionInstances(unLabelData);
	}

	// caculate how many label is owner in the unLabelData
	public int predictionInstances(Instances unLabelData) {
		int ownerLabelNumber = 0;
		int otherLabelNumber = 0;
		Log.d("DecisionMaker", "Predicting Label");
		double prediction[] = null;
		for (int i = 0; i < unLabelData.numInstances(); i++) {
			try {
				prediction = this
						.distributionForInstanceMajorityVoting(unLabelData
								.instance(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (prediction[DecisionMaker.IS_OWNER] > prediction[DecisionMaker.IS_OTHER]) {
				ownerLabelNumber++;
			}
			otherLabelNumber++;
		}
		Log.d("DecisionMaker", "owner :" + Double.toString(ownerLabelNumber)
				+ "other : " + Double.toString(otherLabelNumber));
		
		double precision = (double)ownerLabelNumber/(ownerLabelNumber+otherLabelNumber);
		if ( precision > this.getThreshold())
			return IS_OWNER;
		else
			return IS_OTHER;
	}

	public FastVector getWekaAttributes() {
		return j48.getFvWekaAttributes();
	}

	public double[] voteForInstance(Instance currentInstance) {

		dataUnLabeled = new Instances("TestInstances",
				this.getFvWekaAttributes(), 10);
		dataUnLabeled.setClassIndex(dataUnLabeled.numAttributes() - 1);
		dataUnLabeled.add(currentInstance);
		currentInstance.setDataset(dataUnLabeled);
		double[] prediction = new double[trainingData.numClasses()];
		try {
			prediction = this
					.distributionForInstanceMajorityVoting(currentInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataUnLabeled.remove(0);
		return prediction;

	}

	protected void setOption() {
		try {
			String[] options = weka.core.Utils.splitOptions("-R"
					+ Double.toString(Vote.MAJORITY_VOTING_RULE));
			this.setOptions(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		FastVector<String> fvClassVal = new FastVector<String>(2);
		fvClassVal.addElement("owner");
		fvClassVal.addElement("other");
		Attribute classAttribute = new Attribute("the class", fvClassVal);

		// Declare feature vector
		setFvWekaAttributes(new FastVector(5));
		getFvWekaAttributes().addElement(attribute1);
		getFvWekaAttributes().addElement(attribute2);
		getFvWekaAttributes().addElement(attribute3);
		getFvWekaAttributes().addElement(attribute4);
		getFvWekaAttributes().addElement(classAttribute);

	}

	public void printResult(double[] prediction) {
		System.out.println("\n Result " + this.classifierName
				+ "\n =========================\n");
		for (int i = 0; i < prediction.length; i++) {
			System.out.println("Probability of class "
					+ trainingData.classAttribute().value(i) + " : "
					+ Double.toString(prediction[i]));
		}
	}

	public FastVector getFvWekaAttributes() {
		return fvWekaAttributes;
	}

	public void setFvWekaAttributes(FastVector fvWekaAttributes) {
		this.fvWekaAttributes = fvWekaAttributes;
	}

	@Override
	public double[] distributionForInstanceMajorityVoting(Instance instance)
			throws Exception {

		double[] probs = new double[instance.classAttribute().numValues()];
		double[] votes = new double[probs.length];

		for (int i = 0; i < m_Classifiers.length; i++) {
			probs = getClassifier(i).distributionForInstance(instance);
			int maxIndex = 0;
			for (int j = 0; j < probs.length; j++) {
				if (probs[j] > probs[maxIndex])
					maxIndex = j;
			}

			// Consider the cases when multiple classes happen to have the same
			// probability
			for (int j = 0; j < probs.length; j++) {
				if (probs[j] == probs[maxIndex])
					votes[j]++;
			}
		}

		int tmpMajorityIndex = 0;
		for (int k = 1; k < votes.length; k++) {
			if (votes[k] > votes[tmpMajorityIndex])
				tmpMajorityIndex = k;
		}

		// Consider the cases when multiple classes receive the same amount of
		// votes
		// Vector<Integer> majorityIndexes = new Vector<Integer>();
		// for (int k = 0; k < votes.length; k++) {
		// if (votes[k] == votes[tmpMajorityIndex])
		// majorityIndexes.add(k);
		// }
		// Resolve the ties according to a uniform random distribution
		// int majorityIndex =
		// majorityIndexes.get(m_Random.nextInt(majorityIndexes.size()));

		// set probs to 0
		for (int k = 0; k < probs.length; k++)
			probs[k] = 0;
		probs[tmpMajorityIndex] = 1; // the class that have been voted the most
										// receives 1

		return probs;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
}
