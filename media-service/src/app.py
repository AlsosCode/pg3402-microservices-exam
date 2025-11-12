#!/usr/bin/env python3
"""
Simple static file server for Pokemon card images
"""
from flask import Flask, send_from_directory, jsonify
from flask_cors import CORS
import os

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Configuration
IMAGES_DIR = os.environ.get('IMAGES_DIR', '/app/images')
PORT = int(os.environ.get('PORT', 8084))

@app.route('/health')
def health():
    """Health check endpoint"""
    return jsonify({"status": "UP", "service": "media-service"})

@app.route('/images/<path:filename>')
def serve_image(filename):
    """Serve card image files"""
    try:
        return send_from_directory(IMAGES_DIR, filename)
    except FileNotFoundError:
        return jsonify({"error": "Image not found"}), 404

@app.route('/images')
def list_images():
    """List all available images"""
    try:
        files = [f for f in os.listdir(IMAGES_DIR) if f.endswith('.webp')]
        return jsonify({
            "total": len(files),
            "images": sorted(files)
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    # Ensure images directory exists
    os.makedirs(IMAGES_DIR, exist_ok=True)

    print(f"Starting Media Service on port {PORT}")
    print(f"Serving images from: {IMAGES_DIR}")

    app.run(host='0.0.0.0', port=PORT)
