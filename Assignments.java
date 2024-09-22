//in progress creating template for the project (movie dataset reading, masking, KNN for K=1)
import java.io.*;
import java.util.HashMap;

public class Tests {

	// Use we use 'static' for all methods to keep things simple, so we can call those methods main

	static void Assert (boolean res) // We use this to test our results - don't delete or modify!
	{
	 if(!res)	{
		 System.out.print("Something went wrong.");
	 	 System.exit(0);
	 }
	}

	// vector operations here:
	
    
    static double dot(double [] U, double [] V) { // dot product of two vectors 
    	double total = 0;
    	for (int i = 0; i < V.length; i++) {
    		total = U[i] * V[i] + total;
    		
    	}
    	return total;
    	
    }
    


static int NumberOfFeatures = 7; 
static double[] toFeatureVector(double id, String genre, double runtime, double year, double imdb, double rt, double budget, double boxOffice) {
     double[] feature = new double[NumberOfFeatures]; 
     //feature[0] = id; // this is pointless as it is the ID of the movie and doesn't mean much.
    
     switch (genre) { // We also use represent each movie genre as an integer number:
         case "Action":  feature[0] = 7; break; //I fixed the numbers up so that genre is actually useful. I'll explain it in the word doc.
         case "Drama":   feature[0] = 1; break;
         case "Romance": feature[0] = 3; break;
         case "Sci-Fi": feature[0] = 6; break;
         case "Adventure": feature[0] = 4; break;
         case "Horror": feature[0] = 2; break;
         case "Mystery": feature[0] = 0; break;
         case "Thriller": feature[0] = 5; break;  
     }
     //averages of all elements per column the trainingData. (calculated in Excel)
     double[] averages     = {3.8   , 2021.93, 109.41, 7.294 , 81.41 , 98.04 , 149.57}; 
     double[] correlations = {0.4198, 0.2833 , 0.2958, 0.0904, 0.1033, 0.3524, 0.1793}; //correlations of said elements
     
     feature[0] -= averages[0];  //applying averages, but may remove it for categorial features like genre and year.
     feature[1] = year - averages[1];
     feature[2] = runtime - averages[2];
     feature[3] = imdb - averages[3];
     feature[4] = rt - averages[4];
     feature[5] = budget - averages[5];
     feature[6] = boxOffice - averages[6];
     
     //applying further transformations to equalise it
     
     
     //Because of how powerful certain features are (e.g, boxOffice), logarithms are applied to ensure
     //said features don't dominate and screw the averages
     for(int i = 0; i < NumberOfFeatures; i++) {
    	 if(feature[i] > 0)
    		 feature[i] = Math.log(feature[i] > 1 ? feature[i] : 1) / Math.log(5);
     }
     //Correlations (calculated in excel) are used to make certain features more 'valuable' as
     //Higher correlation simply higher accuracy in the like/dislike department. 
     for(int i = 0; i < NumberOfFeatures; i++)
    	 feature[i] *= Math.pow(correlations[i],2)*100;
     return feature;
 }

// We are using the dot product to determine similarity:
static double similarity(double[] u, double[] v) {
  return dot(u, v);  
}

