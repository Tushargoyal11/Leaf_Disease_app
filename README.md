Note: I've included Model code, Flask Code and Android code files in this repo. You will find those files at the start. Below i've provided the structure of the project files.

# Project Structure Overview

This repository contains an integrated Flask backend and Android application for potato leaf disease detection. Below is the organization of key files:

## Combined Flask Backend & Model Files
flask-api/
├── app.py # Main Flask application (routes + server config)
├── model_loader.py # ML model loading and prediction logic
├── inception_model.h5 # Trained InceptionV3 model weights
├── preprocessor.py # Image preprocessing utilities
├── requirements.txt # Python dependencies
└── temp/ # Temporary image storage (excluded in .gitignore)


## Android Application Files
android/
├── app/
│ ├── src/main/
│ │ ├── java/com/example/leafdiseaseapp/
│ │ │ ├── MainActivity.kt # Primary app screen
│ │ │ ├── ApiClient.kt # Retrofit API interface
│ │ │ └── ResultActivity.kt # Results display
│ │ └── res/ # UI layouts and resources
│ └── build.gradle # Android dependencies
└── build.gradle # Project-level config


## Shared Configuration
project-root/
├── .gitattributes # Git LFS tracking for model files
├── .gitignore # Ignores temp files and build artifacts
└── README.md # Project documentation


### Key Integration Points:
1. `flask-api/app.py` handles both:
   - Web server initialization (Flask)
   - Model predictions (TensorFlow)

2. Android communicates via:
   - `ApiClient.kt` → `flask-api/app.py` `/predict` endpoint

3. Model files are directly loaded by:
   - `model_loader.py` in the Flask application
