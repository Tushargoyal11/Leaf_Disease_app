from flask import Flask, request, jsonify
from tensorflow.keras.models import load_model
import numpy as np
import os
from PIL import Image
import cv2
import os
from flask import Flask, request, jsonify
app = Flask(__name__)

# Load model
try:
    model = load_model('inception_model.h5', compile=False)
    print("✅ Model loaded successfully")
except Exception as e:
    print(f"❌ Error loading model: {str(e)}")
    raise

CLASS_LABELS = {
    0: "Early Blight",
    1: "Fungal Diseases",
    2: "Healthy",
    3: "Late Blight",
    4: "Plant Pests",
    5: "Potato Cyst Nematode",
    6: "Potato Virus"
}


def preprocess_image(img_path):
    try:
        img = cv2.imread(img_path)
        if img is None:
            raise ValueError("Could not read image")
        img = cv2.resize(img, (112, 112))
        return np.expand_dims(img, axis=0)
    except Exception as e:
        print(f"⚠️ Error preprocessing: {str(e)}")
        raise


@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'No file uploaded'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'Empty file'}), 400

    os.makedirs('temp', exist_ok=True)
    temp_path = os.path.join('temp', file.filename)

    try:
        file.save(temp_path)
        img_array = preprocess_image(temp_path)
        predictions = model.predict(img_array)
        return jsonify({
            'prediction': CLASS_LABELS[np.argmax(predictions[0])],
            'confidence': float(np.max(predictions[0]))
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)


if __name__ == '__main__':
    os.makedirs('temp', exist_ok=True)
    app.run(host='0.0.0.0', port=5001, debug=True)  # Using port 5001 now
