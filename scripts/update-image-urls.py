#!/usr/bin/env python3
"""
Generate SQL migration to update card image URLs based on local files
"""
import os
import sys
from pathlib import Path

CARD_IMAGES_DIR = Path(__file__).parent.parent / "Scarlet&Violet-Cards"
MEDIA_SERVICE_BASE_URL = "http://media-service:8084/images"

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
            print(f"Warning: Could not parse card number from {filename}", file=sys.stderr)
            return None, None
    return None, None

def main():
    if not CARD_IMAGES_DIR.exists():
        print(f"Error: Directory {CARD_IMAGES_DIR} does not exist", file=sys.stderr)
        sys.exit(1)

    print("-- Update card image URLs based on local image files")
    print("-- Generated automatically")
    print()

    card_files = []
    for filename in os.listdir(CARD_IMAGES_DIR):
        card_number, fname = parse_filename(filename)
        if card_number and fname:
            card_files.append((card_number, fname))

    # Sort by card number
    card_files.sort(key=lambda x: x[0])

    print(f"-- Found {len(card_files)} card images")
    print()

    for card_number, filename in card_files:
        # URL encode the filename
        image_url = f"{MEDIA_SERVICE_BASE_URL}/{filename}"
        print(f"UPDATE cards SET image_url = '{image_url}' WHERE card_number = {card_number} AND card_set_id = 1;")

    print()
    print(f"-- Updated {len(card_files)} card image URLs")

if __name__ == "__main__":
    main()
