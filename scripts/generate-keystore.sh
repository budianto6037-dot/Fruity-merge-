#!/bin/bash

# Script untuk generate keystore secara otomatis untuk Android App
# Usage: bash generate-keystore.sh

set -e

# Warna untuk output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Android Keystore Generator${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Check if keytool is available
if ! command -v keytool &> /dev/null; then
    echo -e "${RED}Error: keytool tidak ditemukan. Pastikan Java Development Kit (JDK) sudah terinstall.${NC}"
    exit 1
fi

# Set default values
KEYSTORE_FILE="my-upload-key.jks"
KEY_ALIAS="upload"
KEY_SIZE="2048"
VALIDITY="10000"
ALGORITHM="RSA"

# Prompt user for input
echo -e "${YELLOW}Masukkan informasi untuk keystore:${NC}\n"

read -p "Nama file keystore [${KEYSTORE_FILE}]: " input_keystore
KEYSTORE_FILE="${input_keystore:-$KEYSTORE_FILE}"

read -p "Key alias [${KEY_ALIAS}]: " input_alias
KEY_ALIAS="${input_alias:-$KEY_ALIAS}"

read -p "Validity (hari) [${VALIDITY}]: " input_validity
VALIDITY="${input_validity:-$VALIDITY}"

# Prompt for keystore password
read -sp "Keystore password: " STORE_PASSWORD
echo
read -sp "Confirm keystore password: " STORE_PASSWORD_CONFIRM
echo

if [ "$STORE_PASSWORD" != "$STORE_PASSWORD_CONFIRM" ]; then
    echo -e "${RED}Error: Passwords tidak cocok!${NC}"
    exit 1
fi

# Prompt for key password
read -sp "Key password (tekan Enter untuk sama dengan keystore password): " KEY_PASSWORD
echo

if [ -z "$KEY_PASSWORD" ]; then
    KEY_PASSWORD="$STORE_PASSWORD"
fi

# Prompt for certificate information
echo -e "\n${YELLOW}Masukkan informasi sertifikat:${NC}\n"

read -p "First and Last Name: " CN
read -p "Organization Unit (e.g., Development): " OU
read -p "Organization (e.g., Your Company): " O
read -p "City or Locality: " L
read -p "State or Province: " ST
read -p "Country Code (e.g., ID): " C

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo -e "\n${YELLOW}Warning: File $KEYSTORE_FILE sudah ada.${NC}"
    read -p "Apakah Anda ingin menggantikannya? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Dibatalkan.${NC}"
        exit 1
    fi
    rm -f "$KEYSTORE_FILE"
fi

# Generate keystore
echo -e "\n${BLUE}Generating keystore...${NC}\n"

keytool -genkey -v \
    -keystore "$KEYSTORE_FILE" \
    -keyalg "$ALGORITHM" \
    -keysize "$KEY_SIZE" \
    -validity "$VALIDITY" \
    -alias "$KEY_ALIAS" \
    -storepass "$STORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "CN=$CN, OU=$OU, O=$O, L=$L, ST=$ST, C=$C"

if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}✓ Keystore berhasil dibuat!${NC}\n"
    
    # Display keystore information
    echo -e "${BLUE}Informasi Keystore:${NC}"
    echo -e "${BLUE}===================${NC}"
    echo "File: $KEYSTORE_FILE"
    echo "Key Alias: $KEY_ALIAS"
    echo "Algorithm: $ALGORITHM"
    echo "Key Size: $KEY_SIZE bits"
    echo "Validity: $VALIDITY hari"
    echo ""
    
    # Verify keystore
    echo -e "${BLUE}Verifying keystore...${NC}"
    keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASSWORD" -alias "$KEY_ALIAS"
    
    echo -e "\n${GREEN}✓ Keystore verification successful!${NC}\n"
    
    # Create .env file
    echo -e "${BLUE}Membuat file .env...${NC}\n"
    
    KEYSTORE_PATH="$(pwd)/$KEYSTORE_FILE"
    
    cat > .env << EOF
# Keystore Configuration
KEYSTORE_PATH=$KEYSTORE_PATH
STORE_PASSWORD=$STORE_PASSWORD
KEY_PASSWORD=$KEY_PASSWORD
EOF

    echo -e "${GREEN}✓ File .env berhasil dibuat${NC}\n"
    
    echo -e "${YELLOW}PENTING:${NC}"
    echo -e "${RED}1. Jangan commit keystore ke repository!${NC}"
    echo -e "${RED}2. Jangan commit file .env ke repository!${NC}"
    echo -e "${YELLOW}3. Pastikan .gitignore sudah berisi:${NC}"
    echo -e "   - *.jks"
    echo -e "   - .env"
    echo ""
    
    echo -e "${BLUE}Untuk GitHub Actions, tambahkan secrets berikut:${NC}"
    echo -e "${BLUE}Settings → Secrets and variables → Actions${NC}"
    echo ""
    echo "KEYSTORE_PATH: $KEYSTORE_PATH"
    echo "STORE_PASSWORD: (copy dari .env)"
    echo "KEY_PASSWORD: (copy dari .env)"
    echo ""
    
    echo -e "${GREEN}Setup keystore selesai!${NC}\n"
    
else
    echo -e "${RED}Error: Gagal membuat keystore!${NC}"
    exit 1
fi
