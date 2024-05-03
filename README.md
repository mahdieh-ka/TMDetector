# TMDetector

TMDetector is an Android application developed in Java designed to detect a user's mode of transportation. 
The application utilizes a Random Forest classifier and leverages data from various sensors to achieve accurate detection. It's structured into three main modules: Collection, Detection, and Extraction.

Modules
1. Collection Module
This module records ground truth data to generate a new classifier. It utilizes three different sensors: GPS, Accelerometer, Magnetometer

3. Detection Module
Utilizes a pre-trained Random Forest classifier to detect the user’s current mode of transport based on the sensor data. The Random Forest classifier is trained using the scikit-learn library. Then the model is exported as a JPMML file. 

4. Extraction Module
This module focuses on visualizing and analyzing sensor data to extract an efficient set of features that can be used to improve the classifier's performance, particularly the magnetometer sensor. 






