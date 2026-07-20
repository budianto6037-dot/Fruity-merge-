#!/bin/bash

# All-in-One Setup Script untuk Fruity-merge CI/CD Pipeline
# Script ini akan membuat keystore dan setup semua yang diperlukan

set -e

# Warna untuk output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function untuk print header
print_header() {
    echo -e "\n${PURPLE}========================================${NC}"
    echo -e "${PURPLE}$1${NC}"
    echo -e "${PURPLE}========================================${NC}\n"
}

# Function untuk print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function untuk print error
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function untuk print warning
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function untuk print info
print_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

print_header "Fruity-merge CI/CD Complete Setup"

# Check Java
print_info "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
    print_success "Java found: $JAVA_VERSION"
else
    print_error "Java not found. Please install JDK."
    echo "Install from: https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

# Check keytool
print_info "Checking keytool..."
if command -v keytool &> /dev/null; then
    print_success "keytool found"
else
    print_error "keytool not found"
    exit 1
fi

# Check git
print_info "Checking git..."
if command -v git &> /dev/null; then
    print_success "git found"
else
    print_error "git not found"
    exit 1
fi

# Menu
echo -e "${BLUE}Pilih opsi:${NC}"
echo "1) Generate Keystore"
echo "2) Setup GitHub Secrets"
echo "3) Generate Keystore + Setup GitHub Secrets"
echo "4) Verify Setup"
echo "5) Exit"
read -p "Pilihan (1-5): " CHOICE

case $CHOICE in
    1)
        print_header "Generate Keystore"
        
        # Default values
        KEYSTORE_FILE="my-upload-key.jks"
        KEY_ALIAS="upload"
        KEY_SIZE="2048"
        VALIDITY="10000"
        ALGORITHM="RSA"
        
        # Get user input
        read -p "Nama file keystore [${KEYSTORE_FILE}]: " input
        KEYSTORE_FILE="${input:-$KEYSTORE_FILE}"
        
        read -p "Key alias [${KEY_ALIAS}]: " input
        KEY_ALIAS="${input:-$KEY_ALIAS}"
        
        read -p "Validity (hari) [${VALIDITY}]: " input
        VALIDITY="${input:-$VALIDITY}"
        
        # Password input
        read -sp "Keystore password: " STORE_PASSWORD
        echo
        read -sp "Confirm keystore password: " STORE_PASSWORD_CONFIRM
        echo
        
        if [ "$STORE_PASSWORD" != "$STORE_PASSWORD_CONFIRM" ]; then
            print_error "Passwords tidak cocok!"
            exit 1
        fi
        
        read -sp "Key password (Enter untuk same as keystore password): " KEY_PASSWORD
        echo
        
        if [ -z "$KEY_PASSWORD" ]; then
            KEY_PASSWORD="$STORE_PASSWORD"
        fi
        
        # Certificate info
        echo -e "\n${YELLOW}Masukkan informasi sertifikat:${NC}"
        read -p "First and Last Name: " CN
        read -p "Organization Unit: " OU
        read -p "Organization: " O
        read -p "City/Locality: " L
        read -p "State/Province: " ST
        read -p "Country Code: " C
        
        # Check if exists
        if [ -f "$KEYSTORE_FILE" ]; then
            print_warning "File $KEYSTORE_FILE sudah ada"
            read -p "Ganti? (y/n): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                print_warning "Dibatalkan"
                exit 1
            fi
            rm -f "$KEYSTORE_FILE"
        fi
        
        # Generate
        print_info "Generating keystore..."
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
            print_success "Keystore berhasil dibuat!"
            
            # Verify
            print_info "Verifying keystore..."
            keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$STORE_PASSWORD" -alias "$KEY_ALIAS" | head -20
            
            # Create .env
            print_info "Creating .env file..."
            KEYSTORE_PATH="$(pwd)/$KEYSTORE_FILE"
            
            cat > .env << EOF
