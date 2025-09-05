#!/bin/bash

# Test script để upload file lên MinIO service
# Sử dụng: ./test-upload.sh <path-to-file>

if [ $# -eq 0 ]; then
    echo "Usage: ./test-upload.sh <path-to-file>"
    echo "Example: ./test-upload.sh /path/to/document.pdf"
    exit 1
fi

FILE_PATH=$1

if [ ! -f "$FILE_PATH" ]; then
    echo "Error: File '$FILE_PATH' not found"
    exit 1
fi

echo "Uploading file: $FILE_PATH"
echo "To: http://localhost:8080/api/files/upload"
echo ""

# Upload file
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@$FILE_PATH" \
  -H "Accept: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n"

echo ""
echo "Upload completed!"
