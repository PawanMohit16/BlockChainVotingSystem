import cv2
import face_recognition
import os
import sys

def load_known_face(username):
    photos_dir = "user-photos"
    user_dir = os.path.join(photos_dir, username)
    if not os.path.exists(user_dir):
        return None
    image_files = [f for f in os.listdir(user_dir) if f.lower().endswith((".jpg", ".jpeg", ".png"))]
    if not image_files:
        return None
    image_path = os.path.join(user_dir, image_files[0])
    known_image = face_recognition.load_image_file(image_path)
    known_encoding = face_recognition.face_encodings(known_image)
    if not known_encoding:
        return None
    return known_encoding[0]

def verify_with_multiple_frames(username, num_frames=3):
    known_encoding = load_known_face(username)
    if known_encoding is None:
        print("No known face found for user:", username)
        return False
    cap = cv2.VideoCapture(0)
    matches = 0
    for i in range(num_frames):
        ret, frame = cap.read()
        if not ret:
            continue
        face_locations = face_recognition.face_locations(frame)
        face_encodings = face_recognition.face_encodings(frame, face_locations)
        for face_encoding in face_encodings:
            if face_recognition.compare_faces([known_encoding], face_encoding, tolerance=0.6)[0]:
                matches += 1
    cap.release()
    print(f"Matched frames: {matches}/{num_frames}")
    return matches == num_frames

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python verify_multiple_frames.py <username>")
        sys.exit(1)
    username = sys.argv[1]
    result = verify_with_multiple_frames(username)
    print("Verification result:", "SUCCESS" if result else "FAILED")
    sys.exit(0 if result else 1)
