#!/bin/bash

# Generate iOS App Icons from Android Icon
# This script converts the Android app icon to all required iOS sizes

set -e

SOURCE_ICON="../androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
OUTPUT_DIR="CineFillerApp/Assets.xcassets/AppIcon.appiconset"

echo "🎨 Generating iOS app icons from Android icon..."
echo "Source: $SOURCE_ICON"
echo "Output: $OUTPUT_DIR"
echo ""

# Check if source icon exists
if [ ! -f "$SOURCE_ICON" ]; then
    echo "❌ Error: Source icon not found at $SOURCE_ICON"
    exit 1
fi

# Check if sips is available (macOS image tool)
if ! command -v sips &> /dev/null; then
    echo "❌ Error: sips command not found. This script requires macOS."
    exit 1
fi

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Function to generate icon at specific size
generate_icon() {
    local size=$1
    local filename=$2
    echo "  Generating ${size}x${size} → $filename"
    sips -z $size $size "$SOURCE_ICON" --out "$OUTPUT_DIR/$filename" > /dev/null 2>&1
}

# Generate all required iOS icon sizes
echo "Generating icons..."

# iPhone icons
generate_icon 40 "icon-20@2x.png"       # 20pt @2x
generate_icon 60 "icon-20@3x.png"       # 20pt @3x
generate_icon 58 "icon-29@2x.png"       # 29pt @2x
generate_icon 87 "icon-29@3x.png"       # 29pt @3x
generate_icon 80 "icon-40@2x.png"       # 40pt @2x
generate_icon 120 "icon-40@3x.png"      # 40pt @3x
generate_icon 120 "icon-60@2x.png"      # 60pt @2x
generate_icon 180 "icon-60@3x.png"      # 60pt @3x

# iPad icons
generate_icon 20 "icon-20-ipad.png"     # 20pt @1x
generate_icon 40 "icon-20-ipad@2x.png"  # 20pt @2x
generate_icon 29 "icon-29-ipad.png"     # 29pt @1x
generate_icon 58 "icon-29-ipad@2x.png"  # 29pt @2x
generate_icon 40 "icon-40-ipad.png"     # 40pt @1x
generate_icon 80 "icon-40-ipad@2x.png"  # 40pt @2x
generate_icon 76 "icon-76.png"          # 76pt @1x
generate_icon 152 "icon-76@2x.png"      # 76pt @2x
generate_icon 167 "icon-83.5@2x.png"    # 83.5pt @2x

# App Store icon (must be 1024x1024)
echo "  Generating 1024x1024 → icon-1024.png"
sips -z 1024 1024 "$SOURCE_ICON" --out "$OUTPUT_DIR/icon-1024.png" > /dev/null 2>&1

echo ""
echo "✅ iOS app icons generated successfully!"
echo "📍 Location: $OUTPUT_DIR"
echo ""
echo "Note: The 192x192 Android icon has been upscaled to 1024x1024 for iOS."
echo "      For best quality, create a native 1024x1024 icon if possible."
