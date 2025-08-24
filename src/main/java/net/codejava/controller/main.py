def static_vs_live_verification(user_id):
    """
    Compare static image from user-photos/<user_id>/ with one live camera frame.
    Prints match result.
    """
    # Find static image
    photos_dir = "user-photos"
    user_dir = os.path.join(photos_dir, user_id)
    if not os.path.exists(user_dir):
        print(f"User folder not found: {user_dir}")
        return
    image_files = [f for f in os.listdir(user_dir) if f.lower().endswith((".jpg", ".jpeg", ".png"))]
    if not image_files:
        print(f"No image found in {user_dir}")
        return
    image_path = os.path.join(user_dir, image_files[0])
    print(f"Using reference image: {image_path}")
    img = Image.open(image_path).convert('RGB')
    ref_embedding = get_face_embedding(img)
    if ref_embedding is None:
        print("No face detected in reference image.")
        return
    # Capture live frame
    cap = cv2.VideoCapture(0)
    print("Capturing live frame...")
    ret, frame = cap.read()
    cap.release()
    if not ret:
        print("Failed to capture live frame.")
        return
    live_img = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
    live_embedding = get_face_embedding(live_img)
    if live_embedding is None:
        print("No face detected in live frame.")
        return
    sim = cosine_similarity(ref_embedding, live_embedding)
    print(f"Similarity: {sim:.2f}")
    if sim >= THRESHOLD:
        print("MATCH: You are the same person.")
    else:
        print("NO MATCH: You are NOT the same person.")

import os
import sys
import json
import argparse
import numpy as np
import torch
from facenet_pytorch import MTCNN, InceptionResnetV1
import cv2
from PIL import Image

# Paths
EMBEDDINGS_PATH = 'user_data/embeddings.json'
REGISTERED_USERS_DIR = 'registered_users'
THRESHOLD = 0.65  # Default threshold, can be tuned

# Ensure directories exist
os.makedirs('user_data', exist_ok=True)
os.makedirs(REGISTERED_USERS_DIR, exist_ok=True)

# Device setup
device = 'cuda' if torch.cuda.is_available() else 'cpu'
print(f"Using device: {device}")

# Models
mtcnn = MTCNN(image_size=160, margin=0, min_face_size=40, device=device)
resnet = InceptionResnetV1(pretrained='vggface2').eval().to(device)

def load_embeddings():
    if os.path.exists(EMBEDDINGS_PATH):
        with open(EMBEDDINGS_PATH, 'r') as f:
            return json.load(f)
    return {}

def save_embeddings(embeddings):
    with open(EMBEDDINGS_PATH, 'w') as f:
        json.dump(embeddings, f)

def get_face_embedding(img):
    # Detect and align face
    face = mtcnn(img)
    if face is None:
        return None
    face = face.unsqueeze(0).to(device)
    embedding = resnet(face).detach().cpu().numpy()[0]
    # Normalize
    embedding = embedding / np.linalg.norm(embedding)
    return embedding.tolist()

def register_user(image_path, user_id):
    print(f"Registering user '{user_id}' from {image_path}")
    img = Image.open(image_path).convert('RGB')
    embedding = get_face_embedding(img)
    if embedding is None:
        print("No face detected. Registration failed.")
        return
    embeddings = load_embeddings()
    if user_id in embeddings:
        if isinstance(embeddings[user_id], list) and isinstance(embeddings[user_id][0], list):
            embeddings[user_id].append(embedding)
        else:
            embeddings[user_id] = [embeddings[user_id], embedding]
    else:
        embeddings[user_id] = [embedding]
    save_embeddings(embeddings)
    print(f"User '{user_id}' registered successfully.")

def cosine_similarity(a, b):
    a = np.array(a)
    b = np.array(b)
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))

def capture_best_frame(num_frames=3):
    print("Capturing image from webcam...")
    cap = cv2.VideoCapture(0)
    frames = []
    for i in range(num_frames):
        ret, frame = cap.read()
        if not ret:
            print("Failed to capture frame.")
            continue
        img = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        embedding = get_face_embedding(img)
        if embedding is not None:
            print(f"Frame {i+1}: Face detected.")
            frames.append((embedding, frame))
        else:
            print(f"Frame {i+1}: No face detected.")
    cap.release()
    if not frames:
        print("No face detected in any frame.")
        return None
    # Pick the frame with the most centered face (or just the first for simplicity)
    return frames[0][0]

def verify_user(user_id, threshold=THRESHOLD):
    embeddings = load_embeddings()
    if user_id not in embeddings:
        print("User not registered.")
        return
    live_embedding = capture_best_frame()
    if live_embedding is None:
        print("Access Denied: No face detected.")
        return
    user_embeddings = embeddings[user_id]
    if not isinstance(user_embeddings[0], list):
        user_embeddings = [user_embeddings]
    similarities = [cosine_similarity(live_embedding, emb) for emb in user_embeddings]
    best_similarity = max(similarities)
    print(f"Similarity: {best_similarity:.2f}")
    if best_similarity >= threshold:
        print("Access Granted.")
    else:
        print("Access Denied.")

def main():
    parser = argparse.ArgumentParser(description='Face Verification System')
    parser.add_argument('--register', nargs=2, metavar=('user_id', 'image_path'), help='Register a new user')
    parser.add_argument('--verify', metavar='user_id', help='Verify user via webcam')
    parser.add_argument('--static-verify', metavar='user_id', help='Static image vs live camera verification')
    parser.add_argument('--threshold', type=float, default=THRESHOLD, help='Cosine similarity threshold')
    args = parser.parse_args()

    if args.register:
        user_id, image_path = args.register
        register_user(image_path, user_id)
    elif args.verify:
        verify_user(args.verify, threshold=args.threshold)
    elif args.static_verify:
        static_vs_live_verification(args.static_verify)
    else:
        print("Usage:")
        print("  python main.py --register user1 path/to/image.jpg")
        print("  python main.py --verify user1")
        print("  python main.py --static-verify user1")

if __name__ == '__main__':
    main()