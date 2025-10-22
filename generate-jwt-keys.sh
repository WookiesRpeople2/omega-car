#!/bin/bash
# Script to generate proper JWT keys

echo "========================================="
echo "JWT Key Generator"
echo "========================================="
echo ""

# Generate a 64-character signing secret (512 bits)
SIGNING_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo "1. JWT Signing Secret (for HMAC-SHA256):"
echo "   jwt.signing.secret=$SIGNING_SECRET"
echo ""

# Generate a 32-byte AES key (256 bits) encoded in base64
AES_KEY=$(openssl rand -base64 32 | tr -d '\n')
echo "2. JWT AES Encryption Key (for AES-256-GCM):"
echo "   jwt.aes.key=$AES_KEY"
echo ""

echo "========================================="
echo "Copy these values to your application.properties"
echo "========================================="