 // We have implemented KNN classifier for the K=1 case only. You are welcome to modify it to support any K
 // Modified to support k >= 1 (currently supports k == 11)
 
static int knnClassify(double[][] trainingData, int[] trainingLabels, double[] testFeature, int k) {
	
     double[] simArr = new double[trainingLabels.length]; //stores the similaries of each element when compared to the testFeature
     int[] indexes = new int[simArr.length]; //this is the indexes of each similarity. Is necessary once the array gets sorted
     
     for (int i = 0; i < trainingData.length; i++) { //comparing and storing similarity values
         simArr[i] = similarity(testFeature,trainingData[i]);
         indexes[i] = i;
     }
     
        
     //implemented a quicksort to speed up the sorting process
     quickSort(simArr, indexes);
    
     int count = 0;
     
     for(int i = 0; i < k; i++) { //takes the indexes of the trainingData with the highest similarities, and then adds their respective 'Liked it?' values
    	 count += trainingLabels[indexes[i]];
     }
     count = count > k/2 ? 1 : 0; //returns majority like/dislike choice
     
     return count;
     
     
     
 }


public static void quickSort(double[] arr, int[] indexes) {
    if (arr == null || arr.length == 0 || indexes == null || indexes.length == 0) {
        return;
    }
    quickSort(arr, indexes, 0, arr.length - 1);
}

private static void quickSort(double[] arr, int[] indexes, int low, int high) {
    if (low < high) {
        int pi = partition(arr, indexes, low, high);

        // Recursion used:
        quickSort(arr, indexes, low, pi - 1);
        quickSort(arr, indexes, pi + 1, high);
    }
}

private static int partition(double[] arr, int[] indexes, int low, int high) {
    double pivot = arr[high];
    int i = low - 1;

    for (int x = low; x < high; x++) {
        if (arr[x] > pivot) { 
            i++;

            double temp = arr[i];
            arr[i] = arr[x];
            arr[x] = temp;

            int tempInd = indexes[i];
            indexes[i] = indexes[x];
            indexes[x] = tempInd;
        }
    }


    double temp = arr[i + 1];
    arr[i + 1] = arr[high];
    arr[high] = temp;
    int tempInd = indexes[i + 1];
    indexes[i + 1] = indexes[high];
    indexes[high] = tempInd;

    return i + 1;
}

 
 static void loadData(String filePath, double[][] dataFeatures, int[] dataLabels) throws IOException {
     try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
         String line;
         int idx = 0;
         br.readLine(); // skip header line
         while ((line = br.readLine()) != null) {
             String[] values = line.split(",");
             // Assuming csv format: MovieID,Title,Genre,Runtime,Year,Lead Actor,Director,IMDB,RT(%),Budget,Box Office Revenue (in million $),Like it
             double id = Double.parseDouble(values[0]);
             String genre = values[2];
             double runtime = Double.parseDouble(values[3]);               
             double year = Double.parseDouble(values[4]);
             double imdb = Double.parseDouble(values[7]);                
             double rt = Double.parseDouble(values[8]);  
             double budget = Double.parseDouble(values[9]);  
             double boxOffice = Double.parseDouble(values[10]);  
             
             //Title, director and leadActor are not used because there's too many unique values in those columns
             //You can implement them if you please, though. Or if we need more accuracy. Current best I got: 67% (this is thanks to genre)
             dataFeatures[idx] = toFeatureVector(id, genre, runtime, year, imdb, rt, budget, boxOffice);
             dataLabels[idx] = Integer.parseInt(values[11]); // Assuming the label is the last column and is numeric
             idx++;
         }
     }
 }

 public static void main(String[] args) {
	 
	 
     int arrLength = 100; //this is for debugging purposes. leave it at 100 unless you are going to debug it.
     double[][] trainingData = new double[arrLength][];
     int[] trainingLabels = new int[arrLength];
     double[][] testingData = new double[arrLength][]; 
     int[] testingLabels = new int[arrLength]; 
     try {
         // You may need to change the path:        	        	
         loadData("bin\\training-set.csv", trainingData, trainingLabels);            
         loadData("bin\\testing-set.csv", testingData, testingLabels);          
     } 
     catch (IOException e) {
         System.out.println("Error reading data files: " + e.getMessage());
         return;
     }
     // now for the actual code:
     
     int k = (int)Math.sqrt(arrLength); //k value, change it for different k values.
     k += (1 - k % 2); //since KNN is better with an odd number, this basically just adds 1 if the value is even

     int correctPredictions = 0;

     // Add some lines here: ...
     for(int i = 0; i < arrLength; i++) { //comparison. Checks if the 'like' estimates are equal to the actual 'like' values
    	 int temp = knnClassify(trainingData, trainingLabels, testingData[i], k);
    	 System.out.println(temp);
    	 System.out.printf("actual: %s\n", testingLabels[i]);
    	 correctPredictions += (temp == testingLabels[i] ? 1 : 0);
     }
     
     double accuracy = (double) correctPredictions / testingData.length * 100;
     System.out.printf("A: %.2f%%\n", accuracy); 
     
 }

}
