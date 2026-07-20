#!/bin/bash

# Script untuk setup GitHub Secrets otomatis dari keystore
# Usage: bash setup-secrets.sh

set -e

# Warna untuk output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}GitHub Secrets Setup${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${RED}Error: File .env tidak ditemukan!${NC}"
    echo -e "${YELLOW}Jalankan generate-keystore.sh terlebih dahulu.${NC}"
    exit 1
fi

# Check if GitHub CLI is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}Error: GitHub CLI tidak ditemukan.${NC}"
    echo -e "${YELLOW}Install dari: https://cli.github.com${NC}"
    exit 1
fi

# Load environment variables
source .env

echo -e "${BLUE}Informasi Repository:${NC}"
echo "User: $GITHUB_USER"
echo "Repository: $GITHUB_REPO"
echo ""

# Prompt for repository info
read -p "GitHub username: " GITHUB_USER
read -p "GitHub repository name: " GITHUB_REPO

echo -e "\n${BLUE}Login ke GitHub...${NC}"
gh auth login

echo -e "\n${BLUE}Setting up secrets...${NC}\n"

# Setup KEYSTORE_PATH secret
echo "Setting KEYSTORE_PATH..."
echo "$KEYSTORE_PATH" | gh secret set KEYSTORE_PATH -b "$GITHUB_USER/$GITHUB_REPO" 2>/dev/null || \
gh secret set KEYSTORE_PATH --body "$KEYSTORE_PATH" -R "$GITHUB_USER/$GITHUB_REPO"

# Setup STORE_PASSWORD secret
echo "Setting STORE_PASSWORD..."
echo "$STORE_PASSWORD" | gh secret set STORE_PASSWORD -b "$GITHUB_USER/$GITHUB_REPO" 2>/dev/null || \
gh secret set STORE_PASSWORD --body "$STORE_PASSWORD" -R "$GITHUB_USER/$GITHUB_REPO"

# Setup KEY_PASSWORD secret
echo "Setting KEY_PASSWORD..."
echo "$KEY_PASSWORD" | gh secret set KEY_PASSWORD -b "$GITHUB_USER/$GITHUB_REPO" 2>/dev/null || \
gh secret set KEY_PASSWORD --body "$KEY_PASSWORD" -R "$GITHUB_USER/$GITHUB_REPO"

echo -e "\n${GREEN}✓ Semua secrets berhasil di-setup!${NC}\n"

echo -e "${BLUE}Secrets yang telah dibuat:${NC}"
gh secret list -R "$GITHUB_USER/$GITHUB_REPO"

echo -e "\n${GREEN}Setup selesai!${NC}\n"
