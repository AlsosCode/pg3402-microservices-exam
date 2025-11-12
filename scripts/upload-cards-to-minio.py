#!/usr/bin/env python3
"""
Upload card images to MinIO and generate SQL for updating image URLs
"""
import os
import sys
from pathlib import Path

# MinIO configuration
MINIO_ENDPOINT = "http://localhost:9000"
MINIO_BUCKET = "card-images"
CARD_IMAGES_DIR = "../Scarlet&Violet-Cards"

def parse_filename(filename):
    """
    Parse filename format: Name.SV1EN.CardNumber.ID.thumb.webp
    Returns: (card_number, filename)
    """
    if filename == ".DS_Store" or not filename.endswith(".webp"):
        return None, None

    parts = filename.split(".")
    if len(parts) >= 3:
        try:
            card_number = int(parts[2])
            return card_number, filename
        except ValueError:
            return None, None
    return None, None

def generate_minio_commands():
    """Generate mc (MinIO Client) commands to upload files"""
    script_dir = Path(__file__).parent
    images_dir = script_dir / CARD_IMAGES_DIR

    if not images_dir.exists():
        print(f"Error: Directory {images_dir} does not exist", file=sys.stderr)
        sys.exit(1)

    print("#!/bin/bash")
    print("# Upload card images to MinIO")
    print()
    print("# Configure MinIO client (mc)")
    print("docker run --rm -it --network mikrotjenestereksamen2025_pkmn-network \\")
    print("  --entrypoint=/bin/sh \\")
    print("  minio/mc -c \"")
    print("mc alias set minio http://minio:9000 minioadmin minioadmin")
    print("mc mb minio/card-images --ignore-existing")
    print("\"")
    print()

    # Count files
    card_files = []
    for filename in sorted(os.listdir(images_dir)):
        card_number, fname = parse_filename(filename)
        if card_number:
            card_files.append((card_number, fname))

    print(f"# Found {len(card_files)} card images")
    print()

    # Generate upload commands using docker cp + mc
    print("# Create temporary container to upload files")
    print("CONTAINER_ID=$(docker run -d --network mikrotjenestereksamen2025_pkmn-network minio/mc sleep 300)")
    print()

    for card_number, filename in card_files:
        local_path = images_dir / filename
        print(f"docker cp \"{local_path}\" $CONTAINER_ID:/tmp/{filename}")
        print(f"docker exec $CONTAINER_ID mc alias set minio http://minio:9000 minioadmin minioadmin")
        print(f"docker exec $CONTAINER_ID mc cp /tmp/{filename} minio/card-images/{filename}")

    print()
    print("# Cleanup")
    print("docker stop $CONTAINER_ID")
    print("docker rm $CONTAINER_ID")

def generate_sql_updates():
    """Generate SQL UPDATE statements for card image URLs"""
    script_dir = Path(__file__).parent
    images_dir = script_dir / CARD_IMAGES_DIR

    if not images_dir.exists():
        print(f"Error: Directory {images_dir} does not exist", file=sys.stderr)
        sys.exit(1)

    print("-- Update card image URLs")
    print("-- Generated from card images directory")
    print()

    card_files = []
    for filename in sorted(os.listdir(images_dir)):
        card_number, fname = parse_filename(filename)
        if card_number:
            card_files.append((card_number, fname))

    for card_number, filename in sorted(card_files, key=lambda x: x[0]):
        image_url = f"{MINIO_ENDPOINT}/{MINIO_BUCKET}/{filename}"
        print(f"UPDATE cards SET image_url = '{image_url}' WHERE card_number = {card_number} AND card_set_id = 1;")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 upload-cards-to-minio.py [minio-commands|sql-updates]")
        sys.exit(1)

    command = sys.argv[1]

    if command == "minio-commands":
        generate_minio_commands()
    elif command == "sql-updates":
        generate_sql_updates()
    else:
        print(f"Unknown command: {command}")
        print("Available commands: minio-commands, sql-updates")
        sys.exit(1)
