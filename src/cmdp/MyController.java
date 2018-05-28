package cmdp;

import java.awt.Label;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.*;

public class MyController implements Initializable{
	@FXML
	private TextField myTextField1;
	@FXML
	private TextField myTextField2;
	
	@FXML
	private Button openDataFileButton;
	@FXML
	private Button openTestDataButton;
	@FXML
	private Button traningButton;
	@FXML
	private Button evaluatingButton;
	@FXML
	private Button testingButton;
	//user choose .txt file
	public String trainfilepath;
	public String testfilepath;
	//output target .arff file
	public String outFilePathTrain;
	public String outFilePathEvaluate;
	public String outFilePathTest;
	
	public BufferedReader traind;
	public Instances train;
	public NaiveBayes nb;
	public Evaluation eval;
    
	public int numOfTestInstance = 0;
	public double act[][];
	public double predict[];
	
	public ArrayList<double[][]> actStateTransPro;
	public ArrayList<State> stateList;
	public ArrayList<Action> actionList;
	public ArrayList<stateTransition> stateTransList;
	public ArrayList<actTransMatrix> actStateTransList;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	
	}
	//Convert txt training file to two arff file:training & evaluate
	public void traintxt2arff(String filepath) throws IOException {
		outFilePathTrain = filepath.substring(0,filepath.length()-4)+"Training.arff";
		outFilePathEvaluate = filepath.substring(0,filepath.length()-4)+"Evaluate.arff";
		
		int numOfInstance;
		FileReader reader = new FileReader(filepath);
		BufferedReader br = new BufferedReader(reader);
		
		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(filepath)));
		lnr.skip(Long.MAX_VALUE);
		numOfInstance = lnr.getLineNumber()+1-1;
		lnr.close();
		System.out.println(numOfInstance);
		FileWriter writer1 = new FileWriter(outFilePathTrain);  
        BufferedWriter bw1 = new BufferedWriter(writer1); 
        FileWriter writer2 = new FileWriter(outFilePathEvaluate);  
        BufferedWriter bw2 = new BufferedWriter(writer2);
        //first line are the attributes
		String str = null;
		str = br.readLine();
		String[] attributes = str.split(" ");
		//write head information for training file and evaluate file
		bw1.write("@relation"+" "+"TrainingData"+"\r\n"); 
		for(int i = 0;i<attributes.length-1;i++){  
            bw1.write("@attribute"+" "+attributes[i]+" "+"numeric"+"\r\n");  
        }  
		bw1.write("@attribute"+" "+attributes[attributes.length-1]+" "+"{1, 2, 3}"+"\r\n");  
		bw1.write("@data"+"\r\n");
        bw2.write("@relation"+" "+"TrainingData"+"\r\n");
        for(int i = 0;i<attributes.length-1;i++){  
            bw2.write("@attribute"+" "+attributes[i]+" "+"numeric"+"\r\n");  
        }  
        bw2.write("@attribute"+" "+attributes[attributes.length-1]+" "+"{1, 2, 3}"+"\r\n");
        bw2.write("@data"+"\r\n"); 
        //split 0.8 data to train file and 0.2 to evaluate file
        while((str=br.readLine())!=null) {
        	int i;
        	for(i=0;i<numOfInstance*0.8;i++) {		
    			bw1.write(str+"\r\n");
    			str=br.readLine();
    		}
    		for(;i<numOfInstance;i++) {
    			bw2.write(str+"\r\n");	
    			str=br.readLine();
    		}
		}
		
		br.close();
		reader.close();
        bw1.close();  
        bw2.close();  
        writer1.close();  
        writer2.close();
	}
	//transfer test.txt to test.arff,every fourth and fifth columns are actions,generate the action two-dimensional vector
	public double[][] testtxt2arff(String filepath) throws IOException{
		outFilePathTest = filepath.substring(0,filepath.length()-4)+"Test.arff";
		
		FileReader reader = new FileReader(filepath);
		BufferedReader br = new BufferedReader(reader);
		FileWriter writer = new FileWriter(outFilePathTest);  
        BufferedWriter bw = new BufferedWriter(writer); 
        
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(filepath)));
		lnr.skip(Long.MAX_VALUE);
		numOfTestInstance = lnr.getLineNumber()+1-1;
		lnr.close();
		
        //initialize two-dimensional vector
		double tmp[][] = new double[numOfTestInstance][2];
		
      //first line are the attributes
      	String str = null;
      	str = br.readLine();
      	String[] attributes = str.split(" ");
      	
      //write head information for training file and evaluate file
      	bw.write("@relation"+" "+"TestingData"+"\r\n");
      	for(int i=0;i<attributes.length-1;i++) {
      		bw.write("@attribute"+" "+attributes[i]+" "+"numeric"+"\r\n"); 
      	}
      	bw.write("@attribute"+" "+attributes[attributes.length-1]+" "+"{1, 2, 3}"+"\r\n");
        bw.write("@data"+"\r\n");   
      	
        String[] vector;
        while((str=br.readLine())!=null) {
        	for(int i=0;i<numOfTestInstance;i++) {	
        		vector = null;
            	vector=str.split(" ");
    			bw.write(str+"\r\n");
    			tmp[i][0]=Double.valueOf(vector[4]);
    			tmp[i][1]=Double.valueOf(vector[5]);
    			str=br.readLine();
    		}
		}
        
        br.close();
		reader.close();
        bw.close();   
        writer.close();  
        
        return tmp;
	}
	//Through FileChooser to open the training data file.
	public void openTrainingDataFile(ActionEvent event) {
		System.out.println("Please open the trainging data file,format like .txt");
		Stage Filestage = null;
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Training Data File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
		File selectedFile = fileChooser.showOpenDialog(Filestage);
		trainfilepath = selectedFile.getPath();
		System.out.println(trainfilepath);
		myTextField1.setText(trainfilepath);
	}
	public void openTestingDataFile(ActionEvent event) {
		System.out.println("Please open the testing data file,format like .txt");
		Stage Filestage = null;
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Testing Data File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files","*.txt"));
		File selectedFile = fileChooser.showOpenDialog(Filestage);
		testfilepath = selectedFile.getPath();
		System.out.println(testfilepath);
		myTextField2.setText(testfilepath);		
	}
	
	
	public void Traingingdata(ActionEvent event) throws Exception {
		//将。txt文件转化为。arff文件;
		traintxt2arff(trainfilepath);
		//为读入文件建立文件流，构建训练实例train
		traind = new BufferedReader(new FileReader(outFilePathTrain));
		train = new Instances(traind);
		
		//将最后一列的状态值作为训练的目标属性
		train.setClassIndex(train.numAttributes()-1);
		traind.close();
		
		//使用朴素贝叶斯方法训练分类器
		nb = new NaiveBayes();
		nb.buildClassifier(train);
	}
	
	public double evaluatingClassifier() throws Exception {
		//build stream for read file and construct Instance for evaluating
		BufferedReader evaluated = new BufferedReader(new FileReader(outFilePathEvaluate));
		Instances evaluate = new Instances(evaluated);
		//uses the last attribute as class attribute
		evaluate.setClassIndex(evaluate.numAttributes()-1);
		evaluated.close();
		
		//use evaluateModel() to evaluate the classifier
		eval = new Evaluation(train);
		eval.evaluateModel(nb, evaluate);
		//calculate the accurate of the classifier
		double accurate = 1-eval.errorRate();
		
		System.out.println(eval.toSummaryString("\nResult\n==========\n",true));
	    System.out.println(eval.toClassDetailsString()); 
		System.out.println(eval.toMatrixString());
		System.out.println(eval.correct());
		System.out.println(1-eval.errorRate());
		System.out.println(accurate);
		
		return 1-eval.errorRate();
	}
	
	public void TestingData(ActionEvent event) throws Exception{
		System.out.println("Now,We begin to test Driver Data!");
		act = testtxt2arff(testfilepath);
		
		for(int i=0;i<act.length;i++) {
			for(int j=0;j<act[0].length;j++) {
				System.out.print(act[i][j]+" ");
			}
			System.out.println(" ");
		}
		//为驾驶员行为模型的训练数据集文件建立文件流，并生成训练实例
		BufferedReader testd = new BufferedReader(new FileReader(outFilePathTest));
		Instances test = new Instances(testd);
		testd.close();
		//uses the last attribute as class attribute
		test.setClassIndex(test.numAttributes()-1);
		
		
		//初始化Evaluation类对象eval
		eval = new Evaluation(train);
		//利用分类器对训练集test进行预测，将结果保存在predict数组中
		predict = eval.evaluateModel(nb, test);
		
		for(int i=0;i<predict.length;i++) {
			System.out.print(predict[i]+1+" ");
		}
		System.out.println(" ");
		//for 4 action,generate 4 two-dimensional vectors;
		double[][] act1transpro = new double[3][3];
		double[][] act2transpro = new double[3][3];
		double[][] act3transpro = new double[3][3];
		double[][] act4transpro = new double[3][3];
		
		int act1count = 0;
		int act2count = 0;
		int act3count = 0;
		int act4count = 0;
		
		int act1st1count = 0;
		int act1st2count = 0;
		int act1st3count = 0;
		int act2st1count = 0;
		int act2st2count = 0;
		int act2st3count = 0;
		int act3st1count = 0;
		int act3st2count = 0;
		int act3st3count = 0;
		int act4st1count = 0;
		int act4st2count = 0;
		int act4st3count = 0;
		
		for(int i=0;i<act.length-1;i++) {
			if(act[i][0]==0 &&act[i][1]==0) {
				act1count += 1;
				if(predict[i]==0&&predict[i+1]==0) {
					act1transpro[0][0] += 1.0;
					act1st1count +=1;
				}else if(predict[i]==0&&predict[i+1]==1) {
					act1transpro[0][1] += 1.0;
					act1st1count +=1;
				}else if(predict[i]==0&&predict[i+1]==2) {
					act1transpro[0][2] += 1.0;
					act1st1count +=1;
				}else if(predict[i]==1&&predict[i+1]==0) {
					act1transpro[1][0] += 1.0;
					act1st2count +=1;
				}else if(predict[i]==1&&predict[i+1]==1) {
					act1transpro[1][1] += 1.0;
					act1st2count +=1;
				}else if(predict[i]==1&&predict[i+1]==2) {
					act1transpro[1][2] += 1.0;
					act1st2count +=1;
				}else if(predict[i]==2&&predict[i+1]==0) {
					act1transpro[2][0] += 1.0;
					act1st3count +=1;
				}else if(predict[i]==2&&predict[i+1]==1) {
					act1transpro[2][1] += 1.0;
					act1st3count +=1;
				}else if(predict[i]==2&&predict[i+1]==2) {
					act1transpro[2][2] += 1.0;
					act1st3count +=1;
				}
			}else if(act[i][0]==0&&act[i][1]==1) {
				act2count += 1;
				if(predict[i]==0&&predict[i+1]==0) {
					act2transpro[0][0] += 1.0;
					act2st1count +=1;
				}else if(predict[i]==0&&predict[i+1]==1) {
					act2transpro[0][1] += 1.0;
					act2st1count +=1;
				}else if(predict[i]==0&&predict[i+1]==2) {
					act2transpro[0][2] += 1.0;
					act2st1count +=1;
				}else if(predict[i]==1&&predict[i+1]==0) {
					act2transpro[1][0] += 1.0;
					act2st2count +=1;
				}else if(predict[i]==1&&predict[i+1]==1) {
					act2transpro[1][1] += 1.0;
					act2st2count +=1;
				}else if(predict[i]==1&&predict[i+1]==2) {
					act2transpro[1][2] += 1.0;
					act2st2count +=1;
				}else if(predict[i]==2&&predict[i+1]==0) {
					act2transpro[2][0] += 1.0;
					act2st3count +=1;
				}else if(predict[i]==2&&predict[i+1]==1) {
					act2transpro[2][1] += 1.0;
					act2st3count +=1;
				}else if(predict[i]==2&&predict[i+1]==2) {
					act2transpro[2][2] += 1.0;
					act2st3count +=1;
				}
			}else if(act[i][0]==1&&act[i][1]==0) {
				act3count += 1;
				if(predict[i]==0&&predict[i+1]==0) {
					act3transpro[0][0] += 1.0;
					act3st1count +=1;
				}else if(predict[i]==0&&predict[i+1]==1) {
					act3transpro[0][1] += 1.0;
					act3st1count +=1;
				}else if(predict[i]==0&&predict[i+1]==2) {
					act3transpro[0][2] += 1.0;
					act3st1count +=1;
				}else if(predict[i]==1&&predict[i+1]==0) {
					act3transpro[1][0] += 1.0;
					act3st2count +=1;
				}else if(predict[i]==1&&predict[i+1]==1) {
					act3transpro[1][1] += 1.0;
					act3st2count +=1;
				}else if(predict[i]==1&&predict[i+1]==2) {
					act3transpro[1][2] += 1.0;
					act3st2count +=1;
				}else if(predict[i]==2&&predict[i+1]==0) {
					act3transpro[2][0] += 1.0;
					act3st3count +=1;
				}else if(predict[i]==2&&predict[i+1]==1) {
					act3transpro[2][1] += 1.0;
					act3st3count +=1;
				}else if(predict[i]==2&&predict[i+1]==2) {
					act3transpro[2][2] += 1.0;
					act3st3count +=1;
				}
			}else if(act[i][0]==1&&act[i][1]==1) {
				act4count += 1;
				if(predict[i]==0&&predict[i+1]==0) {
					act4transpro[0][0] += 1.0;
					act4st1count +=1;
				}else if(predict[i]==0&&predict[i+1]==1) {
					act4transpro[0][1] += 1.0;
					act4st1count +=1;
				}else if(predict[i]==0&&predict[i+1]==2) {
					act4transpro[0][2] += 1.0;
					act4st1count +=1;
				}else if(predict[i]==1&&predict[i+1]==0) {
					act4transpro[1][0] += 1.0;
					act4st2count +=1;
				}else if(predict[i]==1&&predict[i+1]==1) {
					act4transpro[1][1] += 1.0;
					act4st2count +=1;
				}else if(predict[i]==1&&predict[i+1]==2) {
					act4transpro[1][2] += 1.0;
					act4st2count +=1;
				}else if(predict[i]==2&&predict[i+1]==0) {
					act4transpro[2][0] += 1.0;
					act4st3count +=1;
				}else if(predict[i]==2&&predict[i+1]==1) {
					act4transpro[2][1] += 1.0;
					act4st3count +=1;
				}else if(predict[i]==2&&predict[i+1]==2) {
					act4transpro[2][2] += 1.0;
					act4st3count +=1;
				}
			}
		}
		if(act1st1count!=0) {
			act1transpro[0][0] /= act1st1count;
			act1transpro[0][1] /= act1st1count;
			act1transpro[0][2] /= act1st1count;
		}
		if(act1st2count!=0) {
			act1transpro[1][0] /= act1st2count;
			act1transpro[1][1] /= act1st2count;
			act1transpro[1][2] /= act1st2count;
		}
		if(act1st3count!=0) {
			act1transpro[2][0] /= act1st3count;
			act1transpro[2][1] /= act1st3count;
			act1transpro[2][2] /= act1st3count;
		}
		if(act2st1count!=0) {
			act2transpro[0][0] /= act2st1count;
			act2transpro[0][1] /= act2st1count;
			act2transpro[0][2] /= act2st1count;
		}
		if(act2st2count!=0) {
			act2transpro[1][0] /= act2st2count;
			act2transpro[1][1] /= act2st2count;
			act2transpro[1][2] /= act2st2count;
		}
		if(act2st3count!=0) {
			act2transpro[2][0] /= act2st3count;
			act2transpro[2][1] /= act2st3count;
			act2transpro[2][2] /= act2st3count;
		}
		if(act3st1count!=0) {
			act3transpro[0][0] /= act3st1count;
			act3transpro[0][1] /= act3st1count;
			act3transpro[0][2] /= act3st1count;
		}
		if(act3st2count!=0) {
			act3transpro[1][0] /= act3st2count;
			act3transpro[1][1] /= act3st2count;
			act3transpro[1][2] /= act3st2count;
		}
		if(act3st3count!=0) {
			act3transpro[2][0] /= act3st3count;
			act3transpro[2][1] /= act3st3count;
			act3transpro[2][2] /= act3st3count;
		}
		if(act4st1count!=0) {
			act4transpro[0][0] /= act4st1count;
			act4transpro[0][1] /= act4st1count;
			act4transpro[0][2] /= act4st1count;
		}
		if(act4st2count!=0) {
			act4transpro[1][0] /= act4st2count;
			act4transpro[1][1] /= act4st2count;
			act4transpro[1][2] /= act4st2count;
		}
		if(act4st3count!=0) {
			act4transpro[2][0] /= act4st3count;
			act4transpro[2][1] /= act4st3count;
			act4transpro[2][2] /= act4st3count;
		}
		remainTwoDigit(act1transpro);
		remainTwoDigit(act2transpro);
		remainTwoDigit(act3transpro);
		remainTwoDigit(act4transpro);
		actStateTransPro = new ArrayList<double[][]>();
		actStateTransPro.add(act1transpro);
		actStateTransPro.add(act2transpro);
		actStateTransPro.add(act3transpro);
		actStateTransPro.add(act4transpro);
		
		getFeatureList(actStateTransPro);
		outputPrismFile(testfilepath,actStateTransList);
		Graphviz gv = new Graphviz("C:\\Users\\73232\\eclipse-workspace\\cmdp\\statetransfer", "C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe");
		String tmp = gv.generateDotString(actStateTransList);
    	gv.start_graph();
    	gv.add(tmp);
    	gv.end_graph();
    	try {
    		gv.run();
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	
		System.out.println("==========act1transpro==========");
		for(int i=0;i<3;i++) {
			System.out.print("          ");
			for(int j=0;j<3;j++) {
				System.out.print(act1transpro[i][j]+" ");
			}
			System.out.println(" ");
		}
		//System.out.println("act1count:"+act1count);
		
		System.out.println("==========act2transpro==========");
		for(int i=0;i<3;i++) {
			System.out.print("          ");
			for(int j=0;j<3;j++) {
				System.out.print(act2transpro[i][j]+" ");
			}
			System.out.println(" ");
		}
		//System.out.println("act2count:"+act2count);
		
		System.out.println("==========act3transpro==========");
		for(int i=0;i<3;i++) {
			System.out.print("          ");
			for(int j=0;j<3;j++) {
				System.out.print(act3transpro[i][j]+" ");
			}
			System.out.println(" ");
		}
		//System.out.println("act3count:"+act3count);
		
		
		System.out.println("==========act4transpro==========");
		for(int i=0;i<3;i++) {
			System.out.print("          ");
			for(int j=0;j<3;j++) {
				System.out.print(act4transpro[i][j]+" ");
			}
			System.out.println(" ");
		}
		//System.out.println("act4count:"+act4count);
		
	}
	
	public void getFeatureList(ArrayList<double[][]>actStateTransPro) {
		int actnum = actStateTransPro.size();
		Action act1 = new Action(1,"OnlyDriving");
		Action act2 = new Action(2,"OnlyEditing");
		Action act3 = new Action(3,"OnlyCalling");
		Action act4 = new Action(4,"Both");
		actionList = new ArrayList<Action>();
		actionList.add(act1);
		actionList.add(act2);
		actionList.add(act3);
		actionList.add(act4);
		
		int statesize = actStateTransPro.get(0).length;
		State st1 = new State(1,"Focus");
		State st2 = new State(2,"SemiFocus");
		State st3 = new State(3,"Distracted");
		stateList = new ArrayList<State>();
		stateList.add(st1);
		stateList.add(st2);
		stateList.add(st3);
		
		stateTransList = new ArrayList<stateTransition>();
		actStateTransList = new ArrayList<actTransMatrix>();
		for(int i=0;i<actnum;i++) {
			double tmp[][] = new double[statesize][statesize];
			tmp = actStateTransPro.get(i);
			for(int j=0;j<tmp.length;j++) {
				for(int k=0;k<tmp[0].length;k++) {
					if(tmp[j][k]!=0) {
						stateTransition stt = new stateTransition(j,k);
						actTransMatrix atm = new actTransMatrix(stt,actionList.get(i),tmp[j][k]); 
						stateTransList.add(stt);
						actStateTransList.add(atm);
					}
				}
			}
		}
		System.out.println("-----------------dfasdfadsf-----------");
		System.out.println(stateList.size());
		System.out.println(actionList.size());
		System.out.println(stateTransList.size());
		System.out.println(actStateTransList.size());
//		actStateTransList.get(0).getStateTransition().getStart();
	}
	public void outputPrismFile(String filepath,ArrayList<actTransMatrix> actStateTransList) throws IOException {
		String outputPrismFilePath = filepath.substring(0,filepath.length()-4)+"Prism.txt";
		
		FileWriter writer = new FileWriter(outputPrismFilePath);  
        BufferedWriter bw = new BufferedWriter(writer); 
        
        bw.write("mdp"+"\r\n");
        bw.write("module"+" Driver_behavior"+"\r\n");
        bw.write("state"+" : "+"[0..2]"+";"+"\r\n");
//        bw.write("init"+":"+actStateTransList.get(0).getStateTransition().getStart());
        
        System.out.println("Does program run this?");
        
        int oldStartState = 0;
        int oldAction = 0;
        int currentStartState = actStateTransList.get(0).getStateTransition().getStart();
        int currentEndState = actStateTransList.get(0).getStateTransition().getEnd();
        int currentAction = actStateTransList.get(0).getAction().getActID();
        double probability = actStateTransList.get(0).getProbability();
        
        //write firstline
        bw.write("[]"+"state"+"="+currentStartState+" & "+"act"+"="+currentAction);
        bw.write("->");
		bw.write(probability+":"+"(state'="+currentEndState+")");
        for(int i=1;i<actStateTransList.size();i++) {
        	currentStartState = actStateTransList.get(i).getStateTransition().getStart();
        	currentEndState = actStateTransList.get(i).getStateTransition().getEnd();
        	currentAction = actStateTransList.get(i).getAction().getActID();
        	probability = actStateTransList.get(i).getProbability();
        	
        	if(currentStartState!=oldStartState || currentAction!=oldAction) {
        		bw.write(";"+"\r\n");
        		bw.write("[]"+"states"+"="+currentStartState+" & "+"act"+"="+currentAction);
        		bw.write("->");
        		bw.write(probability+":"+"(state'="+currentEndState+")");
        	}
        	else {
        		bw.write("+");
        		bw.write(probability+":"+"(state'="+currentEndState+")");
        	}
        	oldStartState = currentStartState;
    		oldAction = currentAction;
        }
        
        bw.write("\r\n"+"endmodule"+"\r\n");
        
        bw.close();
        writer.close();
	}
	
	public void remainTwoDigit(double num[][]) {
		for(int i=0;i<num.length;i++) {
			for(int j=0;j<num[0].length;j++) {
				if(num[i][j]!=0) {
					BigDecimal tmp = new BigDecimal(num[i][j]);
					num[i][j] = tmp.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
				}
			}
		}
	}
}