# Keystore Configuration
KEYSTORE_PATH=$KEYSTORE_PATH
STORE_PASSWORD=$STORE_PASSWORD
KEY_PASSWORD=$KEY_PASSWORD
EOF
            
            print_success ".env file created"
            
            echo -e "\n${YELLOW}REMINDER:${NC}"
            echo -e "${RED}1. Do NOT commit keystore to git!${NC}"
            echo -e "${RED}2. Do NOT commit .env to git!${NC}"
            echo -e "${YELLOW}3. Make sure .gitignore includes:${NC}"
            echo "   - *.jks"
            echo "   - .env"
        else
            print_error "Failed to create keystore"
            exit 1
        fi
        ;;
        
    2)
        print_header "Setup GitHub Secrets"
        
        if [ ! -f ".env" ]; then
            print_error ".env file not found!"
            print_info "Run option 1 to generate keystore first"
            exit 1
        fi
        
        # Check GitHub CLI
        if ! command -v gh &> /dev/null; then
            print_error "GitHub CLI not found"
            print_info "Install from: https://cli.github.com"
            exit 1
        fi
        
        source .env
        
        # Login
        print_info "GitHub CLI login..."
        gh auth login 2>/dev/null || true
        
        # Get repo info
        echo -e "\n${BLUE}GitHub Repository Info:${NC}"
        read -p "GitHub username/org: " GITHUB_USER
        read -p "Repository name: " GITHUB_REPO
        
        # Setup secrets
        print_info "Setting up secrets..."
        
        echo "$KEYSTORE_PATH" | gh secret set KEYSTORE_PATH -R "$GITHUB_USER/$GITHUB_REPO" 2>/dev/null
        print_success "KEYSTORE_PATH set"
        
        echo "$STORE_PASSWORD" | gh secret set STORE_PASSWORD -R "$GITHUB_USER/$GITHUB_REPO" 2>/dev/null
        print_success "STORE_PASSWORD set"
        
        echo "$KEY_PASSWORD" | gh secret set KEY_PASSWORD -R "$GITHUB_USER/$GITHUB_REPO" 2>/dev/null
        print_success "KEY_PASSWORD set"
        
        # Verify
        print_info "Verifying secrets..."
        gh secret list -R "$GITHUB_USER/$GITHUB_REPO"
        
        print_success "GitHub Secrets setup complete!"
        ;;
        
    3)
        print_header "Complete Setup (Keystore + Secrets)"
        
        # Generate keystore
        echo -e "\n${BLUE}Step 1: Generate Keystore${NC}"
        bash "$0" <<< "1" || exit 1
        
        # Setup secrets
        echo -e "\n${BLUE}Step 2: Setup GitHub Secrets${NC}"
        bash "$0" <<< "2" || exit 1
        
        print_success "Complete setup finished!"
        ;;
        
    4)
        print_header "Verify Setup"
        
        echo -e "${BLUE}Checking files:${NC}"
        
        if [ -f "my-upload-key.jks" ] || [ -f "my-app-key.jks" ]; then
            print_success "Keystore file found"
        else
            print_error "Keystore file not found"
        fi
        
        if [ -f ".env" ]; then
            print_success ".env file found"
            print_info "Contents:"
            grep -E "KEYSTORE_PATH|STORE_PASSWORD|KEY_PASSWORD" .env | sed 's/=.*/=(set)/'
        else
            print_error ".env file not found"
        fi
        
        echo -e "\n${BLUE}Checking GitHub Secrets:${NC}"
        
        if command -v gh &> /dev/null; then
            read -p "GitHub username/org: " GITHUB_USER
            read -p "Repository name: " GITHUB_REPO
            
            print_info "Secrets on GitHub:"
            gh secret list -R "$GITHUB_USER/$GITHUB_REPO" 2>/dev/null || print_warning "Could not fetch secrets"
        else
            print_warning "GitHub CLI not installed"
        fi
        
        echo -e "\n${BLUE}Checking .gitignore:${NC}"
        
        if grep -q "*.jks" .gitignore 2>/dev/null; then
            print_success "*.jks in .gitignore"
        else
            print_warning "*.jks NOT in .gitignore"
        fi
        
        if grep -q ".env" .gitignore 2>/dev/null; then
            print_success ".env in .gitignore"
        else
            print_warning ".env NOT in .gitignore"
        fi
        ;;
        
    5)
        print_info "Goodbye!"
        exit 0
        ;;
        
    *)
        print_error "Invalid choice"
        exit 1
        ;;
esac

echo -e "\n${GREEN}Setup complete!${NC}\n"
echo -e "${BLUE}Next steps:${NC}"
echo "1. Commit changes: git add . && git commit -m 'Add CI/CD config'"
echo "2. Push to GitHub: git push origin main"
echo "3. Create a release tag: git tag -a v1.0.0 -m 'Version 1.0.0'"
echo "4. Push tags: git push origin --tags"
echo "5. GitHub Actions will automatically build and release!"
echo ""
