# TMDetector

TMDetector is an Android application developed in Java that is designed to detect a user's mode of transportation. This app is solely developed for non-profit and research purposes. For any other usage, please contact bahar.kamalian@gmail.com. 

The application utilizes a Random Forest (RF) classifier and leverages data from various sensors to achieve accurate detection. It's structured into three main modules: Collection, Detection, and Extraction.

Modules
1. Collection Module:
This module records ground truth data to generate a new classifier. It utilizes three different sensors: GPS, Accelerometer, Magnetometer

2. Detection Module:
Utilizes a pre-trained Random Forest classifier to detect the user’s current mode of transport based on the sensor data. The Random Forest classifier is trained using the scikit-learn library. Then the model is exported as a JPMML file. MLP was also developed and evaluated to be integrated into the app. The RF model is publicly available, where
permissions allow (for license restrictions).

3. Visualization Module:
This module focuses on visualizing and analyzing sensor data to extract an efficient set of features that can be used to improve the classifier's performance, particularly the magnetometer sensor.

"The app is designed for scientific use cases".



