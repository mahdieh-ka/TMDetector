# TMDetector
To download the app ask for permision from bahar.kamalian@gmail.com 
download the app from https://drive.google.com/drive/folders/1CVdQ9_LekB7zn33HsRhsKNMNc5Kor0sr?usp=sharing


The app contains two different modules:
1- Recording module: A module to record ground truth for generating a new classifier 
2- Detection module: A module using the Woorti classifier to detect the users modes. 

Recording module
The app uses three different sensors including GPS, accelerometer and magnetometer. 

Detection module
Detection module segmentize data with the window size of 90 seconds, sent the segments to classifier and generate a list of the probabilities for 6 mode classes: still, walk, bike, car, bus, train. Then it merges the segments with a post-processing phase for missclassification correction. 

Post-processing phase
Post-processing phase first step:
add a tag to each segment based on the tagging method. 
Tagging method tag the segments of a Trip to Strong, Candidate or Separation
 if probability of walk > 0.8 or the segment is classified as still--> the segment is strong
 if the probability of walk is the highest or the segment is classified as still--> the segment is Candidate and other segments are known separation segments
 
 Post-processing phase second step:
In this step segments are merged. Merging Strong and Candidate segments of a Trip:
•	if there is a sequence of segments with strong tag and still or walk mode, merge them to one segment the same mode
•	if there is a sequence of segments with strong, separation and then candidate tag, in case of |S| <= (|C|+2)/2 which S is the number of Strong segments and C is the number of Candidate segments
    

